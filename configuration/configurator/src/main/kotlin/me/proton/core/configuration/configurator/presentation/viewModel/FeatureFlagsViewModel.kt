/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.configuration.configurator.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.configuration.configurator.domain.CUSTOM_TYPE
import me.proton.core.configuration.configurator.domain.FeatureFlagsUseCase
import me.proton.core.configuration.configurator.featureflag.data.FeatureFlagsCacheManager
import javax.inject.Inject

@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    private val featureFlagsCacheManager: FeatureFlagsCacheManager,
    private val featureFlagUseCase: FeatureFlagsUseCase
) : ViewModel() {

    private val mutableErrorFlow: MutableSharedFlow<String> = MutableSharedFlow()

    val featureFlags: StateFlow<List<FeatureFlagsUseCase.FeatureFlagEntity>> =
        featureFlagUseCase.configState
            .map { it.toList() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
                initialValue = emptyList()
            )

    private fun launchCatching(block: suspend () -> Unit) = viewModelScope.launch {
        runCatching {
            block()
        }.onFailure {
            mutableErrorFlow.emit(it.message ?: "Unknown error")
        }
    }

    val errorFlow: SharedFlow<String> = mutableErrorFlow.asSharedFlow()

    internal fun resetToUnleashState() = launchCatching {
        featureFlagUseCase.resetToUnleashState()
    }

    internal fun fetchConfig() = launchCatching {
        featureFlagUseCase.initializeWithPersistedFlagsFromDataStore()
        getFeatureFlagsFromUnleashKeepOverrides()
        // Initiate fetching to trigger UI update
        featureFlagUseCase.syncConfigWithContentResolver()
    }

    internal fun saveConfig() = launchCatching {
        featureFlagUseCase.saveConfig()
    }

    private fun updateConfigField(key: String, value: Boolean) = launchCatching {
        featureFlagUseCase.updateConfigField(key, value)
    }

    fun toggleFeatureFlag(featureFlag: FeatureFlagsUseCase.FeatureFlagEntity, newValue: Boolean) =
        launchCatching {
            updateConfigField(featureFlag.name, newValue)
        }

    fun addCustomFeatureFlag(
        name: String,
        description: String?,
        defaultValue: Boolean,
        project: String
    ) =
        launchCatching {
            val newFlag = FeatureFlagsUseCase.FeatureFlagEntity(
                name = name,
                project = project.lowercase(),
                unleashValue = defaultValue,
                configuratorValue = defaultValue,
                description = description,
                type = CUSTOM_TYPE,
                strategiesJson = null,
                stale = false,
                impressionData = false
            )
            val updated = featureFlagUseCase.configState.value.toMutableSet().apply { add(newFlag) }
            featureFlagUseCase.updateFeatureFlags(updated)
            featureFlagUseCase.saveConfig()
        }

    fun removeCustomFeatureFlag(name: String) = launchCatching {
        val updated = featureFlagUseCase.configState.value.filterNot { it.name == name }.toSet()
        featureFlagUseCase.updateFeatureFlags(updated)
    }

    private suspend fun getFeatureFlagsFromUnleashKeepOverrides(): Map<String, Boolean> {
        val unleashFlags = featureFlagsCacheManager.getUnleashFeatureFlags()
        val currentFlags = featureFlagUseCase.configState.value

        // Keep custom flags + update Unleash flags with fresh data
        val updatedFlags =
            currentFlags.filter { it.type == CUSTOM_TYPE } +
                    unleashFlags.map { unleashFlag ->
                        val existing = currentFlags.find { it.name == unleashFlag.name }
                        FeatureFlagsUseCase.FeatureFlagEntity(
                            name = unleashFlag.name,
                            project = unleashFlag.project,
                            unleashValue = unleashFlag.enabled,
                            configuratorValue = existing?.configuratorValue
                                ?: unleashFlag.enabled, // Preserve override
                            description = unleashFlag.description,
                            type = unleashFlag.type,
                            strategiesJson = unleashFlag.strategies.toString(),
                            stale = unleashFlag.stale,
                            impressionData = unleashFlag.impressionData
                        )
                    }

        featureFlagUseCase.updateFeatureFlags(updatedFlags.toSet())
        return updatedFlags.associate { it.name to it.effectiveValue }
    }
}

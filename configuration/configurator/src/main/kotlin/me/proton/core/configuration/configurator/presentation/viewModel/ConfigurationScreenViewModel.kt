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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.configuration.configurator.domain.ConfigurationUseCase
import javax.inject.Inject

@HiltViewModel
class ConfigurationScreenViewModel @Inject constructor(
    private val configurationUseCase: ConfigurationUseCase
) : ViewModel() {

    sealed class Action {
        data object ObserveConfig : Action()
        data object FetchConfig: Action()
        data object SetDefaultConfigFields : Action()
        data class SaveConfig(val isAdvanced: Boolean) : Action()
        data class SetAdvanced(val isAdvanced: Boolean) : Action()
        data class FetchConfigField(val key: String) : Action()
        data class UpdateConfigField(val key: String, val value: Any) : Action()
    }

    data class State(
        val configFieldSet: Set<ConfigurationUseCase.ConfigField>,
        val isAdvanced: Boolean
    )

    private val mutableErrorFlow: MutableSharedFlow<String> = MutableSharedFlow()

    private val isAdvanced: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val errorFlow: SharedFlow<String> = mutableErrorFlow.asSharedFlow()

    val state: StateFlow<State> =
        observeConfig().onStart {
            perform(Action.FetchConfig)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = State(configurationUseCase.configState.value, isAdvanced.value)
        )

    fun perform(action: Action) = runCatching {
        when (action) {
            is Action.SetDefaultConfigFields -> setDefaultConfigFields()
            is Action.ObserveConfig -> observeConfig()
            is Action.FetchConfig -> fetchConfig()
            is Action.SaveConfig -> saveConfig(action.isAdvanced)
            is Action.SetAdvanced -> setAdvanced(action.isAdvanced)
            is Action.FetchConfigField -> fetchConfigField(action.key)
            is Action.UpdateConfigField -> updateConfigField(action.key, action.value)
        }
    }.onFailure {
        mutableErrorFlow.tryEmit(it.message ?: "Unknown message")
    }

    private fun observeConfig(): Flow<State> = combine(
        configurationUseCase.configState,
        isAdvanced
    ) { fieldSet, advanced ->
        val fieldList = if (isAdvanced.value) fieldSet else fieldSet.filter { it.isAdvanced == advanced }
        State(fieldList.toSet(), isAdvanced.value)
    }

    private fun setDefaultConfigFields() = launchCatching {
        configurationUseCase.setDefaultConfigurationFields()
    }

    private fun fetchConfig() = launchCatching {
        configurationUseCase.fetchConfig()
    }

    private fun saveConfig(isAdvanced: Boolean) = launchCatching {
        configurationUseCase.saveConfig(isAdvanced)
    }

    private fun fetchConfigField(key: String) = launchCatching {
        configurationUseCase.fetchConfigField(key)
    }

    private fun updateConfigField(key: String, value: Any) = launchCatching {
        configurationUseCase.updateConfigField(key, value)
    }

    private fun setAdvanced(advancedValue: Boolean) = launchCatching {
        isAdvanced.emit(advancedValue)
    }

    private fun launchCatching(block: suspend () -> Unit) =
        viewModelScope.launch {
            runCatching {
                block()
            }.onFailure {
                mutableErrorFlow.emit(it.message ?: "Unknown error")
            }
        }
}

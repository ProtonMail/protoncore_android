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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.extension.primitiveFieldMap

typealias ConfigFieldMapper<T> = (Map<String, Any?>) -> T

class ConfigurationScreenViewModel<T : Any>(
    private val contentResolverConfigManager: ContentResolverConfigManager,
    private val configFieldMapper: ConfigFieldMapper<T>,
    private val defaultConfig: T
) : ViewModel() {

    private val _configState: MutableStateFlow<T> = MutableStateFlow(defaultConfig)
    val configurationState: StateFlow<T> get() = _configState

    private val _errorEvent = MutableSharedFlow<Throwable>()
    val errorEvent: SharedFlow<Throwable> get() = _errorEvent

    private val _infoEvent = MutableSharedFlow<String>()
    val infoEvent: SharedFlow<String> get() = _infoEvent

    val configFieldMap get() = _configState.value.primitiveFieldMap

    init {
        fetchInitialConfig()
    }

    fun fetchConfigField(fieldName: String, configurationFieldGetter: suspend () -> Any) {
        viewModelScope.launch {
            runCatching {
                _infoEvent.emit("Fetching $fieldName")
                configurationFieldGetter()
            }
                .onFailure {
                    _errorEvent.emit(it)
                }
                .onSuccess { newValue ->
                    updateConfigField(fieldName, newValue)
                }
        }
    }

    fun saveConfiguration(keysToSave: Set<String> = _configState.value.primitiveFieldMap.keys) {
        viewModelScope.launch {
            val mapToInsert = keysToSave.associateWith { _configState.value.primitiveFieldMap[it] }
            runCatching {
                contentResolverConfigManager.insertContentValuesAtPath(
                    mapToInsert,
                    _configState.value::class.java.name
                )
            }.onFailure { _errorEvent.emit(it) }.onSuccess {
                _infoEvent.emit("Configuration Saved")
            }
        }
    }

    fun setDefaultConfigurationFields(preservedFields: Set<String> = configFieldMap.keys) {
        val map = preservedFields.associateWith { configFieldMap[it].toString() }
        _configState.value = configFieldMapper(map)
    }

    fun <R> observeField(key: String, defaultValue: R): StateFlow<R> =
        _configState.map { state ->
            state.primitiveFieldMap[key] as? R ?: defaultValue
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), defaultValue)

    private fun fetchInitialConfig() {
        viewModelScope.launch {
            runCatching {
                val configMap =
                    contentResolverConfigManager.fetchConfigurationDataAtPath(defaultConfig::class.java.name)
                configFieldMapper(configMap ?: emptyMap())
            }.onFailure { _errorEvent.emit(it) }
                .onSuccess { config ->
                    _configState.value = config
                }
        }
    }

    fun updateConfigField(updatedField: String, newValue: Any) {
        _configState.value = _configState.value.withUpdatedField(updatedField, newValue).also {
            println("Updating field: $updatedField, $newValue")
        }
    }

    private fun T.withUpdatedField(updatedField: String, newValue: Any): T =
        configFieldMapper(this.primitiveFieldMap.toMutableMap().apply { this[updatedField] = newValue })
}

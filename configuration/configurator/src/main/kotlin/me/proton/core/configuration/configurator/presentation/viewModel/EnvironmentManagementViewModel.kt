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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.configuration.configurator.domain.ConfigurationUseCase
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.command.jailUnban
import me.proton.core.test.quark.v2.command.systemEnvVariableAsJson
import javax.inject.Inject

@HiltViewModel
class EnvironmentManagementViewModel @Inject constructor(
    private val quarkCommand: QuarkCommand,
    internal val sharedData: SharedData,
    private val configurationUseCase: ConfigurationUseCase
) : ViewModel() {

    private val _isUnbanLoading = MutableStateFlow(false)
    val isUnbanLoading: StateFlow<Boolean> = _isUnbanLoading.asStateFlow()
    private val _isSystemEnvLoading = MutableStateFlow(false)
    val isSystemEnvLoading: StateFlow<Boolean> = _isSystemEnvLoading.asStateFlow()


    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _unbanResponse = MutableStateFlow<String?>(null)
    val unbanResponse: StateFlow<String?> = _unbanResponse.asStateFlow()

    private val _systemEnvResponse = MutableStateFlow<String?>(null)
    val systemEnvResponse: StateFlow<String?> = _systemEnvResponse.asStateFlow()

    val selectedDomain: StateFlow<String> = configurationUseCase.configState.map { set ->
        val hostField = set.first { field -> field.name == "host" }
        hostField.value as String
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue = "")

    fun unban() =
        viewModelScope.launch(Dispatchers.IO) {
            _isUnbanLoading.value = true
            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")

                val response = quarkCommand.jailUnban()
                val responseBody = response.body?.string()
                val responseMessage = response.message
                _unbanResponse.value = responseMessage
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
                _unbanResponse.value = null
            } finally {
                _isUnbanLoading.value = false
            }
        }

    fun systemEnvVariableAsJson(variable: String, value: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _isSystemEnvLoading.value = true
            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")

                val response = quarkCommand.systemEnvVariableAsJson(variable, value)
                val responseBody = response.body?.string()
                val responseMessage = response.message
                _systemEnvResponse.value = responseMessage
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
                _systemEnvResponse.value = null
            } finally {
                _isSystemEnvLoading.value = false
            }
        }
}

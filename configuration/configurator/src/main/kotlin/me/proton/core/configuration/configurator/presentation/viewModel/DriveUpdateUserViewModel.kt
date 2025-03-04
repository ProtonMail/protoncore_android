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
import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.command.populateUserWithData
import me.proton.core.test.quark.v2.command.quotaSetUsedSpace
import me.proton.core.test.quark.v2.command.volumeCreate
import javax.inject.Inject

@HiltViewModel
class DriveUpdateUserViewModel @Inject constructor(
    private val quarkCommand: QuarkCommand,
    internal val sharedData: SharedData,
    configurationUseCase: ConfigurationUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _response = MutableStateFlow<String?>(null)
    val response: StateFlow<String?> = _response.asStateFlow()

    private val _isQuotaLoading = MutableStateFlow(false)
    val isQuotaLoading: StateFlow<Boolean> = _isQuotaLoading.asStateFlow()

    val selectedDomain: StateFlow<String> = configurationUseCase.configState.map { set ->
        val hostField = set.first { field -> field.name == "host" }
        hostField.value as String
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue = "")

    fun drivePopulate(scenario: Int, hasPhotos: Boolean, withDevice: Boolean) =

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")
                val user = User(
                    name = sharedData.lastUsername,
                    password = sharedData.lastPassword,
                    dataSetScenario = scenario.toString()
                )
                val response = quarkCommand.populateUserWithData(user, hasPhotos, withDevice)
                val responseBody = response.body?.string()
                val responseMessage = response.message
                _response.value = responseMessage
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }

    fun driveQuotaSeedUsedSpace(usedSpace: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _isQuotaLoading.value = true
            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")
                val user = getUser()
                if (!sharedData.lastPlan.contains("drive")) {
                    try {
                        quarkCommand.volumeCreate(
                            uid = user.id.toString(),
                            username = user.name,
                            pass = user.password!!
                        )
                    } catch (e: Exception) {
                        _errorState.value = e.localizedMessage
                    } finally {
                        _isQuotaLoading.value = false
                    }
                }
                val response = quarkCommand.quotaSetUsedSpace(user.id, usedSpace, "Drive")
                val responseMessage = response.message

                _response.value = "Drive $usedSpace quota set: $responseMessage"
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
            } finally {
                _isQuotaLoading.value = false
            }
        }
}
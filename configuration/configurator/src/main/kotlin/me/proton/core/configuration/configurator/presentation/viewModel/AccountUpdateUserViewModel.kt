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
import me.proton.core.configuration.configurator.presentation.components.configuration.toSpacedWords
import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.command.expireSession
import me.proton.core.test.quark.v2.command.populateUserWithData
import me.proton.core.test.quark.v2.command.quotaSetUsedSpace
import me.proton.core.test.quark.v2.command.userReset
import javax.inject.Inject

@HiltViewModel
class AccountUpdateUserViewModel @Inject constructor(
    private val quarkCommand: QuarkCommand,
    internal val sharedData: SharedData,
    private val configurationUseCase: ConfigurationUseCase
) : ViewModel() {

    private val _isSessionLoading = MutableStateFlow(false)
    val isSessionLoading: StateFlow<Boolean> = _isSessionLoading.asStateFlow()

    private val _isQuotaLoading = MutableStateFlow(false)
    val isQuotaLoading: StateFlow<Boolean> = _isQuotaLoading.asStateFlow()

    private val _isResetLoading = MutableStateFlow(false)
    val isResetLoading: StateFlow<Boolean> = _isResetLoading.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _response = MutableStateFlow<String?>(null)
    val response: StateFlow<String?> = _response.asStateFlow()

    val selectedDomain: StateFlow<String> = configurationUseCase.configState.map { set ->
        val hostField = set.first { field -> field.name == "host" }
        hostField.value as String
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue = "")

    fun expireSession(shouldExpireRefreshToken: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            _isSessionLoading.value = true
            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")
                val user = getUser()
                val response = quarkCommand.expireSession(user.name, shouldExpireRefreshToken)
                val responseMessage = response.message
                _response.value = responseMessage
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
            } finally {
                _isSessionLoading.value = false
            }
        }

    fun expireRefreshToken(scenario: Int, hasPhotos: Boolean, withDevice: Boolean) =

        viewModelScope.launch(Dispatchers.IO) {
            _isSessionLoading.value = true
            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")
                val user = getUser()
                val response = quarkCommand.expireSession(user.name, true)
                val responseMessage = response.message
                _response.value = responseMessage
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
            } finally {
                _isSessionLoading.value = false
            }
        }

    fun userReset() =
        viewModelScope.launch(Dispatchers.IO) {
            _isResetLoading.value = true
            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")
                val user = getUser()
                val response = quarkCommand.userReset(user.id.toString())
                val responseMessage = response.message
                _response.value = responseMessage
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
            } finally {
                _isResetLoading.value = false
            }
        }

    fun accountQuotaSeedUsedSpace(scenario: Int, usedSpace: Long, product: String) =

        viewModelScope.launch(Dispatchers.IO) {
            _isQuotaLoading.value = true
            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")
                val user = getUser()
                val response = quarkCommand.quotaSetUsedSpace(user.id, usedSpace.toString(), product)
                val responseMessage = response.message
                _response.value = responseMessage
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
            } finally {
                _isQuotaLoading.value = false
            }
        }
}
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
import kotlinx.coroutines.withContext
import me.proton.core.configuration.configurator.domain.ConfigurationUseCase
import me.proton.core.configuration.configurator.quark.entity.User
import me.proton.core.configuration.configurator.quark.entity.getAllUsers
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.command.userDelete
import javax.inject.Inject

@HiltViewModel
class UpdateUserViewModel @Inject constructor(
    private val quarkCommand: QuarkCommand,
    internal val sharedData: SharedData,
    private val configurationUseCase: ConfigurationUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _response = MutableStateFlow<String?>(null)
    val response: StateFlow<String?> = _response.asStateFlow()

    private val _userResponse = MutableStateFlow<List<User>>(emptyList())
    private val _userNames = MutableStateFlow<List<String>>(emptyList())
    val userNames: StateFlow<List<String>> = _userNames.asStateFlow()

    val lastUserData get() = sharedData

    val selectedDomain: StateFlow<String> = configurationUseCase.configState.map { set ->
        val hostField = set.first { field -> field.name == "host" }
        hostField.value as String
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue = "")

    fun fetchUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")
                val users = withContext(Dispatchers.IO) {
                    quarkCommand.getAllUsers()
                }
                sharedData.usersList = users
                _userResponse.value = users
                _userNames.value = users.map { it.name }
                _response.value = null
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
                _userResponse.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser() {
        val user = _userResponse.value.find { it.name == lastUserData.lastUsername }
        if (user != null) {
            deleteUser(user.id)
        } else {
            deleteUser(lastUserData.lastUserId)
        }
    }

    private fun deleteUser(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")
                    val response = quarkCommand.userDelete(id.toInt())
                    val responseBody = response.message
                    _response.value = responseBody
                }
                sharedData.clean()
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
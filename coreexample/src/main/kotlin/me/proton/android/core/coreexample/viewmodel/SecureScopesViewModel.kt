/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.android.core.coreexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.api.CoreExampleRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.usecase.scopes.RemoveSecurityScopes
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.onError
import me.proton.core.network.domain.onSuccess
import javax.inject.Inject

@HiltViewModel
class SecureScopesViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val coreExampleRepository: CoreExampleRepository,
    private val removeSecurityScopes: RemoveSecurityScopes,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow("-")

    val state = _state.asStateFlow()

    private suspend fun getPrimaryUserIdOrNull() = accountManager.getPrimaryUserId().firstOrNull()

    fun removeScopes() = viewModelScope.launch {
        _state.emit("removing")
        getPrimaryUserIdOrNull()?.let { userId ->
            removeSecurityScopes(userId)
            refreshScopes(userId)
            _state.emit("removed")
        }
    }

    fun triggerLockedScope() = viewModelScope.launch {
        _state.emit("triggering")
        val userId = getPrimaryUserIdOrNull() ?: return@launch
        coreExampleRepository.triggerConfirmPasswordLockedScope(userId)
            .onError { _state.emit((it as? ApiResult.Error.Http)?.proton?.code?.toString() ?: "failure") }
            .onSuccess { _state.emit(it.code.toString()) }
        refreshScopes(userId)
    }

    fun triggerPasswordScope() = viewModelScope.launch {
        _state.emit("triggering")
        val userId = getPrimaryUserIdOrNull() ?: return@launch
        coreExampleRepository.triggerConfirmPasswordPasswordScope(userId)
            .onError { _state.emit((it as? ApiResult.Error.Http)?.proton?.code?.toString() ?: "failure") }
            .onSuccess { _state.emit(it.code.toString()) }
        refreshScopes(userId)
    }

    private suspend fun refreshScopes(userId: UserId) {
        sessionManager.getSessionId(userId)?.let { sessionId ->
            sessionManager.refreshScopes(sessionId)
        }
    }
}

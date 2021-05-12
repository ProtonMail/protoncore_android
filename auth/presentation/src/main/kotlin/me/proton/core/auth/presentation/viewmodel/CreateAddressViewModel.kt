/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.presentation.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.SetupInternalAddress
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.SetupUsername
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.firstInternalOrNull

class CreateAddressViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val userManager: UserManager,
    private val setupUsername: SetupUsername,
    private val setupPrimaryKeys: SetupPrimaryKeys,
    private val setupInternalAddress: SetupInternalAddress,
    private val unlockUserPrimaryKey: UnlockUserPrimaryKey
) : ProtonViewModel() {

    private val _state = MutableStateFlow<State>(State.Idle)

    val state = _state.asStateFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        sealed class Success : State() {
            data class UserUnLocked(val userId: UserId) : Success()
        }

        sealed class Error : State() {
            data class Message(val message: String?) : Error()
            data class CannotUnlockPrimaryKey(val error: UserManager.UnlockResult.Error) : Error()
        }
    }

    fun upgradeAccount(
        userId: UserId,
        password: EncryptedString,
        username: String,
        domain: String
    ) = flow {
        emit(State.Processing)

        setupUsername.invoke(userId, username)

        val user = userManager.getUser(userId, refresh = true)
        val hasKeys = user.keys.isNotEmpty()

        val addresses = userManager.getAddresses(userId, refresh = true)
        val hasInternalAddressKey = addresses.firstInternalOrNull()?.keys?.isNotEmpty() ?: false

        when {
            // directly set Internal (only those Accounts support addresses). upgradeAccount is only valid for Internal
            !hasKeys -> setupPrimaryKeys(userId, password, AccountType.Internal)
            !hasInternalAddressKey -> setupInternalAddress(userId, password, domain)
            else -> unlockUserPrimaryKey(userId, password)
        }.let {
            emit(it)
        }
    }.catch { error ->
        _state.tryEmit(State.Error.Message(error.message))
    }.onEach {
        _state.tryEmit(it)
    }.launchIn(viewModelScope)

    private suspend fun setupPrimaryKeys(
        userId: UserId,
        password: EncryptedString,
        requiredAccountType: AccountType
    ): State {
        setupPrimaryKeys.invoke(userId, password, requiredAccountType)
        accountWorkflow.handleCreateAddressSuccess(userId)
        return unlockUserPrimaryKey(userId, password)
    }

    private suspend fun setupInternalAddress(
        userId: UserId,
        password: EncryptedString,
        domain: String
    ): State {
        val result = unlockUserPrimaryKey.invoke(userId, password)
        return if (result is UserManager.UnlockResult.Success) {
            setupInternalAddress.invoke(userId, domain)
            accountWorkflow.handleCreateAddressSuccess(userId)
            accountWorkflow.handleAccountReady(userId)
            State.Success.UserUnLocked(userId)
        } else {
            accountWorkflow.handleUnlockFailed(userId)
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }

    private suspend fun unlockUserPrimaryKey(
        userId: UserId,
        password: EncryptedString
    ): State {
        val result = unlockUserPrimaryKey.invoke(userId, password)
        return if (result == UserManager.UnlockResult.Success) {
            accountWorkflow.handleAccountReady(userId)
            State.Success.UserUnLocked(userId)
        } else {
            accountWorkflow.handleUnlockFailed(userId)
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }
}

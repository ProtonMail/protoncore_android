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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.domain.usecase.SetupAccountCheck
import me.proton.core.auth.domain.usecase.SetupOriginalAddress
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.UserManager
import me.proton.core.account.domain.entity.AccountType
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

class LoginViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performLogin: PerformLogin,
    private val unlockUserPrimaryKey: UnlockUserPrimaryKey,
    private val setupAccountCheck: SetupAccountCheck,
    private val setupPrimaryKeys: SetupPrimaryKeys,
    private val setupOriginalAddress: SetupOriginalAddress,
    private val keyStoreCrypto: KeyStoreCrypto
) : ProtonViewModel(), ViewStateStoreScope {

    val loginState = ViewStateStore<State>().lock

    sealed class State {
        object Processing : State()
        sealed class Success : State() {
            data class UserUnLocked(val userId: UserId) : Success()
        }

        sealed class Need : State() {
            data class ChangePassword(val userId: UserId) : Need()
            data class SecondFactor(val userId: UserId) : Need()
            data class TwoPassMode(val userId: UserId) : Need()
            data class ChooseUsername(val userId: UserId) : Need()
        }

        sealed class Error : State() {
            data class CannotUnlockPrimaryKey(val error: UserManager.UnlockResult.Error) : Error()
            data class Message(val message: String?) : Error()
        }
    }

    fun startLoginWorkflow(
        username: String,
        password: String,
        requiredAccountType: AccountType
    ) = flow {
        emit(State.Processing)

        val encryptedPassword = password.encryptWith(keyStoreCrypto)

        val sessionInfo = performLogin.invoke(username, encryptedPassword)
        val userId = sessionInfo.userId

        // Storing the session is mandatory for executing subsequent requests.
        handleSessionInfo(requiredAccountType, sessionInfo, encryptedPassword)

        // If SecondFactorNeeded, we cannot proceed without.
        if (sessionInfo.isSecondFactorNeeded) {
            emit(State.Need.SecondFactor(userId))
            return@flow
        }

        // Check if setup keys is needed and if it can be done directly.
        when (setupAccountCheck.invoke(userId, sessionInfo.isTwoPassModeNeeded, requiredAccountType)) {
            is SetupAccountCheck.Result.TwoPassNeeded -> twoPassMode(userId)
            is SetupAccountCheck.Result.ChangePasswordNeeded -> changePassword(userId)
            is SetupAccountCheck.Result.NoSetupNeeded -> unlockUserPrimaryKey(userId, encryptedPassword)
            is SetupAccountCheck.Result.SetupPrimaryKeysNeeded -> setupPrimaryKeys(userId, encryptedPassword)
            is SetupAccountCheck.Result.SetupOriginalAddressNeeded -> setupOriginalAddress(userId, encryptedPassword)
            is SetupAccountCheck.Result.ChooseUsernameNeeded -> chooseUsername(userId, encryptedPassword)
        }.let {
            emit(it)
        }
    }.catch { error ->
        loginState.post(State.Error.Message(error.message))
    }.onEach { state ->
        loginState.post(state)
    }.launchIn(viewModelScope)

    private suspend fun twoPassMode(
        userId: UserId,
    ): State {
        accountWorkflow.handleTwoPassModeNeeded(userId)
        return State.Need.TwoPassMode(userId)
    }

    private suspend fun changePassword(
        userId: UserId,
    ): State {
        accountWorkflow.handleChangePasswordNeeded(userId)
        return State.Need.ChangePassword(userId)
    }

    private suspend fun unlockUserPrimaryKey(
        userId: UserId,
        password: EncryptedString
    ): State {
        val result = unlockUserPrimaryKey.invoke(userId, password)
        return if (result is UserManager.UnlockResult.Success) {
            accountWorkflow.handleAccountReady(userId)
            State.Success.UserUnLocked(userId)
        } else {
            accountWorkflow.handleUnlockFailed(userId)
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }

    private suspend fun setupPrimaryKeys(
        userId: UserId,
        password: EncryptedString
    ): State {
        setupPrimaryKeys.invoke(userId, password)
        return unlockUserPrimaryKey(userId, password)
    }

    private suspend fun setupOriginalAddress(
        userId: UserId,
        password: EncryptedString
    ): State {
        val result = unlockUserPrimaryKey.invoke(userId, password)
        return if (result is UserManager.UnlockResult.Success) {
            setupOriginalAddress.invoke(userId)
            accountWorkflow.handleAccountReady(userId)
            State.Success.UserUnLocked(userId)
        } else {
            accountWorkflow.handleUnlockFailed(userId)
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }

    private suspend fun chooseUsername(
        userId: UserId,
        password: EncryptedString
    ): State {
        val result = unlockUserPrimaryKey.invoke(userId, password)
        return if (result is UserManager.UnlockResult.Success) {
            accountWorkflow.handleCreateAddressNeeded(userId)
            State.Need.ChooseUsername(userId)
        } else {
            accountWorkflow.handleUnlockFailed(userId)
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }

    private suspend fun handleSessionInfo(
        requiredAccountType: AccountType,
        sessionInfo: SessionInfo,
        password: EncryptedString
    ) {
        val sessionState = if (sessionInfo.isSecondFactorNeeded)
            SessionState.SecondFactorNeeded
        else
            SessionState.Authenticated

        val account = Account(
            username = sessionInfo.username,
            userId = sessionInfo.userId,
            email = sessionInfo.username.takeIf { it.contains('@') },
            sessionId = sessionInfo.sessionId,
            state = AccountState.NotReady,
            sessionState = sessionState,
            details = AccountDetails(
                session = SessionDetails(
                    initialEventId = sessionInfo.eventId,
                    requiredAccountType = requiredAccountType,
                    secondFactorEnabled = sessionInfo.isSecondFactorNeeded,
                    twoPassModeEnabled = sessionInfo.isTwoPassModeNeeded,
                    password = password
                ),
                humanVerification = null
            )
        )
        val session = Session(
            sessionId = sessionInfo.sessionId,
            accessToken = sessionInfo.accessToken,
            refreshToken = sessionInfo.refreshToken,
            scopes = sessionInfo.scopes
        )
        accountWorkflow.handleSession(account, session)
    }
}

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

import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.domain.usecase.SetupAccountCheck
import me.proton.core.auth.domain.usecase.SetupInternalAddress
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.HumanVerificationOrchestrator
import me.proton.core.network.domain.session.Session
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountWorkflow: AccountWorkflowHandler,
    private val performLogin: PerformLogin,
    private val unlockUserPrimaryKey: UnlockUserPrimaryKey,
    private val setupAccountCheck: SetupAccountCheck,
    private val setupPrimaryKeys: SetupPrimaryKeys,
    private val setupInternalAddress: SetupInternalAddress,
    private val keyStoreCrypto: KeyStoreCrypto,
    humanVerificationManager: HumanVerificationManager,
    humanVerificationOrchestrator: HumanVerificationOrchestrator
) : AuthViewModel(humanVerificationManager, humanVerificationOrchestrator) {

    private val _state = MutableStateFlow<State>(State.Idle)

    val state = _state.asStateFlow()

    sealed class State {
        object Idle : State()
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
            data class UserCheckError(val error: SetupAccountCheck.UserCheckResult.Error) : Error()
            data class CannotUnlockPrimaryKey(val error: UserManager.UnlockResult.Error) : Error()
            data class Message(val message: String?) : Error()
        }
    }

    override val recoveryEmailAddress: String?
        get() = null

    fun stopLoginWorkflow(): Job = viewModelScope.launch {
        savedStateHandle.get<String>(STATE_USER_ID)?.let { accountWorkflow.handleAccountDisabled(UserId(it)) }
    }

    fun observeHumanVerification(context: ComponentActivity) = handleHumanVerificationState(context)

    fun startLoginWorkflow(
        username: String,
        password: String,
        requiredAccountType: AccountType
    ) = flow {
        emit(State.Processing)

        val encryptedPassword = password.encryptWith(keyStoreCrypto)

        val sessionInfo = performLogin.invoke(username, encryptedPassword)
        val userId = sessionInfo.userId
        savedStateHandle.set(STATE_USER_ID, userId.id)

        // Storing the session is mandatory for executing subsequent requests.
        handleSessionInfo(requiredAccountType, sessionInfo, encryptedPassword)

        // If SecondFactorNeeded, we cannot proceed without.
        if (sessionInfo.isSecondFactorNeeded) {
            emit(State.Need.SecondFactor(userId))
            return@flow
        }

        // Check if setup keys is needed and if it can be done directly.
        when (val result = setupAccountCheck.invoke(userId, sessionInfo.isTwoPassModeNeeded, requiredAccountType)) {
            is SetupAccountCheck.Result.UserCheckError -> checkFailed(userId, result.error)
            is SetupAccountCheck.Result.TwoPassNeeded -> twoPassMode(userId)
            is SetupAccountCheck.Result.ChangePasswordNeeded -> changePassword(userId)
            is SetupAccountCheck.Result.NoSetupNeeded -> unlockUserPrimaryKey(userId, encryptedPassword)
            is SetupAccountCheck.Result.SetupPrimaryKeysNeeded -> setupPrimaryKeys(
                userId,
                encryptedPassword,
                requiredAccountType
            )
            is SetupAccountCheck.Result.SetupInternalAddressNeeded -> setupInternalAddress(userId, encryptedPassword)
            is SetupAccountCheck.Result.ChooseUsernameNeeded -> chooseUsername(userId)
        }.let {
            emit(it)
        }
    }.catch { error ->
        _state.tryEmit(State.Error.Message(error.message))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)

    private suspend fun checkFailed(
        userId: UserId,
        error: SetupAccountCheck.UserCheckResult.Error
    ): State {
        accountWorkflow.handleAccountDisabled(userId)
        return State.Error.UserCheckError(error)
    }

    private suspend fun twoPassMode(
        userId: UserId,
    ): State {
        accountWorkflow.handleTwoPassModeNeeded(userId)
        return State.Need.TwoPassMode(userId)
    }

    private suspend fun changePassword(
        userId: UserId,
    ): State {
        accountWorkflow.handleAccountDisabled(userId)
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
        password: EncryptedString,
        requiredAccountType: AccountType
    ): State {
        setupPrimaryKeys.invoke(userId, password, requiredAccountType)
        return unlockUserPrimaryKey(userId, password)
    }

    private suspend fun setupInternalAddress(
        userId: UserId,
        password: EncryptedString
    ): State {
        val result = unlockUserPrimaryKey.invoke(userId, password)
        return if (result is UserManager.UnlockResult.Success) {
            setupInternalAddress.invoke(userId)
            accountWorkflow.handleAccountReady(userId)
            State.Success.UserUnLocked(userId)
        } else {
            accountWorkflow.handleUnlockFailed(userId)
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }

    private suspend fun chooseUsername(
        userId: UserId
    ): State {
        accountWorkflow.handleCreateAddressNeeded(userId)
        return State.Need.ChooseUsername(userId)
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
                )
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

    companion object {
        const val STATE_USER_ID = "userId"
    }
}

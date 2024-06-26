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

package me.proton.core.usersettings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import me.proton.core.accountrecovery.domain.IsAccountRecoveryResetEnabled
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecoverySelfInitiated
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.AccountRecoveryResetTotal
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.UserRecovery
import me.proton.core.usersettings.domain.usecase.IsSessionAccountRecoveryEnabled
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import me.proton.core.usersettings.domain.usecase.PerformUpdateLoginPassword
import me.proton.core.usersettings.domain.usecase.PerformUpdateUserPassword
import me.proton.core.usersettings.domain.usecase.PerformResetUserPassword
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import javax.inject.Inject

@HiltViewModel
class PasswordManagementViewModel @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto,
    private val observeUserRecovery: ObserveUserRecovery,
    private val observeUserSettings: ObserveUserSettings,
    private val observeUserRecoverySelfInitiated: ObserveUserRecoverySelfInitiated,
    private val performUpdateLoginPassword: PerformUpdateLoginPassword,
    private val performUpdateUserPassword: PerformUpdateUserPassword,
    private val performResetPassword: PerformResetUserPassword,
    private val isAccountRecoveryResetEnabled: IsAccountRecoveryResetEnabled,
    override val observabilityManager: ObservabilityManager,
    private val isSessionAccountRecoveryEnabled: IsSessionAccountRecoveryEnabled,
    private val product: Product
) : ProtonViewModel(), ObservabilityContext {

    private var pendingUpdate: Action.UpdatePassword? = null

    private val currentAction = MutableStateFlow<Action?>(null)
    private val currentUserId = MutableStateFlow<UserId?>(null)

    private val currentUserRecovery = currentUserId.filterNotNull().flatMapLatest {
        observeUserRecovery(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val currentUserSettings = currentUserId.filterNotNull().flatMapLatest {
        observeUserSettings(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val currentSelfInitiated = currentUserId.filterNotNull().flatMapLatest {
        observeUserRecoverySelfInitiated(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private fun observeState(userId: UserId): Flow<State> = combine(
        currentUserRecovery,
        currentUserSettings,
        currentSelfInitiated
    ) { _, _, _ ->
        State.ChangePassword(
            userId = userId,
            loginPasswordAvailable = true,
            mailboxPasswordAvailable = isMailboxPassword && product != Product.Vpn,
            recoveryResetAvailable = recoveryResetAvailable(userId),
            recoveryResetEnabled = isRecoveryResetEnabled,
            currentLoginPasswordNeeded = !isRecoveryResetEnabled || !isRecoveryInsecure || !isSelfInitiated,
            twoFactorEnabled = isTwoFactorEnabled,
            userRecoveryState = userRecovery?.state?.enum
        )
    }.onStart {
        currentUserId.emit(userId)
    }

    private suspend fun recoveryResetAvailable(userId: UserId): Boolean =
        isRecoveryResetEnabled && isRecoveryAvailable && isSessionAccountRecoveryEnabled(userId)

    private suspend fun updatePassword(action: Action.UpdatePassword): Flow<State> = when {
        pendingUpdate == null && userSettings?.twoFA?.enabled == true -> {
            pendingUpdate = action
            flowOf(State.TwoFactorNeeded)
        }

        isRecoveryResetEnabled && isRecoveryInsecure && isSelfInitiated -> {
            resetPassword(action)
        }

        else -> when (action.type) {
            PasswordType.Login -> when (isMailboxPassword) {
                true -> updateLoginPassword(action)
                false -> updateBothPassword(action)
            }
            PasswordType.Mailbox -> updateMailboxPassword(action)
            PasswordType.Both -> updateBothPassword(action)
        }
    }

    private fun updateLoginPassword(action: Action.UpdatePassword): Flow<State> = flow {
        emit(State.UpdatingPassword)
        val encryptedPassword = action.password.encrypt(keyStoreCrypto)
        val encryptedNewPassword = action.newPassword.encrypt(keyStoreCrypto)
        performUpdateLoginPassword(
            userId = action.userId,
            password = encryptedPassword,
            newPassword = encryptedNewPassword,
            secondFactorProof = action.secondFactorProof
        )
        emit(State.Success)
    }

    private fun updateMailboxPassword(action: Action.UpdatePassword): Flow<State> = flow {
        emit(State.UpdatingPassword)
        val encryptedLoginPassword = action.password.encrypt(keyStoreCrypto)
        val encryptedNewMailboxPassword = action.newPassword.encrypt(keyStoreCrypto)
        performUpdateUserPassword.invoke(
            twoPasswordMode = true,
            userId = action.userId,
            loginPassword = encryptedLoginPassword,
            newPassword = encryptedNewMailboxPassword,
            secondFactorProof = action.secondFactorProof
        )
        emit(State.Success)
    }

    private fun updateBothPassword(action: Action.UpdatePassword): Flow<State> = flow {
        emit(State.UpdatingPassword)
        val encryptedLoginPassword = action.password.encrypt(keyStoreCrypto)
        val encryptedNewMailboxPassword = action.newPassword.encrypt(keyStoreCrypto)
        performUpdateUserPassword.invoke(
            twoPasswordMode = false,
            userId = action.userId,
            loginPassword = encryptedLoginPassword,
            newPassword = encryptedNewMailboxPassword,
            secondFactorProof = action.secondFactorProof
        )
        emit(State.Success)
    }

    private suspend fun resetPassword(action: Action.UpdatePassword): Flow<State> = flowWithResultContext {
        it.onResultEnqueueObservability("account_recovery.reset") { AccountRecoveryResetTotal(this) }

        send(State.UpdatingPassword)
        val encryptedNewPassword = action.newPassword.encrypt(keyStoreCrypto)
        performResetPassword(
            userId = action.userId,
            newPassword = encryptedNewPassword,
        )
        send(State.Success)
    }

    private suspend fun setTwoFactor(action: Action.SetTwoFactor): Flow<State> {
        val passwordAction = requireNotNull(pendingUpdate?.copy(secondFactorProof = action.secondFactorProof))
        return updatePassword(passwordAction)
    }

    private fun cancelTwoFactor(action: Action.CancelTwoFactor): Flow<State> {
        pendingUpdate = null
        return observeState(action.userId)
    }

    private val userRecovery get() = currentUserRecovery.value
    private val userSettings get() = currentUserSettings.value
    private val isSelfInitiated get() = currentSelfInitiated.value
    private val isMailboxPassword get() = userSettings?.password?.mode == 2
    private val isTwoFactorEnabled get() = userSettings?.twoFA?.enabled ?: false
    private val isRecoveryResetEnabled get() = isAccountRecoveryResetEnabled(currentUserId.value)
    private val isRecoveryInsecure get() = userRecovery?.state?.enum == UserRecovery.State.Insecure
    private val isRecoveryAvailable
        get() = when (userRecovery?.state?.enum) {
            null -> true
            UserRecovery.State.None -> true
            UserRecovery.State.Expired -> true
            else -> false
        }

    fun perform(action: Action) = currentAction.tryEmit(action)


    val state = currentAction.flatMapLatest { action ->
        when (action) {
            is Action.ObserveState -> observeState(action.userId)
            is Action.UpdatePassword -> updatePassword(action)
            is Action.SetTwoFactor -> setTwoFactor(action)
            is Action.CancelTwoFactor -> cancelTwoFactor(action)
            null -> flowOf(State.Idle)
        }
    }.retryWhen { cause, _ ->
        pendingUpdate = null
        emit(State.Error(cause))
        perform(Action.ObserveState(requireNotNull(currentUserId.value)))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = State.Idle
    )

    sealed class State {
        data object Idle : State()
        data class ChangePassword(
            val userId: UserId,
            val loginPasswordAvailable: Boolean,
            val mailboxPasswordAvailable: Boolean,
            val recoveryResetAvailable: Boolean,
            val recoveryResetEnabled: Boolean,
            val currentLoginPasswordNeeded: Boolean,
            val twoFactorEnabled: Boolean,
            val userRecoveryState: UserRecovery.State? = null
        ) : State()

        data object TwoFactorNeeded : State()
        data object UpdatingPassword : State()
        data object Success : State()
        data class Error(val error: Throwable) : State()
    }

    sealed class Action(open val userId: UserId) {
        data class ObserveState(
            override val userId: UserId
        ) : Action(userId)

        data class UpdatePassword(
            override val userId: UserId,
            val type: PasswordType,
            val password: String,
            val newPassword: String,
            val secondFactorProof: SecondFactorProof? = null
        ) : Action(userId)

        data class SetTwoFactor(
            override val userId: UserId,
            val secondFactorProof: SecondFactorProof?
        ) : Action(userId)

        data class CancelTwoFactor(
            override val userId: UserId,
        ) : Action(userId)
    }

    enum class PasswordType { Both, Login, Mailbox }
}

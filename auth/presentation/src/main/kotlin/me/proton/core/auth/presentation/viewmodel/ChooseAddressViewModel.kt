/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.LogTag
import me.proton.core.auth.domain.entity.EncryptedAuthSecret
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup.Result
import me.proton.core.auth.domain.usecase.primaryKeyExists
import me.proton.core.auth.presentation.observability.toUnlockUserStatus
import me.proton.core.auth.presentation.observability.toUserCheckStatus
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.LoginEaToIaFetchDomainsTotal
import me.proton.core.observability.domain.metrics.LoginEaToIaUnlockUserTotalV1
import me.proton.core.observability.domain.metrics.LoginEaToIaUserCheckTotalV1
import me.proton.core.observability.domain.metrics.LoginEaToIaUsernameAvailabilityTotal
import me.proton.core.observability.domain.metrics.LoginScreenViewTotal
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.emailSplit
import me.proton.core.usersettings.domain.usecase.SetupUsername
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import me.proton.core.util.kotlin.retryOnceWhen
import javax.inject.Inject

@HiltViewModel
class ChooseAddressViewModel @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val accountAvailability: AccountAvailability,
    override val observabilityManager: ObservabilityManager,
    private val postLoginAccountSetup: PostLoginAccountSetup,
    private val setupUsername: SetupUsername
) : ProtonViewModel(), ObservabilityContext {

    private val mainState = MutableStateFlow<State>(State.Processing)
    val state = mainState.asStateFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        sealed class Data : State() {
            data class Domains(val domains: List<Domain>) : Data()
            data class UsernameOption(val username: String) : Data()
            data class UsernameAlreadySet(val username: String) : Data()
        }

        sealed class Error : State() {
            data class Start(val error: Throwable) : Error()
            data class SetUsername(val error: Throwable) : Error()
        }

        data class AccountSetupResult(val result: Result) : State()
        object Finished : State()
    }

    fun stopWorkflow(
        userId: UserId
    ): Job = viewModelScope.launch {
        accountWorkflow.handleCreateAddressFailed(userId)
        mainState.emit(State.Finished)
    }

    fun startWorkFlow(userId: UserId) = viewModelScope.launchWithResultContext {
        onResultEnqueueObservability("getAvailableDomains") { LoginEaToIaFetchDomainsTotal(this) }
        onResultEnqueueObservability("checkUsernameAvailable") { LoginEaToIaUsernameAvailabilityTotal(this) }

        flow {
            emit(State.Processing)
            val domains = accountAvailability.getDomains(userId = userId)
            emit(State.Data.Domains(domains))
            emit(checkAccount(userId, domains))
        }.catch { error ->
            emit(State.Error.Start(error))
        }.collect {
            mainState.tryEmit(it)
        }
    }

    fun setUsername(
        userId: UserId,
        username: String,
        authSecret: EncryptedAuthSecret,
        domain: String,
        isTwoPassModeNeeded: Boolean
    ) = viewModelScope.launchWithResultContext {
        onResultEnqueueObservability("unlockUserPrimaryKey") { LoginEaToIaUnlockUserTotalV1(this.toUnlockUserStatus()) }
        onResultEnqueueObservability("defaultUserCheck") { LoginEaToIaUserCheckTotalV1(this.toUserCheckStatus())}

        flow {
            emit(State.Processing)
            setupUsername(userId, username)
            emit(postLoginSetup(userId, authSecret, domain, isTwoPassModeNeeded))
        }.retryOnceWhen(Throwable::primaryKeyExists) {
            CoreLogger.e(LogTag.FLOW_ERROR_RETRY, it, "Retrying to upgrade an account")
        }.catch { error ->
            emit(State.Error.SetUsername(error))
        }.collect {
            mainState.tryEmit(it)
        }
    }

    private suspend fun checkAccount(userId: UserId, domains: List<Domain>): State {
        val user = accountAvailability.getUser(userId, refresh = true)
        return when (user.name) {
            null -> checkUsernameOption(userId, user, domains)
            else -> State.Data.UsernameAlreadySet(requireNotNull(user.name))
        }
    }

    private suspend fun checkUsernameOption(userId: UserId, user: User, domains: List<Domain>): State {
        return when (val username = user.emailSplit?.username) {
            null -> State.Idle
            else -> checkUsername(userId, username, domains.first())
        }
    }

    private suspend fun checkUsername(userId: UserId, username: String, domain: String): State {
        return runCatching {
            accountAvailability.checkUsernameAuthenticated(
                userId = userId,
                username = "$username@$domain"
            )
        }.fold(
            onSuccess = { State.Data.UsernameOption(username) },
            onFailure = { State.Idle }
        )
    }

    private suspend fun postLoginSetup(
        userId: UserId,
        authSecret: EncryptedAuthSecret,
        domain: String,
        isTwoPassModeNeeded: Boolean
    ): State.AccountSetupResult {
        val result = postLoginAccountSetup(
            userId = userId,
            encryptedAuthSecret = authSecret,
            requiredAccountType = AccountType.Internal,
            isSecondFactorNeeded = false,
            isTwoPassModeNeeded = isTwoPassModeNeeded,
            temporaryPassword = false,
            onSetupSuccess = { accountWorkflow.handleCreateAddressSuccess(userId) },
            internalAddressDomain = domain
        )
        return State.AccountSetupResult(result)
    }

    internal fun onScreenView(screenId: LoginScreenViewTotal.ScreenId) {
        observabilityManager.enqueue(LoginScreenViewTotal(screenId))
    }
}

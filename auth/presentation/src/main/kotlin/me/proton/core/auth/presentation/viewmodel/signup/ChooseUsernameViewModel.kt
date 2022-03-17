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

package me.proton.core.auth.presentation.viewmodel.signup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.UsernameDomainAvailability
import me.proton.core.auth.domain.usecase.signup.SignupChallengeConfig
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToEmailDestination
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.Domain
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class ChooseUsernameViewModel @Inject constructor(
    private val usernameDomainAvailability: UsernameDomainAvailability,
    private val sendVerificationCodeToEmailDestination: SendVerificationCodeToEmailDestination,
    private val challengeManager: ChallengeManager,
    private val clientIdProvider: ClientIdProvider,
    private val challengeConfig: SignupChallengeConfig
) : ProtonViewModel() {

    private val _state = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)
    private val _selectedAccountTypeState = MutableSharedFlow<AccountTypeState>(replay = 1, extraBufferCapacity = 3)
    private lateinit var clientAppRequiredAccountType: AccountType

    lateinit var currentAccountType: AccountType
        private set

    var domains: List<Domain>? = null
        private set

    val state = _state.onSubscription { emitAll(fetchDomains()) }
    val selectedAccountTypeState = _selectedAccountTypeState.asSharedFlow()

    sealed class AccountTypeState {
        data class NewAccountType(val type: AccountType) : AccountTypeState()
    }

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class UsernameAvailable(val username: String, val domain: String) : State()
        data class ExternalAccountTokenSent(val email: String) : State()
        data class AvailableDomains(val domains: List<Domain>, val currentAccountType: AccountType) : State()
        sealed class Error : State() {
            object DomainsNotAvailable : Error()
            object UsernameNotAvailable : Error()
            data class Message(val error: Throwable) : Error()
        }
    }

    sealed class EmailVerificationCodeState {
        object Idle : EmailVerificationCodeState()
        object Processing : EmailVerificationCodeState()
        object Success : EmailVerificationCodeState()
        sealed class Error : EmailVerificationCodeState() {
            data class Message(val message: String?) : Error()
        }
    }

    private suspend fun checkUsernameForAccountType(username: String, domain: String?): State {
        viewModelScope.launch {
            val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
            challengeManager.startNewFlow(clientId, challengeConfig.flowName)
        }
        return when (requireCurrentAccountType()) {
            AccountType.Username,
            AccountType.Internal -> {
                requireNotNull(domain) { "For AccountType Internal a domain must be supplied." }
                if (usernameDomainAvailability.isUsernameAvailable(username)) {
                    // if selected Internal account, domain must be set along with username
                    State.UsernameAvailable(username, domain)
                } else {
                    State.Error.UsernameNotAvailable
                }
            }
            AccountType.External -> {
                // for External accounts, the email is the username
                sendVerificationCodeToEmailDestination(emailAddress = username)
                State.ExternalAccountTokenSent(username)
            }
        }.exhaustive
    }

    private fun requireCurrentAccountType(): AccountType {
        require(this::currentAccountType.isInitialized) {
            "currentAccountType is not set. Call setClientAppRequiredAccountType first."
        }
        return currentAccountType
    }

    private fun fetchDomains() = flow {
        if (domains.isNullOrEmpty()) {
            emit(State.Processing)
            domains = usernameDomainAvailability.getDomains()
        }
        emit(State.AvailableDomains(domains!!, requireCurrentAccountType()))
    }.catch { error ->
        emit(State.Error.Message(error))
    }

    /**
     * This is where the client need to set the [AccountType] it want's to create.
     * Note, some clients can support [AccountType.External] (such as Drive) so the user will be allowed to switch
     * back and forth between these.
     */
    fun setClientAppRequiredAccountType(accountType: AccountType) {
        clientAppRequiredAccountType = accountType
        currentAccountType = clientAppRequiredAccountType
        _selectedAccountTypeState.tryEmit(AccountTypeState.NewAccountType(currentAccountType))
    }

    /**
     * Switches the account type between preferred (what the client has requested as a minimal) and if the user want's
     * to create [AccountType.External].
     */
    fun onUserSwitchAccountType() {
        currentAccountType = when (clientAppRequiredAccountType) {
            AccountType.Username -> {
                when {
                    currentAccountType == AccountType.External -> AccountType.Username
                    clientAppRequiredAccountType.canSwitchToExternal() -> AccountType.External
                    else -> AccountType.Username
                }
            }
            AccountType.Internal -> AccountType.Internal
            AccountType.External -> {
                if (currentAccountType == AccountType.External) {
                    AccountType.Username
                } else {
                    AccountType.External
                }
            }
        }.exhaustive
        _selectedAccountTypeState.tryEmit(AccountTypeState.NewAccountType(currentAccountType))
    }

    /**
     * Checks if the chosen username is available on the API for the new account request.
     */
    fun checkUsername(username: String, domain: String? = null) = flow {
        emit(State.Processing)
        emit(checkUsernameForAccountType(username, domain))
        // Needed to not re-emit the navigation state on fragment recreation
        emit(State.Idle)
    }.catch { error ->
        emit(State.Error.Message(error))
    }.onEach {
        _state.tryEmit(it)
    }.launchIn(viewModelScope)
}

/**
 * Returns if the user can switch to [AccountType.External] from the client required [AccountType].
 */
internal fun AccountType.canSwitchToExternal(): Boolean = when (this) {
    AccountType.Username -> false
    AccountType.External -> true
    AccountType.Internal -> false
}.exhaustive

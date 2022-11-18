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
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.Domain
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class ChooseUsernameViewModel @Inject constructor(
    private val usernameDomainAvailability: UsernameDomainAvailability,
    private val sendVerificationCodeToEmailDestination: SendVerificationCodeToEmailDestination,
    private val challengeManager: ChallengeManager,
    private val challengeConfig: SignupChallengeConfig,
    private var requiredAccountType: AccountType
) : ProtonViewModel() {

    private val _state = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)
    private val _selectedAccountTypeState = MutableSharedFlow<AccountTypeState>(replay = 1, extraBufferCapacity = 3)

    private var currentAccountType: AccountType

    var domains: List<Domain>? = null
        private set

    val state = _state.onSubscription {
        if (requiredAccountType == AccountType.Internal) {
            emitAll(fetchDomains())
        }
    }
    val selectedAccountTypeState = _selectedAccountTypeState.asSharedFlow()

    sealed class AccountTypeState {
        data class NewAccountType(val type: AccountType, val minimalAccountType: AccountType) : AccountTypeState()
    }

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class UsernameAvailable(val username: String, val domain: String, val currentAccountType: AccountType) : State()
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

    init {
        viewModelScope.launch {
            challengeManager.resetFlow(challengeConfig.flowName)
        }
        currentAccountType = requiredAccountType
        _selectedAccountTypeState.tryEmit(AccountTypeState.NewAccountType(currentAccountType, requiredAccountType))
    }

    private suspend fun checkUsernameForAccountType(username: String, domain: String?): State {
        return when (currentAccountType) {
            AccountType.Username,
            AccountType.Internal -> {
                requireNotNull(domain) { "For AccountType Internal a domain must be supplied." }
                val email = "$username@$domain"
                if (usernameDomainAvailability.isUsernameAvailable(email)) {
                    // if selected Internal account, domain must be set along with username
                    State.UsernameAvailable(username, domain, currentAccountType)
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

    private fun fetchDomains() = flow {
        if (domains.isNullOrEmpty()) {
            emit(State.Processing)
            domains = usernameDomainAvailability.getDomains()
        }
        emit(State.AvailableDomains(domains!!, currentAccountType))
    }.catch { error ->
        emit(State.Error.Message(error))
    }

    /**
     * Switches the account type between preferred (what the client has requested as a minimal) and if the user want's
     * to create [AccountType.External].
     */
    fun onUserSwitchAccountType() {
        currentAccountType = when (requiredAccountType) {
            AccountType.Internal -> AccountType.Internal
            AccountType.Username -> AccountType.Username
            AccountType.External -> {
                if (currentAccountType == AccountType.External) {
                    AccountType.Internal
                } else {
                    AccountType.External
                }
            }
        }.exhaustive
        _selectedAccountTypeState.tryEmit(AccountTypeState.NewAccountType(currentAccountType, requiredAccountType))
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

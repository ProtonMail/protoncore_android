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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.retryWhen
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.UsernameDomainAvailability
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToEmailDestination
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.Domain
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class ChooseUsernameViewModel @Inject constructor(
    private val usernameDomainAvailability: UsernameDomainAvailability,
    private val sendVerificationCodeToEmailDestination: SendVerificationCodeToEmailDestination
) : ProtonViewModel() {

    private lateinit var fetchDomainJob: Job

    private val _state = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)
    private val _selectedAccountTypeState = MutableSharedFlow<AccountTypeState>(replay = 1, extraBufferCapacity = 3)
    private lateinit var clientAppRequiredAccountType: AccountType

    lateinit var currentAccountType: AccountType
        private set

    val state = _state.asSharedFlow()
        .onSubscription {
            fetchDomainJob = fetchDomains()
        }.onCompletion {
            fetchDomainJob.cancel()
        }

    val selectedAccountTypeState = _selectedAccountTypeState.asSharedFlow()

    var domains: List<Domain>? = null

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
            data class Message(val message: String?) : Error()
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

    private fun fetchDomains() = flow {
        emit(State.Processing)
        val domains = usernameDomainAvailability.getDomains()
        if (domains.isEmpty()) {
            emit(State.Error.DomainsNotAvailable)
            return@flow
        }
        this@ChooseUsernameViewModel.domains = domains
        emit(State.AvailableDomains(domains, requireCurrentAccountType()))
    }.retryWhen { cause, _ ->
        emit(State.Error.Message(cause.message))
        // Retry every 5 seconds.
        delay(5000)
        true
    }.onEach {
        _state.tryEmit(it)
    }.launchIn(viewModelScope)

    // region public API
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
                    AccountType.Internal
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
    }.catch { error ->
        emit(State.Error.Message(error.message))
    }.onEach {
        _state.tryEmit(it)
    }.launchIn(viewModelScope)
    // endregion

    private suspend fun checkUsernameForAccountType(username: String, domain: String?): State {
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
}

/**
 * Returns if the user can switch to [AccountType.External] from the client required [AccountType].
 */
internal fun AccountType.canSwitchToExternal(): Boolean = when (this) {
    AccountType.Username -> true
    AccountType.External -> true
    AccountType.Internal -> false
}.exhaustive

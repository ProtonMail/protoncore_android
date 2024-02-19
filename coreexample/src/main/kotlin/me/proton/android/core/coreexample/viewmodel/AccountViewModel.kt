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

package me.proton.android.core.coreexample.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountCreateAccountFailed
import me.proton.core.accountmanager.presentation.onAccountCreateAccountNeeded
import me.proton.core.accountmanager.presentation.onAccountCreateAddressFailed
import me.proton.core.accountmanager.presentation.onAccountCreateAddressNeeded
import me.proton.core.accountmanager.presentation.onAccountMigrationNeeded
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeFailed
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeNeeded
import me.proton.core.accountmanager.presentation.onSessionForceLogout
import me.proton.core.accountmanager.presentation.onSessionSecondFactorFailed
import me.proton.core.accountmanager.presentation.onSessionSecondFactorNeeded
import me.proton.core.accountmanager.presentation.onUserAddressKeyCheckFailed
import me.proton.core.accountmanager.presentation.onUserKeyCheckFailed
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.observe
import me.proton.core.auth.presentation.onMissingScopeFailed
import me.proton.core.auth.presentation.onMissingScopeSuccess
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.showToast
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val product: Product,
    private val accountType: AccountType,
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private var authOrchestrator: AuthOrchestrator,
    private val missingScopeListener: MissingScopeListener
) : ViewModel() {

    private val _secureSessionScopes = MutableStateFlow(emptyList<String>())
    private val _state = MutableStateFlow(State.Processing as State)

    sealed class State {
        object Processing : State()
        object LoginNeeded : State()
        data class AccountList(val accounts: List<Account>) : State()
    }

    val secureSessionScopes = _secureSessionScopes.asStateFlow()
    val state = _state.asStateFlow()

    fun register(context: FragmentActivity) {
        authOrchestrator.register(context)

        accountManager.getAccounts()
            .flowWithLifecycle(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
            .onEach { accounts ->
                if (accounts.isEmpty())
                    _state.emit(State.LoginNeeded)
                else
                    _state.emit(State.AccountList(accounts))
            }
            .launchIn(context.lifecycleScope)

        authOrchestrator.setOnAddAccountResult {
            if (it == null) {
                viewModelScope.launch {
                    val accounts = accountManager.getAccounts().first()
                    _state.emit(State.AccountList(accounts))
                }
            }
        }

        with(authOrchestrator) {
            accountManager.observe(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
                .onSessionForceLogout { userManager.lock(it.userId) }
                .onSessionSecondFactorNeeded { startSecondFactorWorkflow(it) }
                .onSessionSecondFactorFailed { signIn(username = it.username) }
                .onAccountTwoPassModeNeeded { startTwoPassModeWorkflow(it) }
                .onAccountCreateAddressNeeded { startChooseAddressWorkflow(it) }
                .onAccountCreateAccountNeeded { startSignupWorkflow(accountType, cancellable = false) }
                .onAccountCreateAccountFailed { accountManager.disableAccount(it.userId) }
                .onAccountTwoPassModeFailed { accountManager.disableAccount(it.userId) }
                .onAccountCreateAddressFailed { accountManager.disableAccount(it.userId) }
                .onAccountMigrationNeeded { context.showToast("MigrationNeeded") }
                .onUserKeyCheckFailed { context.errorToast("InvalidUserKey") }
                .onUserAddressKeyCheckFailed { context.errorToast("InvalidUserAddressKey") }
        }

        accountManager.getPrimaryAccount()
            .combine(accountManager.getSessions()) { account, sessions ->
                sessions.find { it.sessionId == account?.sessionId }
            }
            .flowWithLifecycle(context.lifecycle)
            .map { session ->
                val scopes = session?.scopes ?: emptyList()
                scopes.filter { it == Scope.LOCKED.value || it == Scope.PASSWORD.value }
            }
            .onEach { _secureSessionScopes.emit(it) }
            .launchIn(context.lifecycleScope)

        missingScopeListener.observe(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
            .onMissingScopeSuccess { context.showToast("Success Test") }
            .onMissingScopeFailed { context.showToast("Failed test") }
    }

    suspend fun signOut(userId: UserId) = accountManager.disableAccount(userId)

    suspend fun remove(userId: UserId) = accountManager.removeAccount(userId)

    suspend fun setAsPrimary(userId: UserId) = accountManager.setAsPrimary(userId)

    fun getPrimaryUserId() = accountManager.getPrimaryUserId()

    fun signIn(username: String? = null) =
        authOrchestrator.startLoginWorkflow(accountType, username = username)

    fun add() = authOrchestrator.startAddAccountWorkflow(
        requiredAccountType = accountType,
        creatableAccountType = accountType,
        product = product
    )

    fun onAccountClicked(userId: UserId) {
        viewModelScope.launch {
            val account = accountManager.getAccount(userId).first() ?: return@launch
            when (account.state) {
                AccountState.Ready,
                AccountState.NotReady,
                AccountState.TwoPassModeFailed,
                AccountState.CreateAddressFailed,
                AccountState.UnlockFailed -> accountManager.disableAccount(account.userId)
                AccountState.Disabled -> accountManager.removeAccount(account.userId)
                else -> Unit
            }
        }
    }

    fun onInternalSignUpClicked() {
        viewModelScope.launch {
            authOrchestrator.startSignupWorkflow(creatableAccountType = AccountType.Internal)
        }
    }

    fun onExternalSignUpClicked() {
        viewModelScope.launch {
            authOrchestrator.startSignupWorkflow(creatableAccountType = AccountType.External)
        }
    }

    fun onUsernameSignUpClicked() {
        viewModelScope.launch {
            authOrchestrator.startSignupWorkflow(creatableAccountType = AccountType.Username)
        }
    }
}

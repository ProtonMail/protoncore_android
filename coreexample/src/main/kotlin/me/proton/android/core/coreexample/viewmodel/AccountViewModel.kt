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

import androidx.activity.ComponentActivity
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountCreateAddressFailed
import me.proton.core.accountmanager.presentation.onAccountCreateAddressNeeded
import me.proton.core.accountmanager.presentation.onAccountDisabled
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeFailed
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeNeeded
import me.proton.core.accountmanager.presentation.onSessionHumanVerificationNeeded
import me.proton.core.accountmanager.presentation.onSessionSecondFactorNeeded
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.entity.PlanDetails
import me.proton.core.payment.presentation.onPaymentResult

class AccountViewModel @ViewModelInject constructor(
    private val accountManager: AccountManager,
    private var authOrchestrator: AuthOrchestrator,
    private val paymentsOrchestrator: PaymentsOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(State.Processing as State)

    sealed class State {
        object Processing : State()
        object LoginNeeded : State()
        data class AccountList(val accounts: List<Account>) : State()
    }

    val state = _state.asStateFlow()

    fun register(context: ComponentActivity) {
        authOrchestrator.register(context)
        paymentsOrchestrator.register(context)
        accountManager.getAccounts().onEach { accounts ->
            if (accounts.isEmpty()) _state.tryEmit(State.LoginNeeded)
            _state.tryEmit(State.AccountList(accounts))
        }.launchIn(viewModelScope)
    }

    fun getPrimaryUserId() = accountManager.getPrimaryUserId()

    fun getPrimaryAccount() = accountManager.getPrimaryAccount()

    fun startLoginWorkflow() = authOrchestrator.startLoginWorkflow(AccountType.Internal)

    fun onAccountClicked(userId: UserId) {
        viewModelScope.launch {
            val account = accountManager.getAccount(userId).first() ?: return@launch
            when (account.state) {
                AccountState.Ready,
                AccountState.NotReady,
                AccountState.Disabled,
                AccountState.TwoPassModeFailed,
                AccountState.CreateAddressFailed,
                AccountState.UnlockFailed -> accountManager.disableAccount(account.userId)
                else -> Unit
            }
        }
    }

    fun handleAccountState() {
        with(authOrchestrator) {
            accountManager.observe(viewModelScope)
                .onSessionSecondFactorNeeded { startSecondFactorWorkflow(it) }
                .onAccountTwoPassModeNeeded { startTwoPassModeWorkflow(it) }
                .onAccountCreateAddressNeeded { startChooseAddressWorkflow(it) }
                .onSessionHumanVerificationNeeded { startHumanVerificationWorkflow(it) }
                .onAccountTwoPassModeFailed { accountManager.disableAccount(it.userId) }
                .onAccountCreateAddressFailed { accountManager.disableAccount(it.userId) }
                .onAccountDisabled { accountManager.removeAccount(it.userId) }
        }
    }

    fun onPaySignUpClicked() {
        viewModelScope.launch {
            paymentsOrchestrator.startBillingWorkFlow(
                selectedPlan =
                PlanDetails(
                    "ziWi-ZOb28XR4sCGFCEpqQbd1FITVWYfTfKYUmV_wKKR3GsveN4HZCh9er5dhelYylEp-fhjBbUPDMHGU699fw==",
                    "Proton Plus",
                    SubscriptionCycle.YEARLY
                )
            )
        }
    }

    fun onPayUpgradeClicked() {
        viewModelScope.launch {
            getPrimaryUserId().first()?.let {
                val account = accountManager.getAccount(it).first() ?: return@launch
                with(paymentsOrchestrator) {
                    onPaymentResult { result ->
                        // do something with the payment result
                    }

                    startBillingWorkFlow(
                        sessionId = account.sessionId,
                        selectedPlan = PlanDetails(
                            "ziWi-ZOb28XR4sCGFCEpqQbd1FITVWYfTfKYUmV_wKKR3GsveN4HZCh9er5dhelYylEp-fhjBbUPDMHGU699fw==",
                            "Proton Plus",
                            SubscriptionCycle.YEARLY
                        ),
                        codes = null
                    )
                }
            }
        }
    }
}

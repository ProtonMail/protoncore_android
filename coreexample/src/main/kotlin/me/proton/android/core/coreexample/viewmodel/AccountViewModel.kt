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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import me.proton.core.accountmanager.presentation.disableInitialNotReadyAccounts
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountCreateAddressFailed
import me.proton.core.accountmanager.presentation.onAccountCreateAddressNeeded
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeFailed
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeNeeded
import me.proton.core.accountmanager.presentation.onSessionSecondFactorNeeded
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.HumanVerificationOrchestrator
import me.proton.core.humanverification.presentation.observe
import me.proton.core.humanverification.presentation.onHumanVerificationNeeded
import me.proton.core.network.domain.humanverification.HumanVerificationAvailableMethods
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.entity.PlanDetails
import me.proton.core.payment.presentation.onPaymentResult
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val humanVerificationManager: HumanVerificationManager,
    private var authOrchestrator: AuthOrchestrator,
    private var humanVerificationOrchestrator: HumanVerificationOrchestrator,
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
        humanVerificationOrchestrator.register(context)
        paymentsOrchestrator.register(context)

        accountManager.getAccounts()
            .flowWithLifecycle(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
            .onEach { accounts ->
                if (accounts.isEmpty())
                    _state.tryEmit(State.LoginNeeded)
                else
                    _state.tryEmit(State.AccountList(accounts))
            }.launchIn(context.lifecycleScope)

        with(authOrchestrator) {
            accountManager.observe(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
                .onSessionSecondFactorNeeded { startSecondFactorWorkflow(it) }
                .onAccountTwoPassModeNeeded { startTwoPassModeWorkflow(it) }
                .onAccountCreateAddressNeeded { startChooseAddressWorkflow(it) }
                .onAccountTwoPassModeFailed { accountManager.disableAccount(it.userId) }
                .onAccountCreateAddressFailed { accountManager.disableAccount(it.userId) }
                .disableInitialNotReadyAccounts()
        }

        with(humanVerificationOrchestrator) {
            humanVerificationManager.observe(context.lifecycle, minActiveState = Lifecycle.State.RESUMED)
                .onHumanVerificationNeeded {
                    startHumanVerificationWorkflow(
                        clientId = it.clientId,
                        methods = HumanVerificationAvailableMethods(
                            it.verificationMethods, it.captchaVerificationToken
                        )
                    )
                }
        }
    }

    suspend fun signOut(userId: UserId) = accountManager.disableAccount(userId)

    suspend fun remove(userId: UserId) = accountManager.removeAccount(userId)

    suspend fun setAsPrimary(userId: UserId) = accountManager.setAsPrimary(userId)

    fun getPrimaryUserId() = accountManager.getPrimaryUserId()

    fun signIn(username: String? = null) = authOrchestrator.startLoginWorkflow(AccountType.Internal, username)

    fun add() = authOrchestrator.startAddAccountWorkflow(AccountType.Internal, Product.Mail)

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

    /**
     * Starts account creation for Mail account type (Internal)
     */
    fun onSignUpClicked() {
        viewModelScope.launch {
            authOrchestrator.startSignupWorkflow()
        }
    }

    /**
     * Starts account creation for VPN account type (Username only).
     */
    fun onExternalSignUpClicked() {
        viewModelScope.launch {
            authOrchestrator.startSignupWorkflow(requiredAccountType = AccountType.External)
        }
    }

    fun onPaySignUpClicked() {
        viewModelScope.launch {
            paymentsOrchestrator.startBillingWorkFlow(
                selectedPlan = PlanDetails(
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
                    onPaymentResult {
                        // do something with the payment result
                    }

                    startBillingWorkFlow(
                        userId = account.userId,
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

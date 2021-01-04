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

package me.proton.android.core.coreexample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.api.CoreExampleRepository
import me.proton.android.core.coreexample.databinding.ActivityMainBinding
import me.proton.android.core.coreexample.ui.CustomViewsActivity
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountDisabled
import me.proton.core.accountmanager.presentation.onAccountReady
import me.proton.core.accountmanager.presentation.onAccountRemoved
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeFailed
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeNeeded
import me.proton.core.accountmanager.presentation.onSessionHumanVerificationNeeded
import me.proton.core.accountmanager.presentation.onSessionHumanVerificationFailed
import me.proton.core.accountmanager.presentation.onSessionSecondFactorFailed
import me.proton.core.accountmanager.presentation.onSessionSecondFactorNeeded
import me.proton.core.auth.domain.entity.AccountType
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.onHumanVerificationResult
import me.proton.core.auth.presentation.onLoginResult
import me.proton.core.auth.presentation.onScopeResult
import me.proton.core.auth.presentation.onUserResult
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.showForceUpdate
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ProtonActivity<ActivityMainBinding>() {

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var authOrchestrator: AuthOrchestrator

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var coreExampleRepository: CoreExampleRepository

    override fun layoutId(): Int = R.layout.activity_main

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authOrchestrator.register(this)

        authOrchestrator
            .onLoginResult { result ->
                result
            }
            .onUserResult { result ->
                result
            }
            .onScopeResult { result ->
                result
            }
            .onHumanVerificationResult { }

        with(binding) {
            humanVerification.onClick {
                authOrchestrator.startHumanVerificationWorkflow(
                    SessionId("sessionId"),
                    HumanVerificationDetails(
                        listOf(
                            VerificationMethod.CAPTCHA,
                            VerificationMethod.EMAIL,
                            VerificationMethod.PHONE
                        )
                    )
                )
            }
            customViews.onClick { startActivity(Intent(this@MainActivity, CustomViewsActivity::class.java)) }
            login.onClick { authOrchestrator.startLoginWorkflow(AccountType.Internal) }
            forceUpdate.onClick {
                supportFragmentManager.showForceUpdate(
                    apiErrorMessage = "Error Message coming from the API."
                )
            }

            triggerHumanVer.onClick {
                accountManager.getPrimaryUserId().onEach { userId ->
                    userId?.let {
                        coreExampleRepository.triggerHumanVerification(it)
                    }
                }.launchIn(lifecycleScope)
            }
        }

        accountManager.getPrimaryAccount().onEach { primary ->
            binding.primaryAccountText.text = "Primary: ${primary?.username}"
        }.launchIn(lifecycleScope)

        accountManager.getAccounts().onEach { accounts ->
            if (accounts.isEmpty()) authOrchestrator.startLoginWorkflow(AccountType.Internal)

            binding.accountsLayout.removeAllViews()
            accounts.forEach { account ->
                binding.accountsLayout.addView(
                    Button(this@MainActivity).apply {
                        text = "${account.username} -> ${account.state}/${account.sessionState}"
                        onClick {
                            lifecycleScope.launch {
                                when (account.state) {
                                    AccountState.Ready ->
                                        accountManager.disableAccount(account.userId)
                                    AccountState.Disabled ->
                                        accountManager.removeAccount(account.userId)
                                    AccountState.NotReady,
                                    AccountState.TwoPassModeNeeded,
                                    AccountState.TwoPassModeFailed ->
                                        when (account.sessionState) {
                                            SessionState.SecondFactorNeeded,
                                            SessionState.SecondFactorFailed ->
                                                accountManager.disableAccount(account.userId)
                                            SessionState.Authenticated ->
                                                authOrchestrator.startTwoPassModeWorkflow(
                                                    account.sessionId!!,
                                                    AccountType.Username
                                                )
                                            else -> Unit
                                        }
                                    else -> Unit
                                }
                            }
                        }
                    }
                )
            }
        }.launchIn(lifecycleScope)

        accountManager.onHumanVerificationNeeded().onEach { (account, details) ->
            authOrchestrator.startHumanVerificationWorkflow(account.sessionId!!, details)
        }.launchIn(lifecycleScope)

        // Used to test session ForceLogout.
        accountManager.observe(lifecycleScope)
            .onAccountDisabled {
                Timber.d("onAccountDisabled -> remove $it")
                accountManager.removeAccount(it.userId)
            }
            .onAccountRemoved {
                Timber.d("onAccountRemoved -> $it")
            }
            .onAccountReady {
                Timber.d("onAccountReady -> $it")
            }
            .onSessionSecondFactorNeeded {
                Timber.d("onSessionSecondFactorNeeded -> $it")
            }
            .onSessionSecondFactorFailed {
                Timber.d("onSessionSecondFactorFailed -> $it")
            }
            .onAccountTwoPassModeNeeded {
                Timber.d("onAccountTwoPassModeNeeded -> $it")
            }
            .onAccountTwoPassModeFailed {
                Timber.d("onAccountTwoPassModeNeeded -> $it")
            }
            .onSessionHumanVerificationNeeded {
                Timber.d("onSessionHumanVerificationNeeded -> $it")
            }
            .onSessionHumanVerificationFailed {
                Timber.d("onSessionHumanVerificationFailed -> $it")
                // on failed human verification, we do not allow the user to have any interaction with the application.
                finish()
            }

        // Api Call every 10sec (e.g. to test ForceLogout). - commeted for now, move it into another activity
//        lifecycleScope.launch {
//            while (true) {
//                delay(10000)
//                accountManager.getPrimaryAccount().firstOrNull()?.let { account ->
//                    account.sessionId?.let { authRepository.getUser(it) }
//                }
//            }
//        }
    }
}

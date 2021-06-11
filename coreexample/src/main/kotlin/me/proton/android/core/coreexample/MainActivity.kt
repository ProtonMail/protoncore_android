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
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.api.CoreExampleRepository
import me.proton.android.core.coreexample.databinding.ActivityMainBinding
import me.proton.android.core.coreexample.ui.CustomViewsActivity
import me.proton.android.core.coreexample.ui.TextStylesActivity
import me.proton.android.core.coreexample.viewmodel.AccountViewModel
import me.proton.android.core.coreexample.viewmodel.MailMessageViewModel
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.presentation.viewmodel.AccountSwitcherViewModel
import me.proton.core.auth.presentation.ui.AddAccountActivity
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.showForceUpdate
import me.proton.core.presentation.utils.showToast
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ProtonActivity<ActivityMainBinding>() {

    @Inject
    lateinit var coreExampleRepository: CoreExampleRepository

    private val accountViewModel: AccountViewModel by viewModels()
    private val accountSwitcherViewModel: AccountSwitcherViewModel by viewModels()
    private val mailMessageViewModel: MailMessageViewModel by viewModels()

    override fun layoutId(): Int = R.layout.activity_main

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ProtonTheme)
        super.onCreate(savedInstanceState)

        accountViewModel.register(this)

        with(binding) {
            customViews.onClick { startActivity(Intent(this@MainActivity, CustomViewsActivity::class.java)) }
            textStyles.onClick { startActivity(Intent(this@MainActivity, TextStylesActivity::class.java)) }
            addAccount.onClick { startActivity(Intent(this@MainActivity, AddAccountActivity::class.java)) }
            signIn.onClick { accountViewModel.signIn() }
            signup.onClick { accountViewModel.onSignUpClicked() }
            signupExternal.onClick { accountViewModel.onExternalSignUpClicked() }
            forceUpdate.onClick { supportFragmentManager.showForceUpdate("Error Message coming from the API.") }
            triggerHumanVer.onClick {
                lifecycleScope.launch(Dispatchers.IO) {
                    accountViewModel.getPrimaryUserId().first()?.let {
                        coreExampleRepository.triggerHumanVerification(it)
                    }
                }
            }
            usernameAvailable.onClick {
                lifecycleScope.launch(Dispatchers.IO) {
                    coreExampleRepository.usernameAvailable()
                }
            }
            sendDirect.onClick { mailMessageViewModel.sendDirect() }
            payment.onClick { accountViewModel.onPayUpgradeClicked() }
            plans.onClick { accountViewModel.onPlansClicked() }
            plansUpgrade.onClick { accountViewModel.onPlansUpgradeClicked(this@MainActivity) }
            plansCurrent.onClick { accountViewModel.onCurrentPlanClicked(this@MainActivity) }

            accountPrimaryView.setViewModel(accountSwitcherViewModel)
            accountSwitcherViewModel.onAction().onEach {
                when (it) {
                    is AccountSwitcherViewModel.Action.Add -> accountViewModel.signIn()
                    is AccountSwitcherViewModel.Action.SignIn -> accountViewModel.signIn(it.account.username)
                    is AccountSwitcherViewModel.Action.SignOut -> accountViewModel.signOut(it.account.userId)
                    is AccountSwitcherViewModel.Action.Remove -> accountViewModel.remove(it.account.userId)
                    is AccountSwitcherViewModel.Action.SetPrimary -> accountViewModel.setAsPrimary(it.account.userId)
                }
            }.launchIn(lifecycleScope)
        }

        accountViewModel.state.onEach { state ->
            when (state) {
                is AccountViewModel.State.Processing -> Unit
                is AccountViewModel.State.LoginNeeded -> accountViewModel.add()
                is AccountViewModel.State.AccountList -> displayAccounts(state.accounts)
            }.exhaustive
        }.launchIn(lifecycleScope)

        mailMessageViewModel.getState().onEach {
            showToast("MailMessage: $it")
        }.launchIn(lifecycleScope)
    }

    @SuppressLint("SetTextI18n")
    private fun displayAccounts(accounts: List<Account>) {
        binding.accountsLayout.removeAllViews()
        accounts.forEach { account ->
            binding.accountsLayout.addView(
                Button(this@MainActivity).apply {
                    text = "${account.username} -> ${account.state}/${account.sessionState}"
                    onClick { accountViewModel.onAccountClicked(account.userId) }
                }
            )
        }
    }
}

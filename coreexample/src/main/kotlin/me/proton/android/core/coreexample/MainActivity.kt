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
import me.proton.android.core.coreexample.ui.ContactsActivity
import me.proton.android.core.coreexample.ui.CustomViewsActivity
import me.proton.android.core.coreexample.ui.TextStylesActivity
import me.proton.android.core.coreexample.viewmodel.AccountViewModel
import me.proton.android.core.coreexample.viewmodel.ReportsViewModel
import me.proton.android.core.coreexample.viewmodel.MailMessageViewModel
import me.proton.android.core.coreexample.viewmodel.MailSettingsViewModel
import me.proton.android.core.coreexample.viewmodel.PlansViewModel
import me.proton.android.core.coreexample.viewmodel.PublicAddressViewModel
import me.proton.android.core.coreexample.viewmodel.UserAddressKeyViewModel
import me.proton.android.core.coreexample.viewmodel.UserKeyViewModel
import me.proton.android.core.coreexample.viewmodel.UserSettingsViewModel
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.presentation.viewmodel.AccountSwitcherViewModel
import me.proton.core.auth.presentation.ui.AddAccountActivity
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.ui.alert.ForceUpdateActivity
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.showToast
import me.proton.core.presentation.utils.successSnack
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ProtonViewBindingActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    @Inject
    lateinit var coreExampleRepository: CoreExampleRepository

    private val accountViewModel: AccountViewModel by viewModels()
    private val reportsViewModel: ReportsViewModel by viewModels()
    private val plansViewModel: PlansViewModel by viewModels()
    private val accountSwitcherViewModel: AccountSwitcherViewModel by viewModels()
    private val mailMessageViewModel: MailMessageViewModel by viewModels()
    private val mailSettingsViewModel: MailSettingsViewModel by viewModels()
    private val userKeyViewModel: UserKeyViewModel by viewModels()
    private val userAddressKeyViewModel: UserAddressKeyViewModel by viewModels()
    private val publicAddressViewModel: PublicAddressViewModel by viewModels()
    private val settingsViewModel: UserSettingsViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ProtonTheme)
        super.onCreate(savedInstanceState)

        accountViewModel.register(this)
        reportsViewModel.register(this)
        plansViewModel.register(this)
        settingsViewModel.register(this)

        with(binding) {
            customViews.onClick { startActivity(Intent(this@MainActivity, CustomViewsActivity::class.java)) }
            textStyles.onClick { startActivity(Intent(this@MainActivity, TextStylesActivity::class.java)) }
            addAccount.onClick { startActivity(Intent(this@MainActivity, AddAccountActivity::class.java)) }
            signIn.onClick { accountViewModel.signIn() }
            signup.onClick { accountViewModel.onSignUpClicked() }
            signupExternal.onClick { accountViewModel.onExternalSignUpClicked() }
            forceUpdate.onClick {
                startActivity(ForceUpdateActivity(this@MainActivity, "Error Message coming from the API."))
            }
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
            plans.onClick { plansViewModel.onPlansClicked() }
            plansUpgrade.onClick { plansViewModel.onPlansUpgradeClicked(this@MainActivity) }
            plansCurrent.onClick { plansViewModel.onCurrentPlanClicked(this@MainActivity) }

            settingsRecovery.onClick { settingsViewModel.onUpdateRecoveryEmailClicked(this@MainActivity) }
            settingsPassword.onClick { settingsViewModel.onPasswordManagementClicked(this@MainActivity) }

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

            bugReport.onClick { reportsViewModel.reportBugs(waitForServer = false) }
            bugReportWaiting.onClick { reportsViewModel.reportBugs(waitForServer = true) }
            contacts.onClick { startActivity(Intent(this@MainActivity, ContactsActivity::class.java)) }
        }

        accountViewModel.state.onEach { state ->
            when (state) {
                is AccountViewModel.State.Processing -> Unit
                is AccountViewModel.State.LoginNeeded -> accountViewModel.add()
                is AccountViewModel.State.AccountList -> displayAccounts(state.accounts)
            }.exhaustive
        }.launchIn(lifecycleScope)

        reportsViewModel.bugReportSent.onEach {
            binding.root.successSnack(it)
        }.launchIn(lifecycleScope)

        mailMessageViewModel.getState().onEach { showToast("MailMessage: $it") }.launchIn(lifecycleScope)

        mailSettingsViewModel.getMailSettingsState().onEach {
            if (it is MailSettingsViewModel.MailSettingsState.Error) { showToast("MailSettings: $it") }
        }.launchIn(lifecycleScope)

        userKeyViewModel.getUserKeyState().onEach {
            if (it is UserKeyViewModel.UserKeyState.Error) { showToast("UserKey: $it") }
        }.launchIn(lifecycleScope)

        userAddressKeyViewModel.getUserAddressKeyState().onEach {
            if (it is UserAddressKeyViewModel.UserAddressKeyState.Error) { showToast("UserAddressKey: $it") }
        }.launchIn(lifecycleScope)

        publicAddressViewModel.getPublicAddressState().onEach {
            if (it is PublicAddressViewModel.PublicAddressState.Error) { showToast("PublicAddress: $it") }
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

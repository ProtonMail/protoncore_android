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
import me.proton.android.core.coreexample.viewmodel.AccountViewModel
import me.proton.android.core.coreexample.viewmodel.MailMessageViewModel
import me.proton.android.core.coreexample.viewmodel.PublicAddressViewModel
import me.proton.android.core.coreexample.viewmodel.UserAddressKeyViewModel
import me.proton.android.core.coreexample.viewmodel.UserKeyViewModel
import me.proton.core.account.domain.entity.Account
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
    private val userKeyViewModel: UserKeyViewModel by viewModels()
    private val userAddressKeyViewModel: UserAddressKeyViewModel by viewModels()
    private val publicAddressViewModel: PublicAddressViewModel by viewModels()
    private val mailMessageViewModel: MailMessageViewModel by viewModels()

    override fun layoutId(): Int = R.layout.activity_main

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountViewModel.register(this)
        accountViewModel.handleAccountState()

        with(binding) {
            customViews.onClick { startActivity(Intent(this@MainActivity, CustomViewsActivity::class.java)) }
            login.onClick { accountViewModel.startLoginWorkflow() }
            forceUpdate.onClick {
                supportFragmentManager.showForceUpdate(
                    apiErrorMessage = "Error Message coming from the API."
                )
            }
            triggerHumanVer.onClick {
                lifecycleScope.launch(Dispatchers.IO) {
                    accountViewModel.getPrimaryUserId().first()?.let {
                        coreExampleRepository.triggerHumanVerification(it)
                    }
                }
            }
            sendDirect.onClick { mailMessageViewModel.sendDirect() }

            payment.onClick {
                accountViewModel.onPayUpgradeClicked()
            }
        }

        accountViewModel.getPrimaryAccount().onEach { primary ->
            binding.primaryAccountText.text = "Primary: ${primary?.username}"
        }.launchIn(lifecycleScope)

        accountViewModel.state.onEach { state ->
            when (state) {
                is AccountViewModel.State.Processing -> Unit
                is AccountViewModel.State.LoginNeeded -> accountViewModel.startLoginWorkflow()
                is AccountViewModel.State.AccountList -> displayAccounts(state.accounts)
            }.exhaustive
        }.launchIn(lifecycleScope)

        userKeyViewModel.getUserKeyState().onEach { state ->
            binding.primaryAccountKeyState.text = "User Key State: ${state::class.java.simpleName}"
        }.launchIn(lifecycleScope)

        userAddressKeyViewModel.getUserAddressKeyState().onEach { state ->
            binding.primaryAccountAddressKeyState.text = "Address Key State: ${state::class.java.simpleName}"
        }.launchIn(lifecycleScope)

        publicAddressViewModel.getPublicAddressState().onEach { state ->
            binding.primaryAccountPublicAddressState.text = "Public Address State: ${state::class.java.simpleName}"
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

/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityAddAccountBinding
import me.proton.core.auth.presentation.entity.AddAccountInput
import me.proton.core.auth.presentation.entity.AddAccountResult
import me.proton.core.auth.presentation.entity.AddAccountWorkflow
import me.proton.core.auth.presentation.viewmodel.AddAccountViewModel
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.inTransaction

@AndroidEntryPoint
class AddAccountActivity :
    ProtonViewBindingActivity<ActivityAddAccountBinding>(ActivityAddAccountBinding::inflate) {

    private val input: AddAccountInput by lazy {
        intent?.extras?.getParcelable(ARG_INPUT) ?: AddAccountInput(
            requiredAccountType = AccountType.Internal,
            creatableAccountType = AccountType.Internal,
            product = null,
            loginUsername = null
        )
    }

    private val viewModel by viewModels<AddAccountViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOnBackPressedCallback { onClose() }
        supportFragmentManager.onAddAccountFragmentResult(this) {
            if (it != null) onSuccess(it.userId, it.workflow)
        }
        supportFragmentManager.onCredentialLessFragmentResult(this) {
            if (it != null) onSuccess(it.userId, it.workflow)
        }

        binding.progressIndicator.isVisible = true
        lifecycleScope.launch {
            when (viewModel.getNextScreen()) {
                AddAccountViewModel.Screen.AddAccountFragment ->
                    supportFragmentManager.showAddAccountFragment(input)

                AddAccountViewModel.Screen.CredentialLessFragment ->
                    supportFragmentManager.showCredentialLessFragment(input)
            }
            binding.progressIndicator.isVisible = false
        }
    }

    private fun onSuccess(userId: String, workflow: AddAccountWorkflow) {
        val intent =
            Intent().putExtra(ARG_RESULT, AddAccountResult(userId = userId, workflow = workflow))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    companion object {
        const val ARG_INPUT = "arg.addAccountInput"
        const val ARG_RESULT = "arg.addAccountResult"
    }
}

private fun FragmentManager.showAddAccountFragment(input: AddAccountInput) = inTransaction {
    replace(R.id.fragment_container, AddAccountFragment(input))
}

private fun FragmentManager.showCredentialLessFragment(input: AddAccountInput) = inTransaction {
    replace(R.id.fragment_container, CredentialLessWelcomeFragment(input))
}

private fun FragmentManager.onAddAccountFragmentResult(
    lifecycleOwner: LifecycleOwner,
    block: (AddAccountResult?) -> Unit
) {
    setFragmentResultListener(
        AddAccountFragment.ADD_ACCOUNT_REQUEST_KEY,
        lifecycleOwner
    ) { _, bundle ->
        val result =
            bundle.getParcelable<AddAccountResult>(AddAccountFragment.ARG_ADD_ACCOUNT_RESULT)
        block(result)
    }
}

private fun FragmentManager.onCredentialLessFragmentResult(
    lifecycleOwner: LifecycleOwner,
    block: (AddAccountResult?) -> Unit
) {
    setFragmentResultListener(
        CredentialLessWelcomeFragment.CREDENTIAL_LESS_REQUEST_KEY,
        lifecycleOwner
    ) { _, bundle ->
        val result =
            bundle.getParcelable<AddAccountResult>(CredentialLessWelcomeFragment.ARG_ADD_ACCOUNT_RESULT)
        block(result)
    }
}

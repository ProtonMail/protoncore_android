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
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityAddAccountBinding
import me.proton.core.auth.presentation.entity.AddAccountInput
import me.proton.core.auth.presentation.entity.AddAccountResult
import me.proton.core.auth.presentation.entity.AddAccountWorkflow
import me.proton.core.auth.presentation.onLoginResult
import me.proton.core.auth.presentation.onOnSignUpResult
import me.proton.core.auth.presentation.viewmodel.AddAccountViewModel
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.enableProtonEdgeToEdge
import me.proton.core.presentation.utils.inTransaction
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountActivity :
    ProtonViewBindingActivity<ActivityAddAccountBinding>(ActivityAddAccountBinding::inflate) {

    @Inject
    lateinit var authOrchestrator: AuthOrchestrator

    private var foregroundCall: (() -> Unit)? = null

    private val input: AddAccountInput by lazy {
        intent?.extras?.getParcelable(ARG_INPUT) ?: AddAccountInput(username = null)
    }

    private val viewModel by viewModels<AddAccountViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableProtonEdgeToEdge()
        super.onCreate(savedInstanceState)
        authOrchestrator.register(this)
        authOrchestrator.onLoginResult {
            if (it != null) onSuccess(it.userId, AddAccountWorkflow.SignIn)
        }
        authOrchestrator.onOnSignUpResult {
            if (it != null) onSuccess(it.userId, AddAccountWorkflow.SignUp)
        }

        addOnBackPressedCallback { onClose() }

        binding.progressIndicator.isVisible = true
        lifecycleScope.launch {
            when (viewModel.getNextScreen()) {
                AddAccountViewModel.Screen.AddAccountFragment -> updateView(
                    supportFragmentManager.showAddAccountFragment(input)
                )

                AddAccountViewModel.Screen.CredentialLessFragment -> updateView(
                    supportFragmentManager.showCredentialLessFragment(input)
                )
            }
            binding.progressIndicator.isVisible = false
        }
    }

    override fun onDestroy() {
        authOrchestrator.unregister()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        foregroundCall?.let {
            updateView(it)
            foregroundCall = null
        }
    }

    private fun updateView(action: () -> Unit) {
        if (!activityInForeground) {
            foregroundCall = action
        } else {
            action.invoke()
        }
    }

    internal fun onSuccess(userId: String, workflow: AddAccountWorkflow) {
        val intent = Intent().putExtra(ARG_RESULT, AddAccountResult(userId, workflow))
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

private fun FragmentManager.showAddAccountFragment(input: AddAccountInput): () -> Unit = {
    inTransaction {
        replace(R.id.fragment_container, AddAccountFragment(input))
        setTransition(TRANSIT_FRAGMENT_FADE)
    }
}

private fun FragmentManager.showCredentialLessFragment(input: AddAccountInput): () -> Unit = {
    inTransaction {
        replace(R.id.fragment_container, CredentialLessWelcomeFragment(input))
        setTransition(TRANSIT_FRAGMENT_FADE)
    }
}

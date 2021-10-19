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

package me.proton.core.auth.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.databinding.ActivityAddAccountBinding
import me.proton.core.auth.presentation.entity.AddAccountInput
import me.proton.core.auth.presentation.entity.AddAccountResult
import me.proton.core.auth.presentation.onLoginResult
import me.proton.core.auth.presentation.onOnSignUpResult
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.onClick
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountActivity : ProtonViewBindingActivity<ActivityAddAccountBinding>(ActivityAddAccountBinding::inflate) {

    @Inject
    lateinit var authOrchestrator: AuthOrchestrator

    @Inject
    lateinit var accountManager: AccountManager

    private val input: AddAccountInput? by lazy {
        intent?.extras?.getParcelable(ARG_INPUT)
    }

    private val requiredAccountType: AccountType by lazy {
        input?.requiredAccountType ?: AccountType.Internal
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authOrchestrator.register(this)

        binding.signIn.onClick { authOrchestrator.startLoginWorkflow(requiredAccountType) }
        authOrchestrator.onLoginResult { if (it != null) onSuccess(it.userId) }

        binding.signUp.onClick { authOrchestrator.startSignupWorkflow(requiredAccountType) }
        authOrchestrator.onOnSignUpResult { if (it != null) onSuccess(it.userId) }
    }

    override fun onDestroy() {
        authOrchestrator.unregister()
        super.onDestroy()
    }

    override fun onBackPressed() {
        onClose()
    }

    private fun onSuccess(userId: String) {
        val intent = Intent().putExtra(ARG_RESULT, AddAccountResult(userId = userId))
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

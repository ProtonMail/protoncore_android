/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.entity.signup.SignUpInput
import me.proton.core.auth.presentation.entity.signup.SignUpResult
import me.proton.core.auth.presentation.ui.StartSignup
import javax.inject.Inject

class SignupOrchestrator @Inject constructor() {

    private var signUpWorkflowLauncher: ActivityResultLauncher<SignUpInput>? = null

    private var onSignUpResultListener: (result: SignUpResult?) -> Unit = {}

    fun setOnSignUpResult(block: (result: SignUpResult?) -> Unit) {
        onSignUpResultListener = block
    }

    // region private api
    private fun registerSignUpResult(
        context: ComponentActivity
    ): ActivityResultLauncher<SignUpInput> =
        context.registerForActivityResult(
            StartSignup()
        ) { result -> onSignUpResultListener(result) }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call signupOrchestrator.register(context) before starting workflow!" }
    // endregion

    // region public API
    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(context: ComponentActivity) {
        signUpWorkflowLauncher = registerSignUpResult(context)
    }

    /**
     * Starts the SignUp workflow.
     */
    fun startSignupWorkflow(requiredAccountType: AccountType = AccountType.Internal) {
        checkRegistered(signUpWorkflowLauncher).launch(
            SignUpInput(requiredAccountType)
        )
    }
    // endregion
}

fun SignupOrchestrator.onSignupResult(
    block: (result: SignUpResult?) -> Unit
): SignupOrchestrator {
    setOnSignUpResult { block(it) }
    return this
}

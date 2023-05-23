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

package me.proton.core.auth.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.alert.confirmpass.StartConfirmPassword
import me.proton.core.auth.presentation.entity.AddAccountInput
import me.proton.core.auth.presentation.entity.AddAccountResult
import me.proton.core.auth.presentation.entity.ChooseAddressInput
import me.proton.core.auth.presentation.entity.ChooseAddressResult
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.LoginSsoInput
import me.proton.core.auth.presentation.entity.LoginSsoResult
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.SecondFactorResult
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.entity.TwoPassModeResult
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordInput
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordResult
import me.proton.core.auth.presentation.entity.signup.SignUpInput
import me.proton.core.auth.presentation.entity.signup.SignUpResult
import me.proton.core.auth.presentation.ui.StartAddAccount
import me.proton.core.auth.presentation.ui.StartChooseAddress
import me.proton.core.auth.presentation.ui.StartLogin
import me.proton.core.auth.presentation.ui.StartLoginSso
import me.proton.core.auth.presentation.ui.StartSecondFactor
import me.proton.core.auth.presentation.ui.StartSignup
import me.proton.core.auth.presentation.ui.StartTwoPassMode
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.scopes.MissingScopeState
import javax.inject.Inject

class AuthOrchestrator @Inject constructor() {

    // region result launchers
    private var addAccountWorkflowLauncher: ActivityResultLauncher<AddAccountInput>? = null
    private var loginWorkflowLauncher: ActivityResultLauncher<LoginInput>? = null
    private var loginSsoWorkflowLauncher: ActivityResultLauncher<LoginSsoInput>? = null
    private var secondFactorWorkflowLauncher: ActivityResultLauncher<SecondFactorInput>? = null
    private var twoPassModeWorkflowLauncher: ActivityResultLauncher<TwoPassModeInput>? = null
    private var chooseAddressLauncher: ActivityResultLauncher<ChooseAddressInput>? = null
    private var signUpWorkflowLauncher: ActivityResultLauncher<SignUpInput>? = null
    private var confirmPasswordWorkflowLauncher: ActivityResultLauncher<ConfirmPasswordInput>? =
        null
    // endregion

    private var onAddAccountResultListener: ((result: AddAccountResult?) -> Unit)? = {}
    private var onLoginResultListener: ((result: LoginResult?) -> Unit)? = {}
    private var onLoginSsoResultListener: ((result: LoginSsoResult?) -> Unit)? = {}
    private var onTwoPassModeResultListener: ((result: TwoPassModeResult?) -> Unit)? = {}
    private var onSecondFactorResultListener: ((result: SecondFactorResult?) -> Unit)? = {}
    private var onChooseAddressResultListener: ((result: ChooseAddressResult?) -> Unit)? = {}
    private var onSignUpResultListener: ((result: SignUpResult?) -> Unit)? = {}
    private var onConfirmPasswordResultListener: ((result: ConfirmPasswordResult?) -> Unit)? = {}

    fun setOnAddAccountResult(block: (result: AddAccountResult?) -> Unit) {
        onAddAccountResultListener = block
    }

    fun setOnLoginResult(block: (result: LoginResult?) -> Unit) {
        onLoginResultListener = block
    }

    fun setOnLoginSsoResult(block: (result: LoginSsoResult?) -> Unit) {
        onLoginSsoResultListener = block
    }

    fun setOnTwoPassModeResult(block: (result: TwoPassModeResult?) -> Unit) {
        onTwoPassModeResultListener = block
    }

    fun setOnSecondFactorResult(block: (result: SecondFactorResult?) -> Unit) {
        onSecondFactorResultListener = block
    }

    fun setOnChooseAddressResult(block: (result: ChooseAddressResult?) -> Unit) {
        onChooseAddressResultListener = block
    }

    fun setOnSignUpResult(block: (result: SignUpResult?) -> Unit) {
        onSignUpResultListener = block
    }

    @Deprecated("Will be removed in the next major release.")
    fun setOnConfirmPasswordResult(block: (result: ConfirmPasswordResult?) -> Unit) {
        onConfirmPasswordResultListener = block
    }

    // region private module functions

    private fun registerAddAccountResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<AddAccountInput> =
        caller.registerForActivityResult(
            StartAddAccount()
        ) {
            onAddAccountResultListener?.invoke(it)
        }

    private fun registerLoginResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<LoginInput> =
        caller.registerForActivityResult(
            StartLogin()
        ) {
            onLoginResultListener?.invoke(it)
        }

    private fun registerLoginSsoResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<LoginSsoInput> =
        caller.registerForActivityResult(
            StartLoginSso()
        ) {
            onLoginSsoResultListener?.invoke(it)
        }

    private fun registerTwoPassModeResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<TwoPassModeInput> =
        caller.registerForActivityResult(
            StartTwoPassMode()
        ) {
            onTwoPassModeResultListener?.invoke(it)
        }

    private fun registerSecondFactorResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<SecondFactorInput> =
        caller.registerForActivityResult(
            StartSecondFactor()
        ) {
            onSecondFactorResultListener?.invoke(it)
        }

    private fun registerChooseAddressResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<ChooseAddressInput> =
        caller.registerForActivityResult(
            StartChooseAddress()
        ) {
            onChooseAddressResultListener?.invoke(it)
        }

    private fun registerSignUpResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<SignUpInput> =
        caller.registerForActivityResult(
            StartSignup()
        ) {
            onSignUpResultListener?.invoke(it)
        }

    private fun registerConfirmPasswordResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<ConfirmPasswordInput> =
        caller.registerForActivityResult(
            StartConfirmPassword()
        ) {
            onConfirmPasswordResultListener?.invoke(it)
        }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call authOrchestrator.register(context) before starting workflow!" }

    private fun startSecondFactorWorkflow(
        userId: UserId,
        requiredAccountType: AccountType,
        password: EncryptedString,
        isTwoPassModeNeeded: Boolean
    ) {
        checkRegistered(secondFactorWorkflowLauncher).launch(
            SecondFactorInput(userId.id, password, requiredAccountType, isTwoPassModeNeeded)
        )
    }

    private fun startTwoPassModeWorkflow(
        userId: UserId,
        requiredAccountType: AccountType
    ) {
        checkRegistered(twoPassModeWorkflowLauncher).launch(
            TwoPassModeInput(userId.id, requiredAccountType)
        )
    }

    private fun startChooseAddressWorkflow(
        userId: UserId,
        password: EncryptedString,
        externalEmail: String,
        isTwoPassModeNeeded: Boolean
    ) {
        checkRegistered(chooseAddressLauncher).launch(
            ChooseAddressInput(
                userId.id,
                password = password,
                recoveryEmail = externalEmail,
                isTwoPassModeNeeded = isTwoPassModeNeeded
            )
        )
    }

    // endregion

    // region public API
    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(caller: ActivityResultCaller) {
        addAccountWorkflowLauncher = registerAddAccountResult(caller)
        loginWorkflowLauncher = registerLoginResult(caller)
        secondFactorWorkflowLauncher = registerSecondFactorResult(caller)
        twoPassModeWorkflowLauncher = registerTwoPassModeResult(caller)
        chooseAddressLauncher = registerChooseAddressResult(caller)
        signUpWorkflowLauncher = registerSignUpResult(caller)
        confirmPasswordWorkflowLauncher = registerConfirmPasswordResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        addAccountWorkflowLauncher?.unregister()
        loginWorkflowLauncher?.unregister()
        secondFactorWorkflowLauncher?.unregister()
        twoPassModeWorkflowLauncher?.unregister()
        chooseAddressLauncher?.unregister()
        signUpWorkflowLauncher?.unregister()

        addAccountWorkflowLauncher = null
        loginWorkflowLauncher = null
        secondFactorWorkflowLauncher = null
        twoPassModeWorkflowLauncher = null
        chooseAddressLauncher = null
        signUpWorkflowLauncher = null

        onAddAccountResultListener = null
        onLoginResultListener = null
        onTwoPassModeResultListener = null
        onSecondFactorResultListener = null
        onChooseAddressResultListener = null
        onSignUpResultListener = null
    }

    /**
     * Starts the Add Account workflow (sign in or sign up).
     *
     * @see [onAddAccountResult]
     */
    fun startAddAccountWorkflow(
        requiredAccountType: AccountType,
        creatableAccountType: AccountType,
        product: Product,
        loginUsername: String? = null
    ) {
        checkRegistered(addAccountWorkflowLauncher).launch(
            AddAccountInput(
                requiredAccountType = requiredAccountType,
                creatableAccountType = creatableAccountType,
                product = product,
                loginUsername = loginUsername
            )
        )
    }

    /**
     * Starts the Login workflow.
     *
     * @see [onLoginResult]
     */
    fun startLoginWorkflow(
        requiredAccountType: AccountType,
        username: String? = null,
        password: String? = null
    ) {
        checkRegistered(loginWorkflowLauncher).launch(
            LoginInput(requiredAccountType, username, password)
        )
    }

    /**
     * Starts the Login SSO workflow.
     *
     * @see [onLoginSsoResult]
     */
    fun startLoginSsoWorkflow(
        email: String? = null,
    ) {
        checkRegistered(loginSsoWorkflowLauncher).launch(
            LoginSsoInput(email)
        )
    }

    /**
     * Start a Second Factor workflow.
     * In case the Second Factor fails with [SecondFactorResult.UnrecoverableError],
     * you should likely show/go back to the login screen and show the error message there.
     * @see [onSecondFactorResult]
     */
    fun startSecondFactorWorkflow(account: Account) {
        val requiredAccountType = checkNotNull(account.details.session?.requiredAccountType) {
            "Required AccountType is null for startSecondFactorWorkflow."
        }
        val password = checkNotNull(account.details.session?.password) {
            "Password is null for startSecondFactorWorkflow."
        }
        val twoPassModeEnabled = checkNotNull(account.details.session?.twoPassModeEnabled) {
            "TwoPassModeEnabled is null for startSecondFactorWorkflow."
        }
        startSecondFactorWorkflow(
            userId = account.userId,
            requiredAccountType = requiredAccountType,
            password = password,
            isTwoPassModeNeeded = twoPassModeEnabled
        )
    }

    /**
     * Start a TwoPassMode workflow.
     *
     * @see [onTwoPassModeResult]
     */
    fun startTwoPassModeWorkflow(account: Account) {
        val requiredAccountType = checkNotNull(account.details.session?.requiredAccountType) {
            "Required AccountType is null for startSecondFactorWorkflow."
        }
        startTwoPassModeWorkflow(account.userId, requiredAccountType)
    }

    /**
     * Start the Choose/Create Address workflow.
     *
     * @see [onChooseAddressResult]
     */
    fun startChooseAddressWorkflow(account: Account) {
        val email = checkNotNull(account.email) {
            "Email is null for startChooseAddressWorkflow."
        }
        val password = checkNotNull(account.details.session?.password) {
            "Password is null for startChooseAddressWorkflow."
        }
        val twoPassModeEnabled = checkNotNull(account.details.session?.twoPassModeEnabled) {
            "TwoPassModeEnabled is null for startChooseAddressWorkflow."
        }
        startChooseAddressWorkflow(
            userId = account.userId,
            password = password,
            externalEmail = email,
            isTwoPassModeNeeded = twoPassModeEnabled
        )
    }

    /**
     * Starts the SignUp workflow.
     */
    fun startSignupWorkflow(creatableAccountType: AccountType = AccountType.Internal) {
        checkRegistered(signUpWorkflowLauncher).launch(
            SignUpInput(creatableAccountType)
        )
    }

    /**
     * Starts the Confirm Password workflow.
     */
    @Deprecated("Will be removed in the next major release.")
    fun startConfirmPasswordWorkflow(scopeMissing: MissingScopeState.ScopeMissing) {
        checkRegistered(confirmPasswordWorkflowLauncher).launch(
            ConfirmPasswordInput(
                userId = scopeMissing.userId.id,
                missingScopes = scopeMissing.missingScopes.map { it.value }
            )
        )
    }
    // endregion
}

fun AuthOrchestrator.onAddAccountResult(
    block: (result: AddAccountResult?) -> Unit
): AuthOrchestrator {
    setOnAddAccountResult { block(it) }
    return this
}

fun AuthOrchestrator.onOnSignUpResult(
    block: (result: SignUpResult?) -> Unit
): AuthOrchestrator {
    setOnSignUpResult { block(it) }
    return this
}

fun AuthOrchestrator.onLoginResult(
    block: (result: LoginResult?) -> Unit
): AuthOrchestrator {
    setOnLoginResult { block(it) }
    return this
}

fun AuthOrchestrator.onLoginSsoResult(
    block: (result: LoginSsoResult?) -> Unit
): AuthOrchestrator {
    setOnLoginSsoResult { block(it) }
    return this
}

fun AuthOrchestrator.onTwoPassModeResult(
    block: (result: TwoPassModeResult?) -> Unit
): AuthOrchestrator {
    setOnTwoPassModeResult { block(it) }
    return this
}

fun AuthOrchestrator.onSecondFactorResult(
    block: (result: SecondFactorResult?) -> Unit
): AuthOrchestrator {
    setOnSecondFactorResult { block(it) }
    return this
}

fun AuthOrchestrator.onChooseAddressResult(
    block: (result: ChooseAddressResult?) -> Unit
): AuthOrchestrator {
    setOnChooseAddressResult { block(it) }
    return this
}

@Deprecated("Will be removed in the next major release.")
fun AuthOrchestrator.onConfirmPasswordResult(
    block: (result: ConfirmPasswordResult?) -> Unit
): AuthOrchestrator {
    setOnConfirmPasswordResult { block(it) }
    return this
}

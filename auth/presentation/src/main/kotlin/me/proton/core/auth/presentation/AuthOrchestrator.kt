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

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.auth.domain.feature.IsLoginTwoStepEnabled
import me.proton.core.auth.presentation.alert.confirmpass.StartConfirmPassword
import me.proton.core.auth.presentation.entity.AddAccountInput
import me.proton.core.auth.presentation.entity.AddAccountResult
import me.proton.core.auth.presentation.entity.ChooseAddressAuthSecret
import me.proton.core.auth.presentation.entity.ChooseAddressInput
import me.proton.core.auth.presentation.entity.ChooseAddressResult
import me.proton.core.auth.presentation.entity.DeviceSecretResult
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.SecondFactorResult
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.entity.TwoPassModeResult
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordInput
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordResult
import me.proton.core.auth.presentation.entity.signup.SignUpInput
import me.proton.core.auth.presentation.entity.signup.SignUpResult
import me.proton.core.auth.presentation.entity.signup.SubscriptionDetails
import me.proton.core.auth.presentation.ui.LoginSsoActivity
import me.proton.core.auth.presentation.ui.LoginTwoStepActivity
import me.proton.core.auth.presentation.ui.StartAddAccount
import me.proton.core.auth.presentation.ui.StartChooseAddress
import me.proton.core.auth.presentation.ui.StartDeviceSecret
import me.proton.core.auth.presentation.ui.StartLogin
import me.proton.core.auth.presentation.ui.StartLoginTwoStep
import me.proton.core.auth.presentation.ui.StartSecondFactor
import me.proton.core.auth.presentation.ui.StartSignup
import me.proton.core.auth.presentation.ui.StartTwoPassMode
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.scopes.MissingScopeState
import me.proton.core.presentation.utils.setComponentSettings
import javax.inject.Inject

class AuthOrchestrator @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val isLoginTwoStepEnabled: IsLoginTwoStepEnabled
) {

    // region result launchers
    private var addAccountWorkflowLauncher: ActivityResultLauncher<AddAccountInput>? = null
    private var loginWorkflowLauncher: ActivityResultLauncher<LoginInput>? = null
    private var loginTwoStepWorkflowLauncher: ActivityResultLauncher<LoginInput>? = null
    private var secondFactorWorkflowLauncher: ActivityResultLauncher<SecondFactorInput>? = null
    private var twoPassModeWorkflowLauncher: ActivityResultLauncher<TwoPassModeInput>? = null
    private var chooseAddressLauncher: ActivityResultLauncher<ChooseAddressInput>? = null
    private var deviceSecretLauncher: ActivityResultLauncher<String>? = null
    private var signUpWorkflowLauncher: ActivityResultLauncher<SignUpInput>? = null
    private var confirmPasswordWorkflowLauncher: ActivityResultLauncher<ConfirmPasswordInput>? =
        null
    // endregion

    private var onAddAccountResultListener: ((result: AddAccountResult?) -> Unit)? = {}
    private var onLoginResultListener: ((result: LoginResult?) -> Unit)? = {}
    private var onTwoPassModeResultListener: ((result: TwoPassModeResult?) -> Unit)? = {}
    private var onSecondFactorResultListener: ((result: SecondFactorResult?) -> Unit)? = {}
    private var onChooseAddressResultListener: ((result: ChooseAddressResult?) -> Unit)? = {}
    private var onDeviceSecretResultListener: ((result: DeviceSecretResult?) -> Unit)? = {}
    private var onSignUpResultListener: ((result: SignUpResult?) -> Unit)? = {}
    private var onConfirmPasswordResultListener: ((result: ConfirmPasswordResult?) -> Unit)? = {}

    fun setOnAddAccountResult(block: (result: AddAccountResult?) -> Unit) {
        onAddAccountResultListener = block
    }

    fun setOnLoginResult(block: (result: LoginResult?) -> Unit) {
        onLoginResultListener = block
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

    fun setOnDeviceSecretResult(block: (result: DeviceSecretResult?) -> Unit) {
        onDeviceSecretResultListener = block
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
            StartAddAccount
        ) {
            onAddAccountResultListener?.invoke(it)
        }

    private fun registerLoginResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<LoginInput> =
        caller.registerForActivityResult(
            StartLogin
        ) {
            onLoginResultListener?.invoke(it)
        }

    private fun registerLoginTwoStepResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<LoginInput> =
        caller.registerForActivityResult(
            StartLoginTwoStep
        ) {
            onLoginResultListener?.invoke(it)
        }

    private fun registerTwoPassModeResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<TwoPassModeInput> =
        caller.registerForActivityResult(
            StartTwoPassMode
        ) {
            onTwoPassModeResultListener?.invoke(it)
        }

    private fun registerSecondFactorResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<SecondFactorInput> =
        caller.registerForActivityResult(
            StartSecondFactor
        ) {
            onSecondFactorResultListener?.invoke(it)
        }

    private fun registerChooseAddressResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<ChooseAddressInput> =
        caller.registerForActivityResult(
            StartChooseAddress
        ) {
            onChooseAddressResultListener?.invoke(it)
        }

    private fun registerDeviceSecretResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<String> =
        caller.registerForActivityResult(
            StartDeviceSecret
        ) {
            onDeviceSecretResultListener?.invoke(it)
        }

    private fun registerSignUpResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<SignUpInput> =
        caller.registerForActivityResult(
            StartSignup
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
        authSecret: ChooseAddressAuthSecret,
        externalEmail: String,
        isTwoPassModeNeeded: Boolean
    ) {
        checkRegistered(chooseAddressLauncher).launch(
            ChooseAddressInput(
                userId.id,
                authSecret = authSecret,
                recoveryEmail = externalEmail,
                isTwoPassModeNeeded = isTwoPassModeNeeded
            )
        )
    }

    private fun startDeviceSecretWorkflow(
        userId: UserId,
    ) {
        checkRegistered(deviceSecretLauncher).launch(userId.id)
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
        loginTwoStepWorkflowLauncher = registerLoginTwoStepResult(caller)
        secondFactorWorkflowLauncher = registerSecondFactorResult(caller)
        twoPassModeWorkflowLauncher = registerTwoPassModeResult(caller)
        chooseAddressLauncher = registerChooseAddressResult(caller)
        deviceSecretLauncher = registerDeviceSecretResult(caller)
        signUpWorkflowLauncher = registerSignUpResult(caller)
        confirmPasswordWorkflowLauncher = registerConfirmPasswordResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        addAccountWorkflowLauncher?.unregister()
        loginWorkflowLauncher?.unregister()
        loginTwoStepWorkflowLauncher?.unregister()
        secondFactorWorkflowLauncher?.unregister()
        twoPassModeWorkflowLauncher?.unregister()
        chooseAddressLauncher?.unregister()
        deviceSecretLauncher?.unregister()
        signUpWorkflowLauncher?.unregister()

        addAccountWorkflowLauncher = null
        loginWorkflowLauncher = null
        loginTwoStepWorkflowLauncher = null
        secondFactorWorkflowLauncher = null
        twoPassModeWorkflowLauncher = null
        chooseAddressLauncher = null
        deviceSecretLauncher = null
        signUpWorkflowLauncher = null

        onAddAccountResultListener = null
        onLoginResultListener = null
        onTwoPassModeResultListener = null
        onSecondFactorResultListener = null
        onChooseAddressResultListener = null
        onDeviceSecretResultListener = null
        onSignUpResultListener = null
    }

    /**
     * Starts the Add Account workflow (sign in or sign up).
     *
     * @see [onAddAccountResult]
     */
    fun startAddAccountWorkflow(
        username: String? = null
    ) {
        checkRegistered(addAccountWorkflowLauncher).launch(
            AddAccountInput(username = username)
        )
    }

    /**
     * Starts the Login workflow.
     *
     * @see [onLoginResult]
     */
    fun startLoginWorkflow(
        username: String? = null
    ) {
        if (isLoginTwoStepEnabled()) {
            // Disable LoginSsoActivity and let LoginTwoStepActivity handle redirect.
            context.setComponentSettings<LoginSsoActivity>(enabled = false)
            context.setComponentSettings<LoginTwoStepActivity>(enabled = true)
            checkRegistered(loginTwoStepWorkflowLauncher).launch(
                LoginInput(username)
            )
        } else {
            // Disable LoginTwoStepActivity and let LoginSsoActivity handle redirect.
            context.setComponentSettings<LoginTwoStepActivity>(enabled = false)
            context.setComponentSettings<LoginSsoActivity>(enabled = true)
            checkRegistered(loginWorkflowLauncher).launch(
                LoginInput(username)
            )
        }
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
        val authSecret = getChoosePasswordAuthSecret(account.details.session)
        val twoPassModeEnabled = checkNotNull(account.details.session?.twoPassModeEnabled) {
            "TwoPassModeEnabled is null for startChooseAddressWorkflow."
        }
        startChooseAddressWorkflow(
            userId = account.userId,
            authSecret = authSecret,
            externalEmail = email,
            isTwoPassModeNeeded = twoPassModeEnabled
        )
    }

    private fun getChoosePasswordAuthSecret(sessionDetails: SessionDetails?): ChooseAddressAuthSecret {
        val passphrase = sessionDetails?.passphrase
        val password = sessionDetails?.password
        return when {
            passphrase != null && password == null -> ChooseAddressAuthSecret.Passphrase(passphrase)
            passphrase == null && password != null -> ChooseAddressAuthSecret.Password(password)
            else -> error("Either passphrase or password must be set.")
        }
    }

    /**
     * Start the Device Secret workflow.
     *
     * @see [onDeviceSecretResult]
     */
    fun startDeviceSecretWorkflow(account: Account) {
        startDeviceSecretWorkflow(userId = account.userId)
    }

    /**
     * Starts the SignUp workflow.
     * If subscriptionDetails are provided, a new subscription will be created associated with the newly created account.
     * Note, this flow will take care of creating the subscription, but not plan validation nor token conversion.
     */
    fun startSignupWorkflow(
        cancellable: Boolean = true,
        email: String? = null,
        subscriptionDetails: SubscriptionDetails? = null
    ) {
        checkRegistered(signUpWorkflowLauncher).launch(
            SignUpInput(cancellable, email, subscriptionDetails)
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

fun AuthOrchestrator.onDeviceSecretResult(
    block: (result: DeviceSecretResult?) -> Unit
): AuthOrchestrator {
    setOnDeviceSecretResult { block(it) }
    return this
}

@Deprecated("Will be removed in the next major release.")
fun AuthOrchestrator.onConfirmPasswordResult(
    block: (result: ConfirmPasswordResult?) -> Unit
): AuthOrchestrator {
    setOnConfirmPasswordResult { block(it) }
    return this
}

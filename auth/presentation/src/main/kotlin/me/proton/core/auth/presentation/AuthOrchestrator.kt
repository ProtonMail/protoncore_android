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
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.presentation.entity.ChooseAddressInput
import me.proton.core.auth.presentation.entity.ChooseAddressResult
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.SecondFactorResult
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.entity.TwoPassModeResult
import me.proton.core.auth.presentation.ui.StartChooseAddress
import me.proton.core.auth.presentation.ui.StartLogin
import me.proton.core.auth.presentation.ui.StartSecondFactor
import me.proton.core.auth.presentation.ui.StartTwoPassMode
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.ui.StartHumanVerification
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

class AuthOrchestrator @Inject constructor(
    private val accountWorkflowHandler: AccountWorkflowHandler
) {

    // region result launchers
    private var loginWorkflowLauncher: ActivityResultLauncher<LoginInput>? = null
    private var secondFactorWorkflowLauncher: ActivityResultLauncher<SecondFactorInput>? = null
    private var twoPassModeWorkflowLauncher: ActivityResultLauncher<TwoPassModeInput>? = null
    private var humanWorkflowLauncher: ActivityResultLauncher<HumanVerificationInput>? = null
    private var chooseAddressLauncher: ActivityResultLauncher<ChooseAddressInput>? = null
    // endregion

    private var onLoginResultListener: (result: LoginResult?) -> Unit = {}
    private var onTwoPassModeResultListener: (result: TwoPassModeResult?) -> Unit = {}
    private var onSecondFactorResultListener: (result: SecondFactorResult?) -> Unit = {}
    private var onChooseAddressResultListener: (result: ChooseAddressResult?) -> Unit = {}

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

    // region private module functions
    private fun registerLoginResult(
        context: ComponentActivity
    ): ActivityResultLauncher<LoginInput> =
        context.registerForActivityResult(
            StartLogin()
        ) {
            onLoginResultListener(it)
        }

    private fun registerTwoPassModeResult(
        context: ComponentActivity
    ): ActivityResultLauncher<TwoPassModeInput> =
        context.registerForActivityResult(
            StartTwoPassMode()
        ) {
            onTwoPassModeResultListener(it)
        }

    private fun registerSecondFactorResult(
        context: ComponentActivity
    ): ActivityResultLauncher<SecondFactorInput> =
        context.registerForActivityResult(
            StartSecondFactor()
        ) {
            onSecondFactorResultListener(it)
        }

    private fun registerChooseAddressResult(
        context: ComponentActivity
    ): ActivityResultLauncher<ChooseAddressInput> =
        context.registerForActivityResult(
            StartChooseAddress()
        ) {
            onChooseAddressResultListener(it)
        }

    private fun registerHumanVerificationResult(
        context: ComponentActivity
    ): ActivityResultLauncher<HumanVerificationInput> =
        context.registerForActivityResult(
            StartHumanVerification()
        ) { result ->
            result?.let {
                context.lifecycleScope.launch {
                    if (!it.tokenType.isNullOrBlank() && !it.tokenCode.isNullOrBlank()) {
                        accountWorkflowHandler.handleHumanVerificationSuccess(
                            sessionId = SessionId(it.sessionId),
                            tokenType = it.tokenType!!,
                            tokenCode = it.tokenCode!!
                        )
                    } else {
                        accountWorkflowHandler.handleHumanVerificationFailed(
                            sessionId = SessionId(it.sessionId)
                        )
                    }
                }
            }
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
        externalEmail: String
    ) {
        checkRegistered(chooseAddressLauncher).launch(
            ChooseAddressInput(userId.id, recoveryEmail = externalEmail)
        )
    }

    private fun startHumanVerificationWorkflow(
        sessionId: SessionId,
        details: HumanVerificationDetails?
    ) {
        checkRegistered(humanWorkflowLauncher).launch(
            HumanVerificationInput(
                sessionId.id,
                details?.verificationMethods?.map { it.value },
                details?.captchaVerificationToken
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
    fun register(context: ComponentActivity) {
        loginWorkflowLauncher = registerLoginResult(context)
        humanWorkflowLauncher = registerHumanVerificationResult(context)
        secondFactorWorkflowLauncher = registerSecondFactorResult(context)
        twoPassModeWorkflowLauncher = registerTwoPassModeResult(context)
        chooseAddressLauncher = registerChooseAddressResult(context)
    }

    /**
     * Starts the Login workflow.
     *
     * @see [onLoginResult]
     */
    fun startLoginWorkflow(requiredAccountType: AccountType) {
        checkRegistered(loginWorkflowLauncher).launch(
            LoginInput(requiredAccountType)
        )
    }

    /**
     * Start a Second Factor workflow.
     *
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
        val email = checkNotNull(account.email) { "Email is null for startChooseAddressWorkflow." }
        startChooseAddressWorkflow(account.userId, email)
    }

    /**
     * Start a Human Verification workflow.
     */
    fun startHumanVerificationWorkflow(account: Account) {
        val sessionId = checkNotNull(account.sessionId) { "SessionId is null for startHumanVerificationWorkflow." }
        startHumanVerificationWorkflow(sessionId, account.details.humanVerification)
    }
    // endregion
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

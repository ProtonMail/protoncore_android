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
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.presentation.entity.ChooseAddressInput
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.NextStep
import me.proton.core.auth.presentation.entity.ScopeResult
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.SessionResult
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.ui.StartChooseAddress
import me.proton.core.auth.presentation.ui.StartLogin
import me.proton.core.auth.presentation.ui.StartSecondFactor
import me.proton.core.auth.presentation.ui.StartTwoPassMode
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.StartHumanVerification
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.UserType
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
    private var onScopeResultListener: (result: ScopeResult?) -> Unit = {}
    private var onHumanVerificationResultListener: (result: HumanVerificationResult?) -> Unit = {}

    fun setOnLoginResult(block: (result: LoginResult?) -> Unit) {
        onLoginResultListener = block
    }

    fun setOnScopeResult(block: (result: ScopeResult?) -> Unit) {
        onScopeResultListener = block
    }

    fun setOnHumanVerificationResult(block: (result: HumanVerificationResult?) -> Unit) {
        onHumanVerificationResultListener = block
    }

    // region private module functions
    private fun registerLoginResult(
        context: ComponentActivity
    ): ActivityResultLauncher<LoginInput> =
        context.registerForActivityResult(
            StartLogin()
        ) { result ->
            result?.let {
                when (it.nextStep) {
                    NextStep.SecondFactor -> startSecondFactorWorkflow(it.password, it.requiredUserType, it.session)
                    NextStep.TwoPassMode -> startTwoPassModeWorkflow(UserId(it.session.userId), it.requiredUserType)
                    NextStep.ChooseAddress -> startChooseAddressWorkflow(UserId(it.session.userId), it.session.username)
                    NextStep.None -> Unit // Nothing.
                }
            }
            onLoginResultListener(result)
        }

    private fun registerTwoPassModeResult(
        context: ComponentActivity
    ): ActivityResultLauncher<TwoPassModeInput> =
        context.registerForActivityResult(
            StartTwoPassMode()
        ) { result ->
            result?.let {
                // Nothing.
            }
        }

    private fun registerSecondFactorResult(
        context: ComponentActivity
    ): ActivityResultLauncher<SecondFactorInput> =
        context.registerForActivityResult(
            StartSecondFactor()
        ) { result ->
            result?.let {
                when (it.nextStep) {
                    NextStep.TwoPassMode -> startTwoPassModeWorkflow(UserId(it.session.userId), it.requiredUserType)
                    NextStep.ChooseAddress -> startChooseAddressWorkflow(UserId(it.session.userId), it.session.username)
                    NextStep.SecondFactor,
                    NextStep.None -> Unit // Nothing.
                }
                onScopeResultListener(it.scope)
            }
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
                onHumanVerificationResultListener(it)
            }
        }

    private fun registerChooseAddressResult(
        context: ComponentActivity
    ): ActivityResultLauncher<ChooseAddressInput> =
        context.registerForActivityResult(
            StartChooseAddress()
        ) { result ->
            result?.let { }
        }

    private fun startSecondFactorWorkflow(
        password: ByteArray,
        requiredUserType: UserType,
        session: SessionResult
    ) {
        secondFactorWorkflowLauncher?.launch(
            SecondFactorInput(password, requiredUserType, session)
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
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
     */
    fun startLoginWorkflow(requiredUserType: UserType) {
        loginWorkflowLauncher?.launch(
            LoginInput(requiredUserType)
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    /**
     * Start a TwoPassMode workflow.
     */
    fun startTwoPassModeWorkflow(userId: UserId, requiredUserType: UserType) {
        twoPassModeWorkflowLauncher?.launch(
            TwoPassModeInput(userId.id, requiredUserType)
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    /**
     * Start the Choose/Create Address workflow.
     */
    fun startChooseAddressWorkflow(userId: UserId, externalEmail: String?) {
        chooseAddressLauncher?.launch(
            ChooseAddressInput(userId.id, externalEmail)
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    /**
     * Start a Human Verification workflow.
     */
    fun startHumanVerificationWorkflow(sessionId: String, details: HumanVerificationDetails?) {
        humanWorkflowLauncher?.launch(
            HumanVerificationInput(
                sessionId,
                details?.verificationMethods?.map { it.value },
                details?.captchaVerificationToken
            )
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
    }
    // endregion
}

fun AuthOrchestrator.onScopeResult(
    block: (result: ScopeResult?) -> Unit
): AuthOrchestrator {
    setOnScopeResult { block(it) }
    return this
}

fun AuthOrchestrator.onLoginResult(
    block: (result: LoginResult?) -> Unit
): AuthOrchestrator {
    setOnLoginResult { block(it) }
    return this
}

fun AuthOrchestrator.onHumanVerificationResult(
    block: (result: HumanVerificationResult?) -> Unit
): AuthOrchestrator {
    setOnHumanVerificationResult { block(it) }
    return this
}

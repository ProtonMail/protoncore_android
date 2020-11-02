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
import me.proton.core.auth.presentation.entity.ScopeResult
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.SessionResult
import me.proton.core.auth.presentation.entity.UserResult
import me.proton.core.auth.presentation.ui.StartLogin
import me.proton.core.auth.presentation.ui.StartSecondFactor
import me.proton.core.auth.presentation.ui.StartTwoPassMode
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.StartHumanVerification
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

class AuthOrchestrator @Inject constructor(
    private val accountWorkflowHandler: AccountWorkflowHandler
) {

    // region result launchers
    private var loginWorkflowLauncher: ActivityResultLauncher<List<String>>? = null
    private var secondFactorWorkflowLauncher: ActivityResultLauncher<SecondFactorInput>? = null
    private var twoPassModeWorkflowLauncher: ActivityResultLauncher<SessionId>? = null
    private var humanWorkflowLauncher: ActivityResultLauncher<HumanVerificationInput>? = null
    // endregion

    private var onUserResultListener: (result: UserResult?) -> Unit = {}
    private var onSessionResultListener: (result: SessionResult?) -> Unit = {}
    private var onScopeResultListener: (result: ScopeResult?) -> Unit = {}
    private var onHumanVerificationResultListener: (result: HumanVerificationResult?) -> Unit = {}

    fun setOnUserResult(block: (result: UserResult?) -> Unit) {
        onUserResultListener = block
    }

    fun setOnSessionResult(block: (result: SessionResult?) -> Unit) {
        onSessionResultListener = block
    }

    fun setOnScopeResult(block: (result: ScopeResult?) -> Unit) {
        onScopeResultListener = block
    }

    fun setOnHumanVerificationResult(block: (result: HumanVerificationResult?) -> Unit) {
        onHumanVerificationResultListener = block
    }

    // region private module functions
    private fun registerLoginWorkflowLauncher(
        context: ComponentActivity
    ): ActivityResultLauncher<List<String>> =
        context.registerForActivityResult(
            StartLogin()
        ) { result ->
            result?.let {
                if (it.isSecondFactorNeeded) {
                    startSecondFactorWorkflow(SecondFactorInput(it.sessionId, it.isTwoPassModeNeeded))
                } else if (it.isTwoPassModeNeeded) {
                    startTwoPassModeWorkflow(SessionId(it.sessionId))
                }
            }
            onSessionResultListener(result)
        }

    private fun registerTwoPassModeWorkflowLauncher(
        context: ComponentActivity
    ): ActivityResultLauncher<SessionId> =
        context.registerForActivityResult(
            StartTwoPassMode()
        ) {
            onUserResultListener(it)
        }

    private fun registerSecondFactorWorkflow(
        context: ComponentActivity
    ): ActivityResultLauncher<SecondFactorInput> =
        context.registerForActivityResult(
            StartSecondFactor()
        ) { result ->
            result?.let {
                if (it.isTwoPassModeNeeded) {
                    startTwoPassModeWorkflow(SessionId(it.sessionId))
                }
            }
            onScopeResultListener(result)
        }

    private fun registerHumanVerificationWorkflow(
        context: ComponentActivity
    ): ActivityResultLauncher<HumanVerificationInput> =
        context.registerForActivityResult(
            StartHumanVerification()
        ) { result ->
            if (result != null) {
                context.lifecycleScope.launch {
                    if (!result.tokenType.isNullOrBlank() && !result.tokenCode.isNullOrBlank()) {
                        accountWorkflowHandler.handleHumanVerificationSuccess(
                            sessionId = SessionId(result.sessionId),
                            tokenType = result.tokenType!!,
                            tokenCode = result.tokenCode!!
                        )
                    } else {
                        accountWorkflowHandler.handleHumanVerificationFailed(
                            sessionId = SessionId(result.sessionId)
                        )
                    }
                }
            }
            onHumanVerificationResultListener(result)
        }

    /**
     * Start a Second Factor workflow.
     */
    private fun startSecondFactorWorkflow(input: SecondFactorInput) {
        secondFactorWorkflowLauncher?.launch(input)
            ?: throw IllegalStateException("You must call register before any start workflow function!")
    }
    // endregion

    // region public API
    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(context: ComponentActivity) {
        loginWorkflowLauncher = registerLoginWorkflowLauncher(context)
        humanWorkflowLauncher = registerHumanVerificationWorkflow(context)
        secondFactorWorkflowLauncher = registerSecondFactorWorkflow(context)
        twoPassModeWorkflowLauncher = registerTwoPassModeWorkflowLauncher(context)
    }

    /**
     * Starts the Login workflow.
     */
    fun startLoginWorkflow(requiredFeatures: List<String> = emptyList()) {
        loginWorkflowLauncher?.launch(requiredFeatures)
            ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    /**
     * Start a TwoPassMode workflow.
     */
    fun startTwoPassModeWorkflow(input: SessionId) {
        twoPassModeWorkflowLauncher?.launch(input)
            ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    /**
     * Start a Human Verification workflow.
     */
    fun startHumanVerificationWorkflow(sessionId: SessionId, details: HumanVerificationDetails?) {
        humanWorkflowLauncher?.launch(
            HumanVerificationInput(
                sessionId.id,
                details?.verificationMethods?.map { it.value },
                details?.captchaVerificationToken
            )
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
    }
    // endregion
}

fun AuthOrchestrator.onUserResult(
    block: (result: UserResult?) -> Unit
): AuthOrchestrator {
    setOnUserResult { block(it) }
    return this
}

fun AuthOrchestrator.onScopeResult(
    block: (result: ScopeResult?) -> Unit
): AuthOrchestrator {
    setOnScopeResult { block(it) }
    return this
}

fun AuthOrchestrator.onSessionResult(
    block: (result: SessionResult?) -> Unit
): AuthOrchestrator {
    setOnSessionResult { block(it) }
    return this
}

fun AuthOrchestrator.onHumanVerificationResult(
    block: (result: HumanVerificationResult?) -> Unit
): AuthOrchestrator {
    setOnHumanVerificationResult { block(it) }
    return this
}

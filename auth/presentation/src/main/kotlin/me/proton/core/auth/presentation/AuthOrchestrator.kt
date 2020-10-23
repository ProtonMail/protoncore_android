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
import me.proton.core.auth.presentation.entity.ScopeResult
import me.proton.core.auth.presentation.entity.SessionResult
import me.proton.core.auth.presentation.entity.UserResult
import me.proton.core.auth.presentation.ui.StartLogin
import me.proton.core.auth.presentation.ui.StartMailboxLogin
import me.proton.core.auth.presentation.ui.StartSecondFactor
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.StartHumanVerification
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.SessionId

class AuthOrchestrator {

    // region result launchers
    private var loginWorkflowLauncher: ActivityResultLauncher<List<String>>? = null
    private var secondFactorWorkflowLauncher: ActivityResultLauncher<SessionId>? = null
    private var mailboxWorkflowLauncher: ActivityResultLauncher<SessionId>? = null
    private var humanWorkflowLauncher: ActivityResultLauncher<HumanVerificationInput>? = null
    // endregion

    // region private module functions
    private fun registerLoginWorkflowLauncher(
        context: ComponentActivity,
        onSessionResult: (result: SessionResult?) -> Unit = {}
    ): ActivityResultLauncher<List<String>> =
        context.registerForActivityResult(StartLogin()) { result ->
            result?.let {
                if (it.isSecondFactorNeeded) {
                    startSecondFactorWorkflow(SessionId(it.sessionId))
                } else if (it.isMailboxLoginNeeded) {
                    startMailboxLoginWorkflow(SessionId(it.sessionId))
                }
                onSessionResult(it)
            }
        }

    private fun registerMailboxLoginWorkflowLauncher(
        context: ComponentActivity,
        onUserResult: (result: UserResult?) -> Unit = {}
    ): ActivityResultLauncher<SessionId> =
        context.registerForActivityResult(StartMailboxLogin()) {
            onUserResult(it)
        }

    private fun registerSecondFactorWorkflow(
        context: ComponentActivity,
        onScopeResult: (result: ScopeResult?) -> Unit = {}
    ): ActivityResultLauncher<SessionId> =
        context.registerForActivityResult(StartSecondFactor()) { result ->
            result?.let {
                if (it.isMailboxLoginNeeded) {
                    startMailboxLoginWorkflow(SessionId(it.sessionId))
                }
                onScopeResult(it)
            }
        }

    private fun registerHumanVerificationWorkflow(
        context: ComponentActivity,
        onHumanVerificationResult: (result: HumanVerificationResult?) -> Unit = {}
    ): ActivityResultLauncher<HumanVerificationInput> =
        context.registerForActivityResult(StartHumanVerification()) {
            onHumanVerificationResult(it)
        }

    /**
     * Start a Second Factor workflow.
     */
    private fun startSecondFactorWorkflow(input: SessionId) {
        secondFactorWorkflowLauncher?.launch(input)
            ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    /**
     * Start a MailboxLogin workflow.
     */
    private fun startMailboxLoginWorkflow(input: SessionId) {
        mailboxWorkflowLauncher?.launch(input)
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
        loginWorkflowLauncher ?: run { loginWorkflowLauncher = registerLoginWorkflowLauncher(context) }
        humanWorkflowLauncher ?: run { humanWorkflowLauncher = registerHumanVerificationWorkflow(context) }
        secondFactorWorkflowLauncher ?: run { secondFactorWorkflowLauncher = registerSecondFactorWorkflow(context) }
        mailboxWorkflowLauncher ?: run { mailboxWorkflowLauncher = registerMailboxLoginWorkflowLauncher(context) }
    }

    /**
     * Starts the Login workflow.
     */
    fun startLoginWorkflow(requiredFeatures: List<String> = emptyList()) {
        loginWorkflowLauncher?.launch(requiredFeatures)
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

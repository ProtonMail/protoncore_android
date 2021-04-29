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

package me.proton.core.humanverification.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.ui.StartHumanVerification
import me.proton.core.network.domain.humanverification.HumanVerificationApiDetails
import me.proton.core.network.domain.session.ClientId
import me.proton.core.network.domain.session.ClientIdType
import me.proton.core.network.domain.session.CookieSessionId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.getType
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

class HumanVerificationOrchestrator @Inject constructor(
    private val humanVerificationWorkflowHandler: HumanVerificationWorkflowHandler
) {
    // region result launchers
    private var humanWorkflowLauncher: ActivityResultLauncher<HumanVerificationInput>? = null
    // endregion

    // region private functions

    private fun registerHumanVerificationResult(
        context: ComponentActivity
    ) =
        context.registerForActivityResult(
            StartHumanVerification()
        ) { result ->
            result?.let {
                context.lifecycleScope.launch {
                    val clientId = when (ClientIdType.getByValue(it.clientIdType)) {
                        ClientIdType.SESSION -> ClientId.AccountSessionId(SessionId(it.clientId))
                        ClientIdType.COOKIE -> ClientId.NetworkCookieSessionId(CookieSessionId(it.clientId))
                    }.exhaustive
                    clientId.let { id ->
                        if (!it.tokenType.isNullOrBlank() && !it.tokenCode.isNullOrBlank()) {
                            humanVerificationWorkflowHandler.handleHumanVerificationSuccess(
                                clientId = id,
                                tokenType = it.tokenType,
                                tokenCode = it.tokenCode
                            )
                        } else {
                            if (it.canceled) {
                                humanVerificationWorkflowHandler.handleHumanVerificationFailed(clientId = id)
                                humanVerificationWorkflowHandler.handleHumanVerificationCanceled(clientId = id)
                            } else {
                                humanVerificationWorkflowHandler.handleHumanVerificationFailed(clientId = id)
                            }
                        }
                    }
                }
            }
        }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call authOrchestrator.register(context) before starting workflow!" }

    // endregion

    // region public API
    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(context: ComponentActivity) {
        humanWorkflowLauncher = registerHumanVerificationResult(context)
    }

    /**
     * Start a Human Verification workflow.
     */
    private fun startHumanVerificationWorkflow(
        sessionId: SessionId,
        details: HumanVerificationApiDetails?
    ) {
        checkRegistered(humanWorkflowLauncher).launch(
            HumanVerificationInput(
                clientId = sessionId.id,
                clientIdType = ClientIdType.SESSION.value,
                details?.verificationMethods?.map { it.value },
                details?.captchaVerificationToken
            )
        )
    }

    /**
     * Start a Human Verification workflow for signup.
     */
    fun startHumanVerificationSignUpWorkflow(
        clientId: ClientId,
        details: HumanVerificationApiDetails?,
        recoveryEmailAddress: String? = null
    ) {
        checkRegistered(humanWorkflowLauncher).launch(
            HumanVerificationInput(
                clientId = clientId.id(),
                clientIdType = clientId.getType().value,
                details?.verificationMethods?.map { it.value },
                details?.captchaVerificationToken,
                recoveryEmailAddress = recoveryEmailAddress
            )
        )
    }
    // endregion
}

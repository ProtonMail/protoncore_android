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
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.StartHumanVerification
import me.proton.core.network.domain.humanverification.HumanVerificationApiDetails
import me.proton.core.network.domain.session.ClientId
import me.proton.core.network.domain.session.getType

class HumanVerificationOrchestrator {

    // region result launchers
    private var humanWorkflowLauncher: ActivityResultLauncher<HumanVerificationInput>? = null
    // endregion

    private var onHumanVerificationResultListener: ((result: HumanVerificationResult?) -> Unit)? = {}

    // region private functions
    private fun registerHumanVerificationResult(
        context: ComponentActivity
    ): ActivityResultLauncher<HumanVerificationInput> =
        context.registerForActivityResult(
            StartHumanVerification()
        ) { result ->
            onHumanVerificationResultListener?.invoke(result)
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
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        humanWorkflowLauncher?.unregister()
        humanWorkflowLauncher = null
        onHumanVerificationResultListener = null
    }

    /**
     * Start a Human Verification workflow.
     *
     * @param captchaBaseUrl use this one if you want to provide per instance different captcha URL.
     * Otherwise the one from the DI annotated with [CaptchaBaseUrl] will be used.
     * [CaptchaBaseUrl] is only a base Url, Core is responsible to create the full URL.
     * [captchaBaseUrl] should not be only a base Url, but you are responsible to create it full, up to the
     * query params section.
     * If both provided, this parameter takes the precedence.
     */
    fun startHumanVerificationWorkflow(
        clientId: ClientId,
        captchaBaseUrl: String? = null,
        details: HumanVerificationApiDetails?,
        recoveryEmailAddress: String? = null
    ) {
        checkRegistered(humanWorkflowLauncher).launch(
            HumanVerificationInput(
                clientId = clientId.id,
                captchaBaseUrl = captchaBaseUrl,
                clientIdType = clientId.getType().value,
                verificationMethods = details?.verificationMethods?.map { it.value },
                captchaToken = details?.captchaVerificationToken,
                recoveryEmailAddress = recoveryEmailAddress
            )
        )
    }

    fun setOnHumanVerificationResult(block: (result: HumanVerificationResult?) -> Unit) {
        onHumanVerificationResultListener = block
    }
    // endregion
}

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
import androidx.fragment.app.FragmentActivity
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.humanverification.presentation.utils.showHumanVerification
import me.proton.core.network.domain.client.getType
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.presentation.ui.alert.FragmentDialogResultLauncher

class HumanVerificationOrchestrator {

    // region result launchers
    private var humanWorkflowLauncher: FragmentDialogResultLauncher<HumanVerificationInput>? = null
    // endregion

    private var onHumanVerificationResultListener: ((result: HumanVerificationResult) -> Unit)? = null

    // region private functions

    private fun <T> checkRegistered(launcher: FragmentDialogResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call register(context) before starting workflow!" }

    // endregion

    // region public API
    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(context: FragmentActivity, largeLayout: Boolean = false) {
        context.supportFragmentManager.setFragmentResultListener(
            HumanVerificationDialogFragment.REQUEST_KEY,
            context,
            { _, bundle ->
                val hvResult = bundle.getParcelable<HumanVerificationResult>(
                    HumanVerificationDialogFragment.RESULT_HUMAN_VERIFICATION
                ) ?: error("HumanVerificationDialogFragment did not return a result")
                onHumanVerificationResultListener?.invoke(hvResult)
            })
        humanWorkflowLauncher = FragmentDialogResultLauncher(HumanVerificationDialogFragment.REQUEST_KEY) { input ->
            context.supportFragmentManager.showHumanVerification(
                clientId = input.clientId,
                captchaUrl = input.captchaUrl,
                clientIdType = input.clientIdType,
                availableVerificationMethods = input.verificationMethods.orEmpty(),
                verificationToken = input.verificationToken,
                largeLayout = largeLayout,
                recoveryEmailAddress = input.recoveryEmailAddress,
                isPartOfFlow = input.isPartOfFlow,
            )
        }
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister(context: FragmentActivity) {
        context.supportFragmentManager.clearFragmentResultListener(HumanVerificationDialogFragment.REQUEST_KEY)
        onHumanVerificationResultListener = null
    }

    /**
     * Start a Human Verification workflow.
     *
     * @param captchaUrl use this one if you want to provide per instance different captcha URL.
     * Otherwise the one from the DI annotated with [HumanVerificationApiHost] will be used.
     * [HumanVerificationApiHost] is only the host, Core is responsible to create the full URL.
     * [captchaUrl] should not be only a base Url, but you are responsible to create it full, up to the
     * query params section.
     * If both provided, this parameter takes the precedence.
     */
    fun startHumanVerificationWorkflow(
        details: HumanVerificationDetails,
        captchaUrl: String? = null,
        recoveryEmailAddress: String? = null,
        isPartOfFlow: Boolean = false,
    ) {
        startHumanVerificationWorkflow(
            HumanVerificationInput(
                clientId = details.clientId.id,
                clientIdType = details.clientId.getType().value,
                verificationMethods = details.verificationMethods,
                verificationToken = requireNotNull(details.verificationToken),
                captchaUrl = captchaUrl,
                recoveryEmailAddress = recoveryEmailAddress,
                isPartOfFlow = isPartOfFlow,
            )
        )
    }

    fun startHumanVerificationWorkflow(humanVerificationInput: HumanVerificationInput) {
        checkRegistered(humanWorkflowLauncher).show(humanVerificationInput)
    }

    fun setOnHumanVerificationResult(block: (result: HumanVerificationResult) -> Unit) {
        onHumanVerificationResultListener = block
    }
    // endregion
}

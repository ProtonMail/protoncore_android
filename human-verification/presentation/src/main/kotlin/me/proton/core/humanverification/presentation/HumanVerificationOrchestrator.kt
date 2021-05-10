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
import javax.inject.Inject

class HumanVerificationOrchestrator @Inject constructor() {
    // region result launchers
    private var humanWorkflowLauncher: ActivityResultLauncher<HumanVerificationInput>? = null

    // endregion
    private var onHumanVerificationResultListener: (result: HumanVerificationResult?) -> Unit = {}

    // region private functions

    private fun registerHumanVerificationResult(
        context: ComponentActivity
    ) =
        context.registerForActivityResult(
            StartHumanVerification()
        ) { result ->
            onHumanVerificationResultListener(result)
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
     * Start a Human Verification workflow for signup.
     */
    fun startHumanVerificationWorkflow(
        clientId: ClientId,
        details: HumanVerificationApiDetails?,
        recoveryEmailAddress: String? = null
    ) {
        checkRegistered(humanWorkflowLauncher).launch(
            HumanVerificationInput(
                clientId = clientId.id,
                clientIdType = clientId.getType().value,
                details?.verificationMethods?.map { it.value },
                details?.captchaVerificationToken,
                recoveryEmailAddress = recoveryEmailAddress
            )
        )
    }

    fun setOnHumanVerificationResult(block: (result: HumanVerificationResult?) -> Unit) {
        onHumanVerificationResultListener = block
    }
    // endregion
}

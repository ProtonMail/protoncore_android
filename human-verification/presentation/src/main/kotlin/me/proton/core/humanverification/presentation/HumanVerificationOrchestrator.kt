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
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.StartHumanVerification
import me.proton.core.network.domain.client.getType
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import javax.inject.Inject

class HumanVerificationOrchestrator @Inject constructor() {

    private var humanWorkflowLauncher: ActivityResultLauncher<HumanVerificationInput>? = null

    private var onHumanVerificationResultListener: ((result: HumanVerificationResult?) -> Unit)? = {}

    fun setOnHumanVerificationResult(block: (result: HumanVerificationResult?) -> Unit) {
        onHumanVerificationResultListener = block
    }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call register before starting workflow!" }

    private fun registerHumanVerificationResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<HumanVerificationInput> =
        caller.registerForActivityResult(
            StartHumanVerification()
        ) {
            onHumanVerificationResultListener?.invoke(it)
        }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(caller: ActivityResultCaller) {
        humanWorkflowLauncher = registerHumanVerificationResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        humanWorkflowLauncher?.unregister()
        onHumanVerificationResultListener = null
    }

    /**
     * Start a Human Verification workflow.
     */
    fun startHumanVerificationWorkflow(
        details: HumanVerificationDetails
    ) {
        checkRegistered(humanWorkflowLauncher).launch(
            HumanVerificationInput(
                clientId = details.clientId.id,
                clientIdType = details.clientId.getType().value,
                verificationMethods = details.verificationMethods,
                verificationToken = requireNotNull(details.verificationToken)
            )
        )
    }
}

fun HumanVerificationOrchestrator.onHumanVerificationResult(
    block: (result: HumanVerificationResult?) -> Unit
): HumanVerificationOrchestrator {
    setOnHumanVerificationResult { block(it) }
    return this
}

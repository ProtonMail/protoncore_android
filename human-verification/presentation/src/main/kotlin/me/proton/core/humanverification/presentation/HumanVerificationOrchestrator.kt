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

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.StartHumanVerification
import me.proton.core.network.domain.client.getType
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import javax.inject.Inject

@Deprecated("Will be removed in the next major release.")
class HumanVerificationOrchestrator @Inject constructor() {

    private var humanWorkflowLauncher: ActivityResultLauncher<HumanVerificationInput>? = null

    private var onHumanVerificationResultListener: ((result: HumanVerificationResult?) -> Unit)? = {}

    @Deprecated("Will be removed in the next major release.")
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
    @Deprecated("Will be removed in the next major release.")
    fun register(caller: ActivityResultCaller) {
        humanWorkflowLauncher = registerHumanVerificationResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    @Deprecated("Will be removed in the next major release.")
    fun unregister() {
        humanWorkflowLauncher?.unregister()
        onHumanVerificationResultListener = null
    }

    /**
     * Start a Human Verification workflow.
     */
    @Deprecated("Will be removed in the next major release.")
    fun startHumanVerificationWorkflow(
        details: HumanVerificationDetails
    ) {
        checkRegistered(humanWorkflowLauncher).launch(
            details.toInput()
        )
    }

    companion object {
        /**
         * Start a Human Verification workflow.
         */
        fun startHumanVerificationWorkflow(
            details: HumanVerificationDetails,
            context: Context
        ) {
            val intent = StartHumanVerification.getIntent(context, details.toInput()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

fun HumanVerificationDetails.toInput() = HumanVerificationInput(
    clientId = clientId.id,
    clientIdType = clientId.getType().value,
    verificationMethods = verificationMethods,
    verificationToken = requireNotNull(verificationToken)
)

@Deprecated("Will be removed in the next major release.")
fun HumanVerificationOrchestrator.onHumanVerificationResult(
    block: (result: HumanVerificationResult?) -> Unit
): HumanVerificationOrchestrator {
    setOnHumanVerificationResult { block(it) }
    return this
}

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

package me.proton.core.auth.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.auth.presentation.alert.confirmpass.StartConfirmPassword
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordInput
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordResult
import me.proton.core.domain.entity.UserId

class ConfirmPasswordOrchestrator {

    // region result launchers
    private var confirmPasswordWorkflowLauncher: ActivityResultLauncher<ConfirmPasswordInput>? = null
    // endregion

    private var onConfirmPasswordResultListener: ((result: ConfirmPasswordResult?) -> Unit)? = {}

    // region public API
    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(context: ComponentActivity) {
        confirmPasswordWorkflowLauncher = registerConfirmPasswordResult(context)
    }

    fun setOnConfirmPasswordResult(block: (result: ConfirmPasswordResult?) -> Unit) {
        onConfirmPasswordResultListener = block
    }

    /**
     * Starts the Confirm Password workflow.
     */
    fun startConfirmPasswordWorkflow(showSecondFactor: Boolean = false) {
        checkRegistered(confirmPasswordWorkflowLauncher).launch(
            ConfirmPasswordInput(
                showPassword = true,
                showTwoFA = showSecondFactor,
            )
        )
    }
    // endregion

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call authOrchestrator.register(context) before starting workflow!" }

    private fun registerConfirmPasswordResult(
        context: ComponentActivity
    ): ActivityResultLauncher<ConfirmPasswordInput> =
        context.registerForActivityResult(
            StartConfirmPassword()
        ) {
            onConfirmPasswordResultListener?.invoke(it)
        }
}

fun ConfirmPasswordOrchestrator.onConfirmPasswordResult(
    block: (result: ConfirmPasswordResult?) -> Unit
): ConfirmPasswordOrchestrator {
    setOnConfirmPasswordResult { block(it) }
    return this
}

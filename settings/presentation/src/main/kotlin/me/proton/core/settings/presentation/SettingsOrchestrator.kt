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

package me.proton.core.settings.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.domain.entity.UserId
import me.proton.core.settings.presentation.entity.SettingsInput
import me.proton.core.settings.presentation.entity.UpdateRecoveryEmailResult
import me.proton.core.settings.presentation.ui.StartUpdateRecoveryEmail
import javax.inject.Inject

class SettingsOrchestrator @Inject constructor() {

    private var updateRecoveryEmailLauncher: ActivityResultLauncher<SettingsInput>? = null

    private var onUpdateRecoveryEmailResultListener: ((result: UpdateRecoveryEmailResult?) -> Unit)? = {}

    fun setOnUpgradeResult(block: (result: UpdateRecoveryEmailResult?) -> Unit) {
        onUpdateRecoveryEmailResultListener = block
    }

    private fun registerUpdateRecoveryEmailResult(
        context: ComponentActivity
    ): ActivityResultLauncher<SettingsInput> =
        context.registerForActivityResult(
            StartUpdateRecoveryEmail()
        ) {
            onUpdateRecoveryEmailResultListener?.invoke(it)
        }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(context: ComponentActivity) {
        updateRecoveryEmailLauncher = registerUpdateRecoveryEmailResult(context)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        updateRecoveryEmailLauncher?.unregister()
        updateRecoveryEmailLauncher = null
    }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call settingsOrchestrator.register(context) before starting workflow!" }

    /**
     * Starts the Plan Chooser workflow (sign up or upgrade).
     *
     * @see [onUpgradeResult]
     */
    fun startUpdateRecoveryEmailWorkflow(userId: UserId, username: String) {
        checkRegistered(updateRecoveryEmailLauncher).launch(
            SettingsInput(userId.id, username)
        )
    }
}

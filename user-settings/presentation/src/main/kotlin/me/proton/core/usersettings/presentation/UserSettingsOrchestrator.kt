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

package me.proton.core.usersettings.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.presentation.entity.SettingsInput
import me.proton.core.usersettings.presentation.entity.PasswordManagementResult
import me.proton.core.usersettings.presentation.entity.UpdateRecoveryEmailResult
import me.proton.core.usersettings.presentation.ui.StartPasswordManagement
import me.proton.core.usersettings.presentation.ui.StartUpdateRecoveryEmail
import javax.inject.Inject

class UserSettingsOrchestrator @Inject constructor() {

    private var updateRecoveryEmailLauncher: ActivityResultLauncher<SettingsInput>? = null
    private var passwordManagementLauncher: ActivityResultLauncher<SettingsInput>? = null

    private var onUpdateRecoveryEmailResultListener: ((result: UpdateRecoveryEmailResult?) -> Unit)? = {}
    private var onPasswordManagementResultListener: ((result: PasswordManagementResult?) -> Unit)? = {}

    fun setOnUpdateRecoveryEmailResult(block: (result: UpdateRecoveryEmailResult?) -> Unit) {
        onUpdateRecoveryEmailResultListener = block
    }

    fun setPasswordManagementResult(block: (result: PasswordManagementResult?) -> Unit) {
        onPasswordManagementResultListener = block
    }

    private fun registerUpdateRecoveryEmailResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<SettingsInput> =
        caller.registerForActivityResult(
            StartUpdateRecoveryEmail()
        ) {
            onUpdateRecoveryEmailResultListener?.invoke(it)
        }

    private fun registerPasswordManagementResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<SettingsInput> =
        caller.registerForActivityResult(
            StartPasswordManagement()
        ) {
            onPasswordManagementResultListener?.invoke(it)
        }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(caller: ActivityResultCaller) {
        updateRecoveryEmailLauncher = registerUpdateRecoveryEmailResult(caller)
        passwordManagementLauncher = registerPasswordManagementResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        updateRecoveryEmailLauncher?.unregister()
        updateRecoveryEmailLauncher = null
        passwordManagementLauncher?.unregister()
        passwordManagementLauncher = null
    }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call settingsOrchestrator.register(context) before starting workflow!" }

    /**
     * Starts the Recovery email workflow (part of the User Settings).
     *
     * @see [onUpdateRecoveryEmailResult]
     */
    fun startUpdateRecoveryEmailWorkflow(userId: UserId) {
        checkRegistered(updateRecoveryEmailLauncher).launch(
            SettingsInput(userId.id)
        )
    }

    /**
     * Starts the Password Management workflow (part of the User Settings).
     *
     * @see [onPasswordManagementResult]
     */
    fun startPasswordManagementWorkflow(userId: UserId) {
        checkRegistered(passwordManagementLauncher).launch(
            SettingsInput(userId.id)
        )
    }
}

fun UserSettingsOrchestrator.onUpdateRecoveryEmailResult(
    block: (result: UpdateRecoveryEmailResult?) -> Unit
): UserSettingsOrchestrator {
    setOnUpdateRecoveryEmailResult { block(it) }
    return this
}

fun UserSettingsOrchestrator.onPasswordManagementResult(
    block: (result: PasswordManagementResult?) -> Unit
): UserSettingsOrchestrator {
    setPasswordManagementResult { block(it) }
    return this
}

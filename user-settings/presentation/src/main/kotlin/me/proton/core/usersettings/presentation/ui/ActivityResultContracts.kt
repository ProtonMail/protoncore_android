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

package me.proton.core.usersettings.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import me.proton.core.auth.presentation.entity.SecondFactorProofEntity
import me.proton.core.usersettings.presentation.entity.SettingsInput
import me.proton.core.usersettings.presentation.entity.PasswordManagementResult
import me.proton.core.usersettings.presentation.entity.TwoFaDialogArguments
import me.proton.core.usersettings.presentation.entity.UpdateRecoveryEmailResult

class StartUpdateRecoveryEmail : ActivityResultContract<SettingsInput, UpdateRecoveryEmailResult?>() {
    override fun createIntent(context: Context, input: SettingsInput): Intent =
        Intent(context, UpdateRecoveryEmailActivity::class.java).apply {
            putExtra(UpdateRecoveryEmailActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): UpdateRecoveryEmailResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(UpdateRecoveryEmailActivity.ARG_RESULT)
    }
}

class StartPasswordManagement : ActivityResultContract<SettingsInput, PasswordManagementResult?>() {
    override fun createIntent(context: Context, input: SettingsInput): Intent =
        Intent(context, PasswordManagementActivity::class.java).apply {
            putExtra(PasswordManagementActivity.ARG_INPUT, input)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): PasswordManagementResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(PasswordManagementActivity.ARG_RESULT)
    }
}

class StartTwoFAInputDialog : ActivityResultContract<TwoFaDialogArguments, SecondFactorProofEntity?>() {

    override fun createIntent(context: Context, input: TwoFaDialogArguments): Intent =
        Intent(context, TwoFaInputActivity::class.java).apply {
            putExtra(TwoFaInputActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): SecondFactorProofEntity? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(TwoFaInputActivity.ARG_RESULT)
    }
}

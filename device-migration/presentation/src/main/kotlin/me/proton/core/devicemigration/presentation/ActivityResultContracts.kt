/*
 * Copyright (c) 2025 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.devicemigration.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import me.proton.core.domain.entity.UserId

public class StartDeviceMigration : ActivityResultContract<DeviceMigrationInput, DeviceMigrationOutput?>() {
    override fun createIntent(
        context: Context,
        input: DeviceMigrationInput
    ): Intent = Intent(context, DeviceMigrationActivity::class.java).apply {
        putExtra(DeviceMigrationRoutes.Arg.KEY_USER_ID, input.userId.id)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): DeviceMigrationOutput? {
        return when (resultCode) {
            Activity.RESULT_OK -> DeviceMigrationOutput.Success
            Activity.RESULT_CANCELED -> DeviceMigrationOutput.Cancelled
            else -> null
        }
    }
}

public data class DeviceMigrationInput(val userId: UserId)

public sealed interface DeviceMigrationOutput {
    public data object Success : DeviceMigrationOutput
    public data object Cancelled : DeviceMigrationOutput
}

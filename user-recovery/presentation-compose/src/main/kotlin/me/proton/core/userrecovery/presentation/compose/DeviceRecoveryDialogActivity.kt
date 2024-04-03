/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.userrecovery.presentation.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonActivity

@AndroidEntryPoint
class DeviceRecoveryDialogActivity : ProtonActivity() {

    private val input: DeviceRecoveryDialogInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val userId by lazy { UserId(input.userId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProtonTheme {
                NavHost(
                    navController = rememberNavController(),
                    startDestination = DeviceRecoveryDeeplink.Recovery.Deeplink
                ) {
                    addDeviceRecoveryDialog(
                        userId = userId,
                        onDismiss = { finish() },
                    )
                }
            }
        }
    }

    @Parcelize
    data class DeviceRecoveryDialogInput(
        val userId: String
    ) : Parcelable

    companion object {

        const val ARG_INPUT = "arg.deviceRecoveryDialogInput"

        fun start(context: Context, input: DeviceRecoveryDialogInput) =
            context.startActivity(getIntent(context, input))

        fun getIntent(context: Context, input: DeviceRecoveryDialogInput): Intent =
            Intent(context, DeviceRecoveryDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(ARG_INPUT, input)
            }
    }
}

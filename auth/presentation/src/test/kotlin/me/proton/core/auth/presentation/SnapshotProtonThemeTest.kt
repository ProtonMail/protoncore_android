/*
 * Copyright (c) 2023 Proton Technologies AG
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

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class SnapshotProtonThemeTest(deviceConfig: DeviceConfig) {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = deviceConfig,
        theme = "ProtonTheme"
    )

    @Test
    fun authHelpLayout() {
        val view = paparazzi.inflate<CoordinatorLayout>(R.layout.activity_auth_help)
        paparazzi.snapshot(view)
    }

    @Test
    fun authHelpWithQrLayout() {
        val view = paparazzi.inflate<CoordinatorLayout>(R.layout.activity_auth_help)
        view.findViewById<View>(R.id.helpOptionSignInWithQrCode).visibility = View.VISIBLE
        paparazzi.snapshot(view)
    }

    companion object {
        @Parameters
        @JvmStatic
        fun parameters() = listOf(
            DeviceConfig.PIXEL_5,
            DeviceConfig.PIXEL_5.copy(nightMode = NightMode.NIGHT)
        )
    }
}

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

package me.proton.core.presentation

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.presentation.ui.view.ProtonProgressButton
import org.junit.Rule
import org.junit.Test

class SnapshotProtonThemeTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun protonProgressButtonIdle() {
        val view = ProtonProgressButton(paparazzi.context)
        view.text = "Button"
        view.setIdle()
        paparazzi.snapshot(view)
    }

    @Test
    fun protonProgressButtonLoading() {
        val view = ProtonProgressButton(paparazzi.context)
        view.text = "Button"
        view.setLoading()
        paparazzi.snapshot(view)
    }
}

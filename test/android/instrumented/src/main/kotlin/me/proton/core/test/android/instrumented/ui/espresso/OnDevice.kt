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

package me.proton.core.test.android.instrumented.ui.espresso

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

/**
 * [OnDevice] allows you to perform actions outside the proton application using UIAutomator.
 */
class OnDevice {

    fun clickHomeBtn() = apply {
        uiDevice.pressHome()
    }

    fun clickRecentAppsBtn() = apply {
        uiDevice.pressRecentApps()
    }

    fun clickBackBtn() = apply {
        uiDevice.pressBack()
    }

    fun expandNotifications() = apply {
        uiDevice.openNotification()
    }

    fun clickNotificationByText(text: String, timeout: Long = TIMEOUT_5S) = apply {
        uiDevice.wait(Until.findObject(By.text(text)), timeout).click()
    }

    fun clickShareDialogJustOnceBtn(applicationName: String, timeout: Long = TIMEOUT_5S): OnDevice = apply {
        uiDevice.wait(Until.findObject(By.textStartsWith(applicationName)), timeout)?.click()
        uiDevice.wait(Until.findObject(By.res("android:id/button_once")), timeout).click()
    }

    fun waitForObjectByText(text: String, timeout: Long = TIMEOUT_5S) = apply {
        uiDevice.wait(Until.hasObject(By.text(text)), timeout)
    }

    companion object {
        private const val TIMEOUT_5S = 5000L
        private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
}

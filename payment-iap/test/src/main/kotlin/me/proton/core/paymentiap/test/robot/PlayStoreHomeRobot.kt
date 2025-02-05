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

package me.proton.core.paymentiap.test.robot

import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiWatcher
import me.proton.test.fusion.Fusion.byObject

/**
 * Contains code to launch PlayStore app and deal with PlayStore application home screen.
 */
@SdkSuppress(minSdkVersion = 33)
public class PlayStoreHomeRobot {

    public fun clickOnAccountButton(): PlayStoreAccountViewRobot {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageName = "com.android.vending"

        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        requireNotNull(intent) { "Google Play Store not found! Is it installed on the emulator?" }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)

        registerMeetTheSearchTabWatcher()

        byObject.withContentDescContains("Signed in as")
            .instanceOf(FrameLayout::class.java)
            .waitForExists()
            .click()
        return PlayStoreAccountViewRobot()
    }

    private val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    public fun registerMeetTheSearchTabWatcher() {
        device.registerWatcher(playStoreMeetTheSearchTabName, UiWatcher {
            val meetTheSearchTab = device.findObject(By.text("Meet the Search tab").clazz(TextView::class.java))
            if (meetTheSearchTab != null) {
                println("Handling 'Google play Meet the Search tab' pop up.")
                val searchTab = device.findObject(By.text("Search").clazz(TextView::class.java).pkg("com.android.vending"))
                if (searchTab != null) {
                    searchTab.click()
                    return@UiWatcher true
                }
            }
            false
        })
        device.runWatchers()
    }
}
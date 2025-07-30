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

import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiWatcher
import me.proton.core.test.android.instrumented.FusionConfig
import me.proton.test.fusion.Fusion.byObject
import kotlin.time.Duration.Companion.seconds

/**
 * Google Play bottom sheet robot, containing Subscribe button with additional actions.
 */
@SdkSuppress(minSdkVersion = 33)
public class GPBottomSheetSubscribeRobot {

    private val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    public fun registerPlayPointsNotNowButtonWatcher() {
        device.registerWatcher(notNowButtonUiWatcherName, UiWatcher {
            val registerButton = device.findObject(By.text("Not now").clazz(Button::class.java))
            if (registerButton != null && registerButton.isEnabled) {
                println("'Google play Points Not now' button detected! Clicking it now...")
                registerButton.click()
                return@UiWatcher true
            }
            false
        })
        device.runWatchers()
    }

    public fun registerPlayRequireAuthenticationWatcher() {
        device.registerWatcher(authRequiredScreenWatcherName, UiWatcher {
            val noThanksButton =
                device.findObject(By.text("No, thanks").clazz(RadioButton::class.java))
            if (noThanksButton != null) {
                println("'Google play authentication required screen is shown. Dealing with it.")
                noThanksButton.click()
                val okButton =
                    device.findObject(By.text("OK").clazz(Button::class.java).enabled(true))
                if (okButton != null) {
                    okButton.click()
                    return@UiWatcher true
                }
            }
            false
        })
        device.runWatchers()
    }

    public fun openPaymentMethods(): GPBottomSheetPaymentMethodsRobot {
        FusionConfig.uiAutomator.boost()
        byObject
            .withContentDescContains("Change payment method")
            .withPkg(playStorePkg)
            .isClickable()
            .instanceOf(LinearLayout::class.java)
            .waitForExists(timeout = 15.seconds)
            .checkExists() // This will fail the test if it doesn't exist.
            .click()
        return GPBottomSheetPaymentMethodsRobot()
    }

    public inline fun <reified T> clickSubscribeButton(): T {
        FusionConfig.uiAutomator.boost()
        byObject.withText("Subscribe")
            .withPkg(playStorePkg)
            .instanceOf(Button::class.java)
            .isClickable()
            .waitForExists(15.seconds)
            .checkExists() // This will fail the test if it doesn't exist.
            .click()

        // Below UIWatchers should handle one time pop-up bottom sheets:
        // PlayStore points and require PlayStore authentication.
        registerPlayPointsNotNowButtonWatcher()
        registerPlayRequireAuthenticationWatcher()

        return T::class.java.getDeclaredConstructor().newInstance()
    }
}
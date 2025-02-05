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
import android.widget.TextView
import androidx.test.filters.SdkSuppress
import me.proton.core.test.android.instrumented.FusionConfig
import me.proton.test.fusion.Fusion.byObject

/**
 * Google Play payment error bottom sheet robot, containing actions and validations.
 */
@SdkSuppress(minSdkVersion = 33)
public class GPBottomSheetSubscribeErrorRobot {

    public inline fun <reified T> clickGotIt(): T {
        FusionConfig.uiAutomator.boost()
        byObject
            .withText("Got it")
            .withPkg(playStorePkg)
            .isClickable()
            .instanceOf(Button::class.java)
            .waitForExists()
            .click()
        return T::class.java.getDeclaredConstructor().newInstance()
    }

    public fun errorMessageIsShown(): GPBottomSheetSubscribeErrorRobot {
        FusionConfig.uiAutomator.boost()
        byObject
            .withPkg(playStorePkg)
            .instanceOf(TextView::class.java)
            .withText("Error")
            .waitForExists()
            .checkExists()

        byObject
            .withPkg(playStorePkg)
            .instanceOf(TextView::class.java)
            .withText("Declined by always denied test instrument")
            .waitForExists()
            .checkExists()

        return GPBottomSheetSubscribeErrorRobot()
    }
}

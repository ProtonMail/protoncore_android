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

import android.widget.TextView
import androidx.test.filters.SdkSuppress
import me.proton.test.fusion.Fusion.byObject

@SdkSuppress(minSdkVersion = 33)
public class PlayStoreSubscriptionsRobot {

    public fun clickActiveSubscriptionByText(planName: String): PlayStoreManageSubscriptionRobot {
        byObject
            // We need a new line here as we can match 1 month plan name
            .containsText("$planName\n")
            .instanceOf(TextView::class.java)
            .withPkg(playStorePkg)
            .waitForExists()
            .click()
        return PlayStoreManageSubscriptionRobot()
    }
}
/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.test

import me.proton.core.plan.test.robot.SubscriptionRobot.verifyAtLeastOnePlanIsShown
import me.proton.core.plan.test.robot.SubscriptionRobot.verifyUpgradeIsShown
import me.proton.core.plan.test.robot.SubscriptionRobot.verifyUpgradeYourPlanTitleIsDisplayed
import me.proton.core.test.quark.Quark
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Minimal Upgrade tests (for both Static and Dynamic plans implementation).
 */
public interface MinimalUpgradeTests {

    public val quark: Quark

    public fun startUpgrade()

    @Before
    public fun setPaymentMethods() {
        quark.setPaymentMethods()
    }

    @After
    public fun resetPaymentMethods() {
        quark.setDefaultPaymentMethods()
    }

    @Test
    public fun upgradeScreenIsShownForFreeUser() {
        startUpgrade()
        verifyUpgradeIsShown()
        verifyUpgradeYourPlanTitleIsDisplayed()
        verifyAtLeastOnePlanIsShown()
    }
}

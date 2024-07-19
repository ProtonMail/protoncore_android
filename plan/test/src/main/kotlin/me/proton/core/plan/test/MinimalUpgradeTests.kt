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

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.test.fusion.FusionConfig
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Minimal Upgrade tests (for both Static and Dynamic plans implementation).
 */
@HiltAndroidTest
public interface MinimalUpgradeTests {

    public fun startUpgrade(): SubscriptionRobot

    @Before
    public fun setUpTimeouts() {
        FusionConfig.Compose.waitTimeout.set(60.seconds)
        FusionConfig.Espresso.waitTimeout.set(60.seconds)
    }

    @Test
    @PrepareUser(loginBefore = true)
    public fun upgradeScreenIsShownForFreeUser() {
        startUpgrade()
            .apply {
                upgradeYourPlanTitleIsDisplayed()
            }
    }
}

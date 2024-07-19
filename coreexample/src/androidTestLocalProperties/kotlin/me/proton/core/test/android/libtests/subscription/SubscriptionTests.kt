/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.test.android.libtests.subscription

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.plan.test.MinimalSubscriptionTests
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.rule.extension.protonActivityScenarioRule
import org.junit.Rule

@HiltAndroidTest
open class SubscriptionTests : MinimalSubscriptionTests() {
    @get:Rule
    val protonRule = protonActivityScenarioRule<MainActivity>()

    override fun startSubscription(): SubscriptionRobot {
        CoreexampleRobot().plansCurrent()
        return SubscriptionRobot
    }
}
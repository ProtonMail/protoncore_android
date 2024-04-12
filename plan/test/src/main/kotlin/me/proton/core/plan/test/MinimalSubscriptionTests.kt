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

import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.quark.data.Plan
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Minimal Subscription tests (for both Static and Dynamic plans implementation).
 */
@RunWith(Parameterized::class)
public abstract class MinimalSubscriptionTests(private val plan: Plan) {
    public abstract fun startSubscription(): SubscriptionRobot

    @Test
    public fun subscriptionIsShownForPlan() {
        startSubscription()
            .apply {
                currentPlanIsDisplayed()
            }
    }

    public companion object {
        @get:Parameterized.Parameters(name = "{0}")
        @get:JvmStatic
        public val data: Collection<Plan> = listOf(
            Plan.MailPlus,
            Plan.Unlimited
        )
    }
}

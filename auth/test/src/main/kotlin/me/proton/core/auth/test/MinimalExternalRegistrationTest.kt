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

package me.proton.core.auth.test

import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.signup.CongratsRobot
import me.proton.core.auth.test.robot.signup.SetPasswordRobot
import me.proton.core.auth.test.robot.signup.SignupExternal
import me.proton.core.auth.test.robot.signup.SignupInternal
import me.proton.core.humanverification.test.robot.HvCodeRobot
import me.proton.core.plan.test.BillingPlan
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.plan.test.robot.SubscriptionRobot.planIsDisplayed
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.util.kotlin.random
import me.proton.test.fusion.FusionConfig
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Minimal InternalRegistrationTest Payments tests using Proton email (Internal).
 * Clients tests should extend it in order to run payments tests from their repositories.
 * When running on Android emulator it should support PlayStore API.
 * Client plans should be provided using providePlans() function.
 *
 * Usage (below code should be added on client side):
 *
 * Usage (below example demonstrates usage):
 * @HiltAndroidTest
 * open class BasicExternalRegistrationWithSubscriptionFlowTest : MinimalExternalRegistrationTest {
 *
 *     override fun providePlans(): List<BillingPlan> {
 *         return listOf(
 *             DrivePlans.drivePlusMonthly,
 *             DrivePlans.drivePlusYearly,
 *             DrivePlans.driveProtonUnlimitedMonthly,
 *             DrivePlans.driveProtonUnlimitedYearly
 *         )
 *     }
 * }
 */
@SdkSuppress(minSdkVersion = 33)
public interface MinimalExternalRegistrationTest {

    @Before
    public fun setUpTimeouts() {
        FusionConfig.Compose.waitTimeout.set(60.seconds)
        FusionConfig.Espresso.waitTimeout.set(60.seconds)
        FusionConfig.UiAutomator.waitTimeout.set(60.seconds)
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("settings put secure autofill_service null")
    }

    public fun providePlans(): List<BillingPlan>

    @Test
    @EnvironmentConfig(host = "payments.proton.black")
    public fun externalRegistration(): Unit = runBlocking {
        val testEmail = "${String.random()}@example.com"

        val plans = providePlans()
        assertTrue(plans.isNotEmpty(), "BillingPlan list is empty, failing tests.")

        AddAccountRobot.clickSignUp()

        SignupExternal
            .fillUsername(testEmail)
            .clickNext()
        HvCodeRobot
            .fillCode()
            .clickVerify()

        SetPasswordRobot
            .fillAndClickNext(String.random(12))

        plans.forEach { plan ->
            planIsDisplayed(plan)
        }
    }
}

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

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.signup.SignupInternal
import me.proton.core.paymentiap.test.robot.GPBottomSheetSubscribeErrorRobot
import me.proton.core.paymentiap.test.robot.GPBottomSheetSubscribeRobot
import me.proton.core.plan.test.BillingPlan
import me.proton.core.plan.test.SubscriptionHelper
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.util.kotlin.random
import me.proton.test.fusion.Fusion.byObject
import me.proton.test.fusion.FusionConfig
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Minimal abstract Payments tests for new user registration using Proton email (Internal).
 * Clients tests should extend it in order to run payments tests from their repositories.
 * Should be run on payments test environment with PlayStore licensed test accounts.
 * When running on Android emulator it should support PlayStore API.
 * This test is parametrized and uses [BillingPlan]s as parameters.
 * Client plans should be created in advance and maintained in clients repository.
 *
 * Usage (below code should be added on client side):
 *
 * @RunWith(Parameterized::class)
 * open class InternalRegistrationWithSubscriptionFlowTest(plan: Plan) :
 *     MinimalInternalRegistrationWithSubscriptionTest(plan) {
 *
 *     override fun afterSubscriptionSteps() {
 *         CongratsRobot.uiElementsDisplayed()
 *     }
 *
 *     companion object {
 *         @JvmStatic
 *         @Parameterized.Parameters(name = "{0}")
 *         fun data(): List<Plan> {
 *             return listOf(
 *                 DrivePlans.drivePlusMonthly,
 *                 DrivePlans.drivePlusYearly,
 *                 DrivePlans.driveProtonUnlimitedMonthly,
 *                 DrivePlans.driveProtonUnlimitedYearly
 *             )
 *         }
 *     }
 * }
 */
public abstract class MinimalInternalRegistrationWithSubscriptionTest(private val billingPlan: BillingPlan) {

    public abstract fun afterSubscriptionSteps()

    @Before
    public fun setUpTimeouts() {
        FusionConfig.Compose.waitTimeout.set(60.seconds)
        FusionConfig.Espresso.waitTimeout.set(60.seconds)
        FusionConfig.UiAutomator.waitTimeout.set(60.seconds)
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("settings put secure autofill_service null")
    }

    @Test
    @EnvironmentConfig(host = "payments.proton.black")
    public fun registerUserWithPlan(): Unit = runBlocking {
        val testEmail = String.random()

        AddAccountRobot.clickSignUp()

        SignupInternal
            .clickSwitch()
            .apply {
                robotDisplayed()
            }
            .fillUsername(testEmail)
            .clickNext()
            .apply {
                uiElementsDisplayed()
            }
            .fillAndClickNext(String.random(12))
            .clickNextWithoutRecoveryMethod()
            .skipConfirm()

        SubscriptionRobot
            .selectBillingCycle(billingPlan.billingCycle)
            .selectPlan(billingPlan)

            .openPaymentMethods()
            .selectAlwaysDeclines<GPBottomSheetSubscribeRobot>()
            .clickSubscribeButton<GPBottomSheetSubscribeErrorRobot>()
            .errorMessageIsShown()
            .clickGotIt<SubscriptionRobot>()

            .selectExpandedPlan(billingPlan)
            .openPaymentMethods()
            .selectAlwaysApproves<GPBottomSheetSubscribeRobot>()
            .clickSubscribeButton<SubscriptionRobot>()

        InstrumentationRegistry.getInstrumentation().uiAutomation.waitForIdle(5_000L, 60_000L)
        byObject.withPkg(InstrumentationRegistry.getInstrumentation().targetContext.packageName)
            .waitForExists(5.seconds)

        afterSubscriptionSteps()

        SubscriptionHelper.cancelSubscription(billingPlan)
    }
}

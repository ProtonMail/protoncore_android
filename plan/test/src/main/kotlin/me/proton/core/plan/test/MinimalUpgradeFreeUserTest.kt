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
import kotlinx.coroutines.runBlocking
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.paymentiap.data.GooglePurchaseStateHandler
import me.proton.core.paymentiap.test.robot.GPBottomSheetSubscribeErrorRobot
import me.proton.core.paymentiap.test.robot.GPBottomSheetSubscribeRobot
import me.proton.core.plan.test.robot.Plan
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.test.fusion.FusionConfig
import org.junit.Before
import org.junit.Test
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
public abstract class MinimalUpgradeFreeUserTest(private val plan: Plan) {

    @Inject
    internal lateinit var purchaseManager: PurchaseManager

    @Inject
    internal lateinit var giapHandler: GooglePurchaseStateHandler

    public abstract fun startSubscription(): SubscriptionRobot

    public abstract fun afterSubscriptionSteps()

    @Before
    public fun setUpTimeouts() {
        FusionConfig.Compose.waitTimeout.set(60.seconds)
        FusionConfig.Espresso.waitTimeout.set(60.seconds)
        FusionConfig.UiAutomator.waitTimeout.set(60.seconds)
    }

    @Test
    @PrepareUser(loginBefore = true)
    public fun upgradeFreeUserToPlanFailFirstPaymentAttempt(): Unit = runBlocking {
        startSubscription()
            .selectBillingCycle(plan.billingCycle)
            .selectPlan(plan)
            .openPaymentMethods()
            .selectAlwaysDeclines<GPBottomSheetSubscribeRobot>()
            .clickSubscribeButton<GPBottomSheetSubscribeErrorRobot>()
            .errorMessageIsShown()
            .clickGotIt<SubscriptionRobot>()
            .selectExpandedPlan(plan)
            .openPaymentMethods()
            .selectAlwaysApproves<GPBottomSheetSubscribeRobot>()
            .clickSubscribeButton<SubscriptionRobot>()

        GiapHandler(giapHandler).waitForGiapSubscribed(plan)
        GiapHandler(giapHandler).waitForGiapAcknowledged(plan)
        PurchaseManagerHandler(purchaseManager).waitForPurchaseState(plan, PurchaseState.Deleted)

        afterSubscriptionSteps()
        SubscriptionHelper.cancelSubscription(plan)
    }
}

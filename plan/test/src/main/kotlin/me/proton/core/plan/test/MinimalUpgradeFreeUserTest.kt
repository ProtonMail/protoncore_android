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

import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.paymentiap.test.robot.GPBottomSheetSubscribeErrorRobot
import me.proton.core.paymentiap.test.robot.GPBottomSheetSubscribeRobot
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.test.fusion.Fusion.byObject
import me.proton.test.fusion.FusionConfig
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
@SdkSuppress(minSdkVersion = 33)
public abstract class MinimalUpgradeFreeUserTest(private val billingPlan: BillingPlan) {

    @Inject
    internal lateinit var purchaseManager: PurchaseManager

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
    @EnvironmentConfig(host = "payments.proton.black")
    public fun upgradeFreeUserToPlanFailFirstPaymentAttempt(): Unit = runBlocking {
        startSubscription()
            .selectBillingCycle(billingPlan.billingCycle)
            .selectPlan(billingPlan)
            // Error flow
            .openPaymentMethods()
            .selectAlwaysDeclines<GPBottomSheetSubscribeRobot>()
            .clickSubscribeButton<GPBottomSheetSubscribeErrorRobot>()
            .errorMessageIsShown()
            // Success flow
            .clickGotIt<SubscriptionRobot>()
            .selectExpandedPlan(billingPlan)
            .openPaymentMethods()
            .selectAlwaysApproves<GPBottomSheetSubscribeRobot>()
            .clickSubscribeButton<SubscriptionRobot>()

        PurchaseManagerHandler(purchaseManager).waitForPurchaseState(
            billingPlan,
            PurchaseState.Subscribed
        )
        PurchaseManagerHandler(purchaseManager).waitForPurchaseState(
            billingPlan,
            PurchaseState.Acknowledged
        )
        PurchaseManagerHandler(purchaseManager).waitForPurchaseState(
            billingPlan,
            PurchaseState.Deleted
        )

        InstrumentationRegistry.getInstrumentation()
            .uiAutomation.waitForIdle(5_000L, 30_000L)
        byObject.withPkg(InstrumentationRegistry.getInstrumentation().targetContext.packageName)
            .waitForExists()

        afterSubscriptionSteps()
    }

    @After
    public fun cancelPlayStoreSubscription() {
        SubscriptionHelper.cancelSubscription(billingPlan)
    }
}

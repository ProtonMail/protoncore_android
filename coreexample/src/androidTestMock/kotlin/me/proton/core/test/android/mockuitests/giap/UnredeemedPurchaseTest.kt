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

package me.proton.core.test.android.mockuitests.giap

import android.content.Context
import androidx.lifecycle.ProcessLifecycleInitializer
import androidx.startup.AppInitializer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.usecase.WaitForPrimaryAccount
import me.proton.core.plan.presentation.UnredeemedPurchaseInitializer
import me.proton.core.plan.test.robot.UnredeemedPurchaseRobot
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import me.proton.core.test.android.mocks.mockAcknowledgePurchase
import me.proton.core.test.android.mocks.mockQueryPurchasesAsync
import me.proton.core.test.android.mocks.mockStartConnection
import me.proton.core.test.android.mockuitests.SampleMockTest
import me.proton.core.test.android.robot.CoreexampleRobot
import javax.inject.Inject
import kotlin.test.Test

@HiltAndroidTest
class UnredeemedPurchaseTest : SampleMockTest() {

    @Inject
    lateinit var billingClientFactory: FakeBillingClientFactory

    @Inject
    lateinit var waitForPrimaryAccount: WaitForPrimaryAccount

    @Test
    fun happyPath() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appInitializer = AppInitializer.getInstance(context)
        appInitializer.initializeComponent(ProcessLifecycleInitializer::class.java)

        setupGiapMocks()
        setupApiMocks()

        ActivityScenario.launch(MainActivity::class.java)

        AddAccountRobot
            .clickSignIn()
            .fillUsername(testUsername)
            .fillPassword(testPassword)
            .login()

        waitForPrimaryAccount()
        appInitializer.initializeComponent(UnredeemedPurchaseInitializer::class.java)

        // WHEN
        UnredeemedPurchaseRobot
            .apply {
                robotDisplayed()
            }.clickRedeem()

        // THEN
        verify { billingClientFactory.billingClient.acknowledgePurchase(any(), any()) }
        CoreexampleRobot().verify {
            accountSwitcherDisplayed()
        }
    }

    @Test
    fun unredeemedPurchaseActivityIsClosedWhenCancelIsClicked() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appInitializer = AppInitializer.getInstance(context)
        appInitializer.initializeComponent(ProcessLifecycleInitializer::class.java)

        setupGiapMocks()
        setupApiMocks()

        ActivityScenario.launch(MainActivity::class.java)

        AddAccountRobot
            .clickSignIn()
            .fillUsername(testUsername)
            .fillPassword(testPassword)
            .login()

        waitForPrimaryAccount()
        appInitializer.initializeComponent(UnredeemedPurchaseInitializer::class.java)

        // WHEN
        UnredeemedPurchaseRobot.clickCancel()

        // THEN
        verify(exactly = 0) { billingClientFactory.billingClient.acknowledgePurchase(any(), any()) }
        CoreexampleRobot().verify {
            accountSwitcherDisplayed()
        }
    }

    private fun setupGiapMocks() {
        billingClientFactory.billingClient.mockStartConnection(BillingClient.BillingResponseCode.OK)
        val purchase = mockk<Purchase> {
            every { accountIdentifiers } returns mockk {
                every { obfuscatedAccountId } returns "cus_google_QTqe7W-dkfX09qtIy7Mb"
            }
            every { isAcknowledged } returns false // unacknowledged
            every { orderId } returns "order-id"
            every { packageName } returns "package-name"
            every { purchaseTime } returns 0
            every { products } returns listOf("giappass_pass2023_12_renewing")
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { purchaseToken } returns "google-purchase-token"
        }
        billingClientFactory.billingClient.mockQueryPurchasesAsync(
            BillingClient.BillingResponseCode.OK,
            listOf(purchase)
        )
        billingClientFactory.billingClient.mockAcknowledgePurchase(
            BillingClient.BillingResponseCode.OK
        )
    }

    private fun setupApiMocks() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-iap-only.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v5/subscription",
            "GET/payments/v5/subscription-free.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-keys-not-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-pass-plus-google-managed.json"
        )
    }
}

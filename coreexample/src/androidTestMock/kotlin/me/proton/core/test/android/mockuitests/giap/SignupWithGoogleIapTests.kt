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
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.Purchase
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import me.proton.core.auth.presentation.ui.AddAccountActivity
import me.proton.core.auth.test.robot.signup.CongratsRobot
import me.proton.core.paymentiap.test.robot.GoogleIAPRobot
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import me.proton.core.test.android.mocks.mockBillingClientSuccess
import me.proton.core.test.android.mocks.mockQueryPurchasesAsync
import me.proton.core.test.android.mockuitests.SampleMockTest
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.quark.data.Card
import javax.inject.Inject
import kotlin.test.Test
import me.proton.core.test.quark.data.Plan as TestPlan

@HiltAndroidTest
class SignupWithGoogleIapTests : SampleMockTest() {

    @Inject
    lateinit var billingClientFactory: FakeBillingClientFactory

    private val appContext: Context get() = ApplicationProvider.getApplicationContext()
    private val billingClient: BillingClient get() = billingClientFactory.billingClient

    // TODO: add check for Google Billing dialog display
//    @Test
    fun signUpAndSubscribeGiapOnly() {
        billingClientFactory.mockBillingClientSuccess()

        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )

        dispatcher.mockFromAssets(
            "GET", "/core/v4/domains/available",
            "GET/core/v4/domains/available.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-iap-only.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-keys-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )

        ActivityScenario.launch(AddAccountActivity::class.java)
        AddAccountRobot()
            .createAccount()
            .chooseInternalEmail()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<SelectPlanRobot>()
            .toggleExpandPlan(TestPlan.MailPlus)
            .selectPlan<GoogleIAPRobot>(TestPlan.MailPlus)
            .apply {
                verify<GoogleIAPRobot.Verify> {
                    payWithGoogleButtonIsClickable()
                    payWithCardButtonIsNotVisible()
                    switchPaymentProviderButtonIsNotVisible()
                }
            }
            .payWithGPay<CoreRobot>()

        CongratsRobot
            .uiElementsDisplayed()
    }

    // TODO: add check for Google Billing dialog display
//    @Test
    fun signUpAndSubscribeAllPaymentProviders() {
        billingClient.mockBillingClientSuccess { billingClientFactory.listeners }

        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )

        dispatcher.mockFromAssets(
            "GET", "/core/v4/domains/available",
            "GET/core/v4/domains/available.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-keys-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )

        ActivityScenario.launch(AddAccountActivity::class.java)
        AddAccountRobot()
            .createAccount()
            .chooseInternalEmail()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<SelectPlanRobot>()
            .toggleExpandPlan(TestPlan.MailPlus)
            .selectPlan<AddCreditCardRobot>(TestPlan.MailPlus)
            .apply {
                verify<AddCreditCardRobot.Verify> {
                    payWithCardIsClickable()
                    payWithGoogleButtonIsNotVisible()
                    switchPaymentProviderButtonIsVisible()
                }
            }
            .payWithCreditCard<CoreRobot>(Card.default)

        CongratsRobot
            .uiElementsDisplayed()
    }

    @Test
    fun createFreeAccountWithoutPaymentProviders() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/domains/available",
            "GET/core/v4/domains/available.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-all-disabled.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-keys-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )

        ActivityScenario.launch(AddAccountActivity::class.java)
        AddAccountRobot()
            .createAccount()
            .chooseInternalEmail()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<CoreRobot>()

        CongratsRobot
            .uiElementsDisplayed()
    }

    // TODO: add check for Google Billing dialog display
//    @Test
    fun signUpAndSubscribeUnredeemedGiap() {
        billingClientFactory.mockBillingClientSuccess()
        billingClientFactory.billingClient.mockQueryPurchasesAsync(BillingResponseCode.OK, listOf(
            mockk {
                every { accountIdentifiers } returns mockk {
                    // Note: the `obfuscatedAccountId` is different than
                    // the `customerId` that is returned from `payments/v4/plans.json`.
                    every { obfuscatedAccountId } returns "cus_google_1"
                }
                every { purchaseState } returns Purchase.PurchaseState.PURCHASED
                every { isAcknowledged } returns false // <- Note: unacknowledged
                every { orderId } returns "order-id"
                every { products } returns listOf("googlemail_mail2022_12_renewing")
                every { purchaseTime } returns 0
                every { purchaseToken } returns "google-purchase-token"
            }
        ))

        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )

        dispatcher.mockFromAssets(
            "GET", "/core/v4/domains/available",
            "GET/core/v4/domains/available.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-iap-only.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-keys-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )

        ActivityScenario.launch(AddAccountActivity::class.java)
        AddAccountRobot()
            .createAccount()
            .chooseInternalEmail()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<SelectPlanRobot>()
            .toggleExpandPlan(TestPlan.MailPlus)
            .selectPlan<GoogleIAPRobot>(TestPlan.MailPlus)
            .apply {
                verify<GoogleIAPRobot.Verify> {
                    payWithGoogleButtonIsClickable()
                    payWithCardButtonIsNotVisible()
                    switchPaymentProviderButtonIsNotVisible()
                }
            }
            .payWithGPay<GoogleIAPRobot>()
            .redeemExistingPurchase<CoreRobot>()

        CongratsRobot
            .uiElementsDisplayed()
    }
}

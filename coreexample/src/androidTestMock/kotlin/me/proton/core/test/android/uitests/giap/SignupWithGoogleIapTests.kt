/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.test.android.uitests.giap

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.auth.presentation.ui.AddAccountActivity
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.ui.StartPlanChooser
import me.proton.core.plan.presentation.ui.UpgradeActivity
import me.proton.core.test.android.TestWebServerDispatcher
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import me.proton.core.test.android.mocks.mockBillingClientSuccess
import me.proton.core.test.android.mocks.mockStartConnection
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.auth.signup.SignupFinishedRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robots.payments.GoogleIAPRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.BaseMockTest
import me.proton.core.test.android.uitests.MockTestRule
import okhttp3.HttpUrl
import org.junit.Rule
import javax.inject.Inject
import kotlin.test.Test
import me.proton.core.paymentiap.presentation.R as PaymentIapR
import me.proton.core.test.android.plugins.data.Plan as TestPlan

@HiltAndroidTest
class SignupWithGoogleIapTests : BaseMockTest {
    @get:Rule
    val mockTestRule = MockTestRule(this)

    @BindValue
    @BaseProtonApiUrl
    override lateinit var baseProtonApiUrl: HttpUrl

    @Inject
    lateinit var billingClientFactory: FakeBillingClientFactory

    private val appContext: Context get() = ApplicationProvider.getApplicationContext()
    private val billingClient: BillingClient get() = billingClientFactory.billingClient
    private val dispatcher: TestWebServerDispatcher get() = mockTestRule.dispatcher

    @Test
    fun googleBillingNotAvailable() {
        billingClient.mockStartConnection(BillingResponseCode.BILLING_UNAVAILABLE)

        val intent = StartPlanChooser().createIntent(appContext, PlanInput())
        ActivityScenario.launch<UpgradeActivity>(intent)

        SelectPlanRobot()
            .toggleExpandPlan(TestPlan.Plus)
            .selectPlan<GoogleIAPRobot>(TestPlan.Plus)
            .verify {
                errorSnackbarDisplayed(PaymentIapR.string.payments_iap_error_billing_client_unavailable)
            }
    }

    @Test
    fun signUpAndSubscribeGiapOnly() {
        billingClientFactory.mockBillingClientSuccess()

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-iap-only.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/users",
            "GET/users-with-keys-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/addresses",
            "GET/addresses-with-keys.json"
        )

        ActivityScenario.launch(AddAccountActivity::class.java)
        AddAccountRobot()
            .createAccount()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<SelectPlanRobot>()
            .toggleExpandPlan(TestPlan.Plus)
            .selectPlan<GoogleIAPRobot>(TestPlan.Plus)
            .apply {
                verify<GoogleIAPRobot.Verify> {
                    payWithGoogleButtonIsClickable()
                    payWithCardButtonIsNotVisible()
                    switchPaymentProviderButtonIsNotVisible()
                }
            }
            .payWithGPay<SignupFinishedRobot>()
            .verify {
                signupFinishedDisplayed()
            }
    }

    @Test
    fun signUpAndSubscribeAllPaymentProviders() {
        billingClient.mockBillingClientSuccess { billingClientFactory.listeners }

        dispatcher.mockFromAssets(
            "GET", "/users",
            "GET/users-with-keys-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/addresses",
            "GET/addresses-with-keys.json"
        )

        ActivityScenario.launch(AddAccountActivity::class.java)
        AddAccountRobot()
            .createAccount()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<SelectPlanRobot>()
            .toggleExpandPlan(TestPlan.Plus)
            .selectPlan<GoogleIAPRobot>(TestPlan.Plus)
            .apply {
                verify<GoogleIAPRobot.Verify> {
                    payWithGoogleButtonIsClickable()
                    payWithCardButtonIsNotVisible()
                    switchPaymentProviderButtonIsVisible()
                }
            }
            .payWithGPay<SignupFinishedRobot>()
            .verify {
                signupFinishedDisplayed()
            }
    }

    @Test
    fun switchPaymentOptions() {
        billingClient.mockBillingClientSuccess { billingClientFactory.listeners }

        val intent = StartPlanChooser().createIntent(appContext, PlanInput())
        ActivityScenario.launch<UpgradeActivity>(intent)

        SelectPlanRobot()
            .toggleExpandPlan(TestPlan.Plus)
            .selectPlan<GoogleIAPRobot>(TestPlan.Plus)
            .apply { verify { googleIAPElementsDisplayed() } }
            .switchPaymentProvider<AddCreditCardRobot>()
            .apply { verify { addCreditCardElementsDisplayed() } }
            .switchPaymentProvider<GoogleIAPRobot>()
            .apply { verify { googleIAPElementsDisplayed() } }
            .back<SelectPlanRobot>()
            .verify {
                planDetailsDisplayed(TestPlan.Plus)
                canSelectPlan(TestPlan.Plus)
            }
    }

    @Test
    fun createFreeAccountWithoutPaymentProviders() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-all-disabled.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/users",
            "GET/users-with-keys-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/addresses",
            "GET/addresses-with-keys.json"
        )

        ActivityScenario.launch(AddAccountActivity::class.java)
        AddAccountRobot()
            .createAccount()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<SignupFinishedRobot>()
            .verify {
                signupFinishedDisplayed()
            }
    }
}

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

package me.proton.core.test.android.mockuitests.giap

import androidx.test.core.app.ActivityScenario
import com.android.billingclient.api.BillingClient
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.paymentiap.test.robot.GoogleIAPRobot
import me.proton.core.test.android.TestWebServerDispatcher
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import me.proton.core.test.android.mocks.mockBillingClientSuccess
import me.proton.core.test.android.mockuitests.BaseMockTest
import me.proton.core.test.android.mockuitests.MockTestRule
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.quark.data.Card
import okhttp3.HttpUrl
import org.junit.Rule
import javax.inject.Inject
import kotlin.test.Test
import me.proton.core.test.quark.data.Plan as TestPlan

@HiltAndroidTest
class GiapUpgradeTests : BaseMockTest {
    @get:Rule
    val mockTestRule = MockTestRule(this)

    @BindValue
    @BaseProtonApiUrl
    override lateinit var baseProtonApiUrl: HttpUrl

    @Inject
    lateinit var billingClientFactory: FakeBillingClientFactory

    private val billingClient: BillingClient get() = billingClientFactory.billingClient

    private val dispatcher: TestWebServerDispatcher get() = mockTestRule.dispatcher

    // TODO: add check for Google Billing dialog display
//    @Test
    fun freeUserUpgradeAllProvidersAvailable() {
        billingClientFactory.mockBillingClientSuccess()

        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-none.json", 422
        )

        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-keys-not-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/organizations",
            "GET/core/v4/organizations-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-mail-plus-proton-managed.json"
        )

        ActivityScenario.launch(MainActivity::class.java)

        AddAccountRobot()
            .signIn()
            .username(testUsername)
            .password(testPassword)
            .signIn<CoreexampleRobot>()

        CoreexampleRobot()
            .plansUpgrade()
            .toggleExpandPlan(TestPlan.PassPlus)
            .selectPlan<AddCreditCardRobot>("Get " + TestPlan.PassPlus.text)
            .apply {
                verify {
                    addCreditCardElementsDisplayed()
                }
            }
            .payWithCreditCard<CoreexampleRobot>(Card.default)
            .plansCurrent()
            .verify {
                currentPlanDetailsDisplayed()
                planDetailsDisplayed(TestPlan.PassPlus)
            }
    }

    // TODO: add check for Google Billing dialog display
//    @Test
    fun freeUserUpgradeOnlyGiapAvailable() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-iap-only.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-keys-not-subscribed.json"
        )
        freeUserCanUpgradeGIAP()
    }

    @Test
    fun freeUserWithCreditsUpgradeOnlyGiapAvailable() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-iap-only.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-saved-credits.json"
        )
        billingClientFactory.mockBillingClientSuccess()

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-none.json", 422
        )

        dispatcher.mockFromAssets(
            "GET", "/core/v4/organizations",
            "GET/core/v4/organizations-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-mail-plus-proton-managed.json"
        )

        ActivityScenario.launch(MainActivity::class.java)

        AddAccountRobot()
            .signIn()
            .username(testUsername)
            .password(testPassword)
            .signIn<CoreexampleRobot>()

        CoreexampleRobot()
            .verify {
                plansUpgradeDisabled()
            }
    }

    // TODO: add check for Google Billing dialog display
//    @Test
    fun freeUserWithCreditsCurrentPlansOnlyGiapAvailable() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-iap-only.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-saved-credits.json"
        )
        billingClientFactory.mockBillingClientSuccess()

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-none.json", 422
        )

        dispatcher.mockFromAssets(
            "GET", "/core/v4/organizations",
            "GET/core/v4/organizations-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-mail-plus-proton-managed.json"
        )

        ActivityScenario.launch(MainActivity::class.java)

        AddAccountRobot()
            .signIn()
            .username(testUsername)
            .password(testPassword)
            .signIn<CoreexampleRobot>()

        CoreexampleRobot()
            .plansCurrent()
            .verify {
                currentPlanDetailsDisplayed()
                planDetailsDisplayed(TestPlan.Free)
                plansNotDisplayed()
            }
    }

    // TODO: add check for Google Billing dialog display
//    @Test
    fun freeUserWithCreditsCurrentPlansCardAndGiapAvailable() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-iap-and-card.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-saved-credits.json"
        )
        billingClientFactory.mockBillingClientSuccess()

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-none.json", 422
        )

        dispatcher.mockFromAssets(
            "GET", "/core/v4/organizations",
            "GET/core/v4/organizations-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-mail-plus-proton-managed.json"
        )

        ActivityScenario.launch(MainActivity::class.java)

        AddAccountRobot()
            .signIn()
            .username(testUsername)
            .password(testPassword)
            .signIn<CoreexampleRobot>()

        CoreexampleRobot()
            .plansCurrent()
            .apply {
                verify {
                    currentPlanDetailsDisplayed()
                    planDetailsDisplayed(TestPlan.Free)
                    plansDisplayed()
                }
            }
            .toggleExpandPlan(TestPlan.MailPlus)
            .selectPlan<AddCreditCardRobot>(TestPlan.MailPlus)
            .apply {
                verify {
                    addCreditCardElementsDisplayed()
                    nextPaymentProviderButtonNotDisplayed()
                }
            }
    }

    @Test
    fun freeUserSubscriptionScreenNoPlansNoPaymentOptionsAvailable() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-all-disabled.json"
        )

        billingClient.mockBillingClientSuccess { billingClientFactory.listeners }

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-none.json", 422
        )

        dispatcher.mockFromAssets(
            "GET", "/core/v4/users",
            "GET/core/v4/users-with-keys-not-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/organizations",
            "GET/core/v4/organizations-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )

        ActivityScenario.launch(MainActivity::class.java)

        AddAccountRobot()
            .signIn()
            .username(testUsername)
            .password(testPassword)
            .signIn<CoreexampleRobot>()

        CoreexampleRobot()
            .plansCurrent()
            .verify {
                currentPlanDetailsDisplayed()
                plansNotDisplayed()
            }
    }

    private fun freeUserCanUpgradeGIAP() {
        billingClientFactory.mockBillingClientSuccess()

        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-none.json", 422
        )

        dispatcher.mockFromAssets(
            "GET", "/core/v4/organizations",
            "GET/core/v4/organizations-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/core/v4/addresses",
            "GET/core/v4/addresses-with-keys.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-google-managed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-mail-plus-proton-managed.json"
        )

        ActivityScenario.launch(MainActivity::class.java)

        AddAccountRobot()
            .signIn()
            .username(testUsername)
            .password(testPassword)
            .signIn<CoreexampleRobot>()

        CoreexampleRobot()
            .plansUpgrade()
            .toggleExpandPlan(TestPlan.MailPlus)
            .selectPlan<GoogleIAPRobot>(TestPlan.MailPlus)
            .apply {
                verify<GoogleIAPRobot.Verify> {
                    googleIAPElementsDisplayed()
                    payWithGoogleButtonIsClickable()
                }
            }
            .payWithGPay<CoreexampleRobot>()
            .plansCurrent()
            .verify {
                currentPlanDetailsDisplayed()
                planDetailsDisplayed(TestPlan.MailPlus)
            }
    }
}
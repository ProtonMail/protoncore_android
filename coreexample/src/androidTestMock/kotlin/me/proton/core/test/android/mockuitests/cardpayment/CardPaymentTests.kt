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

package me.proton.core.test.android.mockuitests.cardpayment

import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.auth.presentation.ui.AddAccountActivity
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.payment.presentation.R
import me.proton.core.test.android.TestWebServerDispatcher
import me.proton.core.test.android.instrumented.utils.StringUtils
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import me.proton.core.test.android.mocks.mockBillingClientSuccess
import me.proton.core.test.quark.data.Card
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.auth.signup.SignupFinishedRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robots.payments.ExistingPaymentMethodsRobot
import me.proton.core.test.android.robots.payments.GoogleIAPRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.mockuitests.BaseMockTest
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.mockuitests.MockTestRule
import okhttp3.HttpUrl
import org.junit.Rule
import javax.inject.Inject
import kotlin.test.Test

@HiltAndroidTest
class CardPaymentTests : BaseMockTest {
    @get:Rule
    val mockTestRule = MockTestRule(this)

    @BindValue
    @BaseProtonApiUrl
    override lateinit var baseProtonApiUrl: HttpUrl

    @Inject
    lateinit var billingClientFactory: FakeBillingClientFactory

    private val dispatcher: TestWebServerDispatcher get() = mockTestRule.dispatcher

    @Test
    fun signupWithCreditCardOnly() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-card-only.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-proton-managed.json"
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
            .chooseInternalEmail()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<SelectPlanRobot>()
            .toggleExpandPlan(Plan.MailPlus)
            .selectPlan<AddCreditCardRobot>(Plan.MailPlus)
            .apply {
                verify {
                    billingDetailsDisplayed(Plan.MailPlus, "CHF", false)
                }
            }
            .payWithCreditCard<SignupFinishedRobot>(Card.default)
            .verify {
                signupFinishedDisplayed()
            }
    }

    @Test
    fun signupAndPayWithCreditCardWhenAllProvidersAvailable() {
        billingClientFactory.mockBillingClientSuccess()
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
            .chooseInternalEmail()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<SelectPlanRobot>()
            .toggleExpandPlan(Plan.MailPlus)
            .selectPlan<GoogleIAPRobot>(Plan.MailPlus)
            .apply {
                verify<GoogleIAPRobot.Verify> {
                    payWithGoogleButtonIsClickable()
                    switchPaymentProviderButtonIsVisible()
                }
            }
            .switchPaymentProvider<AddCreditCardRobot>()
            .apply {
                verify {
                    billingDetailsDisplayed(Plan.MailPlus, "CHF", false)
                }
            }
            .payWithCreditCard<SignupFinishedRobot>(Card.default)
            .verify {
                signupFinishedDisplayed()
            }
    }

    @Test
    fun upgradeWithExistingCreditCard() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-card-only.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/methods",
            "GET/payments/v4/methods-card.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/users",
            "GET/users-with-keys-not-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/organizations",
            "GET/organizations-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/addresses",
            "GET/addresses-with-keys.json"
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
            .toggleExpandPlan(Plan.MailPlus)
            .selectPlan<ExistingPaymentMethodsRobot>(Plan.MailPlus)
            .apply {
                verify {
                    val expectedTitle = StringUtils.stringFromResource(
                        R.string.payment_cc_list_item,
                        "MyCompany", "1234", "01", "2053"
                    )
                    paymentMethodDisplayed(expectedTitle, "Jane Doe")
                }
            }
            .pay<CoreexampleRobot>()
            .plansCurrent()
            .verify {
                currentPlanDetailsDisplayed()
                planDetailsDisplayed(Plan.MailPlus)
            }
    }

    @Test
    fun upgradeWithCreditCardOnly() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-card-only.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/users",
            "GET/users-with-keys-not-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/organizations",
            "GET/organizations-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/addresses",
            "GET/addresses-with-keys.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-proton-managed.json"
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
            .toggleExpandPlan(Plan.MailPlus)
            .selectPlan<AddCreditCardRobot>(Plan.MailPlus)
            .apply {
                verify {
                    billingDetailsDisplayed(Plan.MailPlus, "CHF", false)
                }
            }
            .payWithCreditCard<CoreexampleRobot>(Card.default)
            .plansCurrent()
            .verify {
                currentPlanDetailsDisplayed()
                planDetailsDisplayed(Plan.MailPlus)
            }
    }

    @Test
    fun upgradeWithCreditCardWhenAllProvidersAvailable() {
        billingClientFactory.mockBillingClientSuccess()
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/subscription",
            "GET/payments/v4/subscription-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/users",
            "GET/users-with-keys-not-subscribed.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/organizations",
            "GET/organizations-none.json", 422
        )
        dispatcher.mockFromAssets(
            "GET", "/addresses",
            "GET/addresses-with-keys.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-proton-managed.json"
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
            .toggleExpandPlan(Plan.MailPlus)
            .selectPlan<GoogleIAPRobot>(Plan.MailPlus)
            .switchPaymentProvider<AddCreditCardRobot>()
            .apply {
                verify {
                    billingDetailsDisplayed(Plan.MailPlus, "CHF", false)
                }
            }
            .payWithCreditCard<CoreexampleRobot>(Card.default)
            .plansCurrent()
            .verify {
                currentPlanDetailsDisplayed()
                planDetailsDisplayed(Plan.MailPlus)
            }
    }
}

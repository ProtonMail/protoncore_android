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
import dagger.Binds
import dagger.Module
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.auth.presentation.ui.AddAccountActivity
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.dagger.CorePlansFeaturesModule
import me.proton.core.plan.domain.IsDynamicPlanEnabled
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import me.proton.core.test.android.mocks.mockBillingClientSuccess
import me.proton.core.test.android.mockuitests.SampleMockTest
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.quark.data.Card
import me.proton.core.test.quark.data.Plan
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [CorePlansFeaturesModule::class])
interface CorePlansFeaturesModuleTestModule {

    @Binds
    @Singleton
    fun provideIsDynamicPlanEnabled(impl: CardPaymentTests.IsDynamicPlanEnabledTest): IsDynamicPlanEnabled
}

@HiltAndroidTest
class CardPaymentTests : SampleMockTest() {

    @Inject
    lateinit var billingClientFactory: FakeBillingClientFactory

    class IsDynamicPlanEnabledTest @Inject constructor() : IsDynamicPlanEnabled {
        override fun invoke(userId: UserId?): Boolean = true

        override fun isLocalEnabled(): Boolean = true

        override fun isRemoteEnabled(userId: UserId?): Boolean = true
    }

    @Test
    fun signupWithCreditCardOnly() {
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
            "GET/payments/v4/status/google-card-only.json"
        )
        dispatcher.mockFromAssets(
            "POST", "/payments/v4/subscription",
            "POST/payments/v4/subscription-mail-plus-proton-managed.json"
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
            .toggleExpandPlan(Plan.PassPlus)
            .selectPlan<AddCreditCardRobot>("Get " + Plan.PassPlus.text)
            .apply {
                verify {
                    billingDetailsDisplayed(Plan.PassPlus, "$", false)
                }
            }
    }

    @Test
    fun upgradeWithCreditCardOnly() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v5/plans",
            "GET/payments/v5/dynamic-plans.json"
        )
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-card-only.json"
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
            .toggleExpandPlan(Plan.PassPlus)
            .selectPlan<AddCreditCardRobot>("Get " + Plan.PassPlus.text)
            .apply {
                verify {
                    billingDetailsDisplayed(Plan.PassPlus, "CHF", false)
                }
            }
            .payWithCreditCard<CoreexampleRobot>(Card.default)
            .plansCurrent()
            .verify {
                currentPlanDetailsDisplayed()
                planDetailsDisplayed(Plan.PassPlus)
            }
    }

    // TODO: add check for Google Billing dialog display
//    @Test
    fun upgradeWithCreditCardWhenAllProvidersAvailable() {
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
            .toggleExpandPlan(Plan.PassPlus)
            .selectPlan<AddCreditCardRobot>(Plan.PassPlus)
            .apply {
                verify {
                    billingDetailsDisplayed(Plan.PassPlus, "$", false)
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

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
import me.proton.core.test.android.TestWebServerDispatcher
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import me.proton.core.test.android.mocks.mockBillingClientSuccess
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.payments.GoogleIAPRobot
import me.proton.core.test.android.mockuitests.BaseMockTest
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.mockuitests.MockTestRule
import okhttp3.HttpUrl
import org.junit.Rule
import javax.inject.Inject
import kotlin.test.Test
import me.proton.core.test.quark.data.Plan as TestPlan

@HiltAndroidTest
class GiapUpgradeTests: BaseMockTest {
    @get:Rule
    val mockTestRule = MockTestRule(this)

    @BindValue
    @BaseProtonApiUrl
    override lateinit var baseProtonApiUrl: HttpUrl

    @Inject
    lateinit var billingClientFactory: FakeBillingClientFactory

    private val billingClient: BillingClient get() = billingClientFactory.billingClient

    private val dispatcher: TestWebServerDispatcher get() = mockTestRule.dispatcher

    @Test
    fun freeUserUpgradeAllProvidersAvailable() {
        freeUserCanUpgrade()
    }

    @Test
    fun freeUserUpgradeOnlyGiapAvailable() {
        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-iap-only.json"
        )
        freeUserCanUpgrade()
    }

    @Test
    fun freeUserSubscriptionScreenNoPlansNoPaymentOptionsAvailable() {
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

    private fun freeUserCanUpgrade() {
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
            .toggleExpandPlan(TestPlan.Plus)
            .selectPlan<GoogleIAPRobot>(TestPlan.Plus)
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
                planDetailsDisplayed(TestPlan.Plus)
            }
    }
}
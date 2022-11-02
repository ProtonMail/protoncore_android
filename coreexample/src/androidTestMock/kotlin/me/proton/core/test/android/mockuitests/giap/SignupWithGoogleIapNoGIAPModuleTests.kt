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

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.BillingClient
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import me.proton.android.core.coreexample.CoreExampleLogger
import me.proton.core.auth.presentation.ui.AddAccountActivity
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.payment.data.ProtonIAPBillingLibraryImpl
import me.proton.core.test.android.TestWebServerDispatcher
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import me.proton.core.test.android.mocks.mockBillingClientSuccess
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.auth.signup.SignupFinishedRobot
import me.proton.core.test.android.robots.payments.GoogleIAPRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import timber.log.Timber
import javax.inject.Inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@HiltAndroidTest
class SignupWithGoogleIapNoGIAPModuleTests {
    private val testUsername = "test-mock-936"
    private val testPassword = "password"

    @get:Rule(order = Rule.DEFAULT_ORDER - 1)
    val hiltAndroidRule = HiltAndroidRule(this)

    @BindValue
    @BaseProtonApiUrl
    lateinit var baseProtonApiUrl: HttpUrl

    @Inject
    lateinit var billingClientFactory: FakeBillingClientFactory

    private val appContext: Context get() = ApplicationProvider.getApplicationContext()
    private val billingClient: BillingClient get() = billingClientFactory.billingClient
    private lateinit var dispatcher: TestWebServerDispatcher
    private lateinit var webServer: MockWebServer

    @BeforeTest
    fun setUp() {
        Timber.plant(Timber.DebugTree())
        CoreLogger.set(CoreExampleLogger())

        dispatcher = TestWebServerDispatcher()
        webServer = MockWebServer().apply {
            dispatcher = this@SignupWithGoogleIapNoGIAPModuleTests.dispatcher
        }
        baseProtonApiUrl = webServer.url("/")

        hiltAndroidRule.inject()
    }

    @AfterTest
    fun tearDown() {
        webServer.shutdown()
    }

    @BindValue
    val protonIAPBillingLibrary: ProtonIAPBillingLibraryImpl = mockk {
        every { isAvailable() } returns false
    }

    @Test
    fun signUpAndSubscribeNoPaymentProvidersGIAPModuleUnavailable() {
        billingClient.mockBillingClientSuccess { billingClientFactory.listeners }

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-all-disabled.json"
        )
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
            .skipConfirm<SignupFinishedRobot>()
            .verify {
                signupFinishedDisplayed()
            }
    }

    @Test
    fun signUpAndSubscribeAllPaymentProvidersGIAPModuleUnavailable() {
        billingClient.mockBillingClientSuccess { billingClientFactory.listeners }

        dispatcher.mockFromAssets(
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google.json"
        )
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
            .toggleExpandPlan(Plan.Plus)
            .selectPlan<GoogleIAPRobot>(Plan.Plus)
            .verify { addCreditCardElementsDisplayed() }
    }
}

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
import io.mockk.every
import io.mockk.mockk
import me.proton.core.auth.presentation.ui.AddAccountActivity
import me.proton.core.auth.test.robot.signup.CongratsRobot
import me.proton.core.payment.data.ProtonIAPBillingLibraryImpl
import me.proton.core.paymentiap.test.robot.GoogleIAPRobot
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import me.proton.core.test.android.mocks.mockBillingClientSuccess
import me.proton.core.test.android.mockuitests.SampleMockTest
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.quark.data.Plan
import javax.inject.Inject
import kotlin.test.Test

@HiltAndroidTest
class SignupWithGoogleIapNoGIAPModuleTests : SampleMockTest() {

    @Inject
    lateinit var billingClientFactory: FakeBillingClientFactory

    private val billingClient: BillingClient get() = billingClientFactory.billingClient

    @BindValue
    val protonIAPBillingLibrary: ProtonIAPBillingLibraryImpl = mockk {
        every { isAvailable() } returns false
    }

    @Test
    fun signUpAndSubscribeNoPaymentProvidersGIAPModuleUnavailable() {
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
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google-all-disabled.json"
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
            .skipConfirm<CoreRobot>()

        CongratsRobot
            .uiElementsDisplayed()
    }

    // TODO: the price should be read from the device
//    @Test
    fun signUpAndSubscribeAllPaymentProvidersGIAPModuleUnavailable() {
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
            "GET", "/payments/v4/status/google",
            "GET/payments/v4/status/google.json"
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
            .chooseExternalEmail()
            .switchSignupType()
            .setUsername(testUsername)
            .setAndConfirmPassword<RecoveryMethodsRobot>(testPassword)
            .skip()
            .skipConfirm<SelectPlanRobot>()
            .toggleExpandPlan(Plan.MailPlus)
            .selectPlan<GoogleIAPRobot>(Plan.MailPlus)
            .verify { addCreditCardElementsDisplayed() }
    }
}

/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.test.android.uitests.tests.medium.plans

import me.proton.android.core.coreexample.MainActivity
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.signup.ChooseInternalAddressRobot
import me.proton.core.auth.test.robot.signup.RecoveryMethodRobot
import me.proton.core.auth.test.robot.signup.SetPasswordRobot
import me.proton.core.domain.entity.AppStore
import me.proton.core.humanverification.test.robot.HvCodeRobot
import me.proton.core.paymentiap.test.robot.GoogleIAPRobot
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.uitests.tests.SmokeTest
import me.proton.core.test.quark.data.Plan.Free
import me.proton.core.test.quark.data.Plan.MailPlus
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.annotation.payments.TestPaymentMethods
import me.proton.core.test.rule.annotation.payments.annotationTestData
import me.proton.core.test.rule.extension.protonActivityScenarioRule
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("Outdated")
class DynamicSelectPlanTests {

    @get:Rule
    val protonRule = protonActivityScenarioRule<MainActivity>(
        setOf(
            TestPaymentMethods(
                AppStore.GooglePlay,
                card = true,
                paypal = false,
                inApp = false
            ).annotationTestData
        )
    )

    @Before
    fun goToPlanSelection() {
        AddAccountRobot
            .clickSignUp()

        ChooseInternalAddressRobot
            .apply {
                domainInputDisplayed()
                continueButtonIsEnabled()
                usernameInputIsEmpty()
            }
            .fillUsername(TestUserData.randomUsername())
            .next()

        SetPasswordRobot
            .fillAndClickNext("password")

        RecoveryMethodRobot
            .skip()
            .skipConfirm()
    }

    @Test
    fun selectFreeAndCancelHumanVerification() {
        SubscriptionRobot.selectPlan(Free)

        HvCodeRobot
            .apply {
                waitForWebView()
            }
            .close()

        SubscriptionRobot.verifyAtLeastOnePlanIsShown()
    }

    @Test
    @SmokeTest
    fun selectPlusAndCancelPayment() {
        SubscriptionRobot.selectPlan(MailPlus)
        AddCreditCardRobot()
            .apply {
                verify<AddCreditCardRobot.Verify> {
                    nextPaymentProviderButtonNotDisplayed()
                    addCreditCardElementsDisplayed()
                }
            }
            .close<CoreRobot>()
        SubscriptionRobot.verifyAtLeastOnePlanIsShown()
    }

    @Test
    @SmokeTest
    @TestPaymentMethods(AppStore.GooglePlay, card = false, paypal = false, inApp = true)
    fun selectPlusAndCancelPaymentIAPOnly() {
        SubscriptionRobot.selectPlan(MailPlus)
        GoogleIAPRobot()
            .apply {
                verify<GoogleIAPRobot.Verify> {
                    googleIAPElementsDisplayed()
                    nextPaymentProviderButtonIsNotVisible()
                }
            }
            .close<CoreRobot>()
        SubscriptionRobot.verifyAtLeastOnePlanIsShown()
    }

    @Test
    @SmokeTest
    @TestPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
    fun selectPlusAndCancelPaymentIAPAndCard() {
        SubscriptionRobot.selectPlan(MailPlus)
        AddCreditCardRobot()
            .apply {
                verify<AddCreditCardRobot.Verify> { nextPaymentProviderButtonDisplayed() }
            }
            .switchPaymentProvider<GoogleIAPRobot>()
        GoogleIAPRobot()
            .apply {
                verify<GoogleIAPRobot.Verify> { googleIAPElementsDisplayed() }
            }
            .close<CoreRobot>()
        SubscriptionRobot.verifyAtLeastOnePlanIsShown()
    }

    @Test
    @SmokeTest
    @TestPaymentMethods(AppStore.GooglePlay, card = false, paypal = false, inApp = false)
    fun selectPlusNoPaymentProvidersAvailable() {
        SubscriptionRobot.selectPlan(MailPlus)
        SubscriptionRobot.verifyAtLeastOnePlanIsShown()
    }
}

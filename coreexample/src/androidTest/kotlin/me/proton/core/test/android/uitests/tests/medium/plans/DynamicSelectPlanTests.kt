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

package me.proton.core.test.android.uitests.tests.medium.plans

import me.proton.core.domain.entity.AppStore
import me.proton.core.paymentiap.test.robot.GoogleIAPRobot
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.ChooseUsernameRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.humanverification.HVRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import me.proton.core.test.quark.data.Plan.Dev
import me.proton.core.test.quark.data.Plan.Free
import me.proton.core.test.quark.data.User
import org.junit.After
import org.junit.Before
import org.junit.Test

class DynamicSelectPlanTests : BaseTest() {
    @After
    fun setDefaults() {
        quark.setDefaultPaymentMethods()
    }

    @Before
    fun goToPlanSelection() {
        quark.jailUnban()

        AddAccountRobot()
            .createAccount()
            .chooseInternalEmail()
            .verify { domainInputDisplayed() }

        val user = User()
        ChooseUsernameRobot()
            .username(user.name)
            .next()
            .setAndConfirmPassword<RecoveryMethodsRobot>(user.password)
            .skip()
            .skipConfirm<CoreRobot>()
    }

    @Test
    fun verifyInitialState() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)

        SubscriptionRobot.verifyAtLeastOnePlanIsShown()
    }

    @Test
    fun selectFreeAndCancelHumanVerification() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)

        SubscriptionRobot.selectPlan(Free)
        HVRobot().apply {
            verify { hvElementsDisplayed() }
        }.close<CoreRobot>()
        SubscriptionRobot.verifyAtLeastOnePlanIsShown()
    }

    @Test
    @SmokeTest
    fun selectPlusAndCancelPayment() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)

        SubscriptionRobot.selectPlan(Dev)
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
    fun selectPlusAndCancelPaymentIAPOnly() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = false, paypal = false, inApp = true)

        SubscriptionRobot.selectPlan(Dev)
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
    fun selectPlusAndCancelPaymentIAPAndCard() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)

        SubscriptionRobot.selectPlan(Dev)
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
    fun selectPlusNoPaymentProvidersAvailable() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = false, paypal = false, inApp = false)
        SubscriptionRobot.selectPlan(Dev)
        SubscriptionRobot.verifyAtLeastOnePlanIsShown()
    }
}

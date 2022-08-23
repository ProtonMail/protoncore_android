/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.test.android.uitests.tests.medium.auth.signup

import me.proton.core.domain.entity.AppStore
import me.proton.core.test.android.plugins.data.Plan.Dev
import me.proton.core.test.android.plugins.data.Plan.Free
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.ChooseUsernameRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.humanverification.HumanVerificationRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robots.payments.GoogleIAPRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class SelectPlanTests : BaseTest() {

    private var humanVerificationRobot: HumanVerificationRobot? = null
    private var selectPlanRobot: SelectPlanRobot? = null

    @After
    fun setDefaults() {
        quark.setDefaultPaymentMethods()
    }

    @Before
    fun goToPlanSelection() {
        quark.jailUnban()
        val user = User()
        AddAccountRobot()
            .createAccount()
            .verify { domainInputDisplayed() }

        val skipRecoveryRobot = ChooseUsernameRobot()
            .username(user.name)
            .next()
            .setAndConfirmPassword<RecoveryMethodsRobot>(user.password)
            .skip()

        if (paymentProvidersForSignup().isNotEmpty()) {
            selectPlanRobot = skipRecoveryRobot.skipConfirm<SelectPlanRobot>()
        } else {
            humanVerificationRobot = skipRecoveryRobot.skipConfirm<HumanVerificationRobot>()
        }
    }

    @Test
    fun verifyInitialState() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)
        humanVerificationRobot?.let {
            it.verify { hvElementsDisplayed() }
        }

        selectPlanRobot?.let {
            it
                .toggleExpandPlan(Free)
                .verify { planDetailsDisplayedInsideRecyclerView(Free) }
            it
                .toggleExpandPlan(Dev)
                .verify { canSelectPlan(Dev) }
        }
    }

    @Test
    fun selectFreeAndCancelHumanVerification() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)
        humanVerificationRobot?.let {
            it.verify { hvElementsDisplayed() }
        }

        selectPlanRobot?.let {
            it.scrollToPlan(Free)
                .toggleExpandPlan(Free)
                .selectPlan<HumanVerificationRobot>(Free)
                .verify {
                    hvElementsDisplayed()
                }

            HumanVerificationRobot()
                .close<SelectPlanRobot>()
                .verify {
                    planDetailsDisplayedInsideRecyclerView(Free)
                    errorSnackbarDisplayed("Human verification required")
                }
        }
    }

    @Test
    @SmokeTest
    fun selectPlusAndCancelPayment() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)
        humanVerificationRobot?.let {
            it.verify { hvElementsDisplayed() }
        }

        selectPlanRobot?.let {
            it.toggleExpandPlan(Dev)
                .selectPlan<AddCreditCardRobot>(Dev)
                .verify { addCreditCardElementsDisplayed() }

            AddCreditCardRobot()
                .close<SelectPlanRobot>()
                .toggleExpandPlan(Free)
                .verify { planDetailsDisplayedInsideRecyclerView(Free) }
        }
    }

    @Test
    @SmokeTest
    fun selectPlusAndCancelPaymentIAPOnly() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = false, paypal = false, inApp = true)
        humanVerificationRobot?.let {
            it.verify { hvElementsDisplayed() }
        }

        selectPlanRobot?.let {
            it.toggleExpandPlan(Dev)
                .selectPlan<GoogleIAPRobot>(Dev)
                .verify { googleIAPElementsDisplayed() }

            GoogleIAPRobot()
                .close<SelectPlanRobot>()
                .toggleExpandPlan(Free)
                .verify { planDetailsDisplayedInsideRecyclerView(Free) }
        }
    }

    @Test
    @SmokeTest
    fun selectPlusAndCancelPaymentIAPAndCard() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        humanVerificationRobot?.let {
            it.verify { hvElementsDisplayed() }
        }

        selectPlanRobot?.let {
            it.toggleExpandPlan(Dev)
                .selectPlan<GoogleIAPRobot>(Dev)
                .verify { googleIAPElementsDisplayed() }

            GoogleIAPRobot()
                .close<SelectPlanRobot>()
                .toggleExpandPlan(Free)
                .verify { planDetailsDisplayedInsideRecyclerView(Free) }
        }
    }

    @Test
    @SmokeTest
    fun selectPlusNoPaymentProvidersAvailable() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = false, paypal = false, inApp = false)
        humanVerificationRobot?.let {
            it.verify { hvElementsDisplayed() }
        }
    }
}

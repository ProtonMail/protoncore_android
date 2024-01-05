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

package me.proton.core.test.android.uitests.tests.large.auth

import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.auth.test.robot.signup.CongratsRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.humanverification.HVRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.quark.data.Card
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.quark.data.User
import me.proton.core.util.kotlin.random
import org.junit.Test

class SignupTests : BaseTest(defaultTimeout = 60_000L) {
    @Test
    fun signupFreeWithCaptchaAndRecoveryEmail() {
        val user = User(recoveryEmail = "${String.random()}@proton.wtf")
        val recoveryMethodsRobot = AddAccountRobot()
            .createAccount()
            .chooseInternalEmail()
            .apply { verify { nextButtonEnabled() } }
            .setUsername(user.name)
            .setAndConfirmPassword<RecoveryMethodsRobot>(user.password)
            .email(user.recoveryEmail)

        val hvRobot: HVRobot = if (paymentProvidersForSignup().isNotEmpty()) {
            recoveryMethodsRobot.next<SelectPlanRobot>().selectPlan(user.plan)
        } else {
            recoveryMethodsRobot.next()
        }

        hvRobot
            .captcha()
            .iAmHuman(CoreexampleRobot::class.java)
        CongratsRobot.apply {
            uiElementsDisplayed()
            clickStart()
        }
        CoreexampleRobot()
            .apply { verify { userStateIs(user, Ready, Authenticated) } }
            .settingsRecoveryEmail()
            .verify {
                recoveryEmailElementsDisplayed()
                currentRecoveryEmailIs(user.recoveryEmail)
            }
    }

    @Test
    fun signupPlusWithCreditCard() {
        val user = User(plan = Plan.Dev)
        val skipRecoveryRobot = AddAccountRobot()
            .createAccount()
            .chooseInternalEmail()
            .apply { verify { nextButtonEnabled() } }
            .setUsername(user.name)
            .setAndConfirmPassword<RecoveryMethodsRobot>(user.password)
            .skip()

        if (paymentProvidersForSignup().isNotEmpty()) {
            skipRecoveryRobot.skipConfirm<SelectPlanRobot>()
                .selectPlan<AddCreditCardRobot>(user.plan)
                .payWithCreditCard<CoreexampleRobot>(Card.default)
                .verify { userStateIs(user, Ready, Authenticated) }
        } else {
            skipRecoveryRobot.skipConfirm<HVRobot>().verify {
                hvElementsDisplayed()
            }
        }
    }
}

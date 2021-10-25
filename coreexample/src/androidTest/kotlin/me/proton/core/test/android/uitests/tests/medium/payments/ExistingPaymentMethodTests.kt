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

package me.proton.core.test.android.uitests.tests.medium.payments

import me.proton.core.test.android.uitests.tests.SmokeTest
import me.proton.core.test.android.plugins.data.Card
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.login.LoginRobot
import me.proton.core.test.android.robots.payments.ExistingPaymentMethodsRobot
import me.proton.core.test.android.robots.payments.ExistingPaymentMethodsRobot.PaymentMethodElement.paymentMethod
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ExistingPaymentMethodTests : BaseTest() {

    private val loginRobot = LoginRobot()

    companion object {
        val userWithCard: User = quark.seedUserWithCreditCard()
    }

    @Before
    fun goToLogin() {
        quark.jailUnban()
        AddAccountRobot().signIn()
    }

    private fun upgradeUserToPlan(user: User, plan: Plan = Plan.Dev): ExistingPaymentMethodsRobot =
        loginRobot
            .loginUser<CoreexampleRobot>(user)
            .plansUpgrade()
            .selectPlan(plan)


    @Test
    @Ignore("Requires user with paypal account linked")
    fun existingPaypalMethodDisplayed() {
        val user = users.getUser { it.paypal.isNotEmpty() }

        upgradeUserToPlan(user)
            .verify { paymentMethodDisplayed("PayPal", user.paypal) }
    }

    @Test
    @SmokeTest
    fun existingCreditCardMethodDisplayed() {
        upgradeUserToPlan(userWithCard)
            .verify { paymentMethodDisplayed(Card.default.details, Card.default.name) }
    }

    @Test
    @Ignore("Requires user with paypal account linked")
    fun existingCreditCardAndPayPalDisplayed() {
        val user = users.getUser { it.paypal.isNotEmpty() && it.cards.isNotEmpty() && !it.isPaid }
        val card = user.cards[0]

        upgradeUserToPlan(user)
            .verify {
                paymentMethodDisplayed(card.details, card.name)
                paymentMethodDisplayed("PayPal", user.paypal)
            }
    }

    @Test
    @Ignore("Requires user with paypal account linked")
    fun switchPaymentMethod() {
        val user = users.getUser { it.paypal.isNotEmpty() && it.cards.isNotEmpty() && !it.isPaid }

        upgradeUserToPlan(user)
            .selectPaymentMethod(user.cards[0].details)
            .verify {
                paymentMethod(user.paypal).checkIsNotChecked()
                paymentMethod(user.cards[0].details).checkIsChecked()
            }

        ExistingPaymentMethodsRobot()
            .selectPaymentMethod(user.paypal)
            .verify {
                paymentMethod(user.paypal).checkIsChecked()
                paymentMethod(user.cards[0].details).checkIsNotChecked()
            }
    }
}

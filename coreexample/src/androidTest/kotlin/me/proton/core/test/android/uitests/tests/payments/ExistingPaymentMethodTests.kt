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

package me.proton.core.test.android.uitests.tests.payments

import me.proton.core.test.android.plugins.Requests.jailUnban
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.login.LoginRobot
import me.proton.core.test.android.robots.payments.ExistingPaymentMethodsRobot
import me.proton.core.test.android.robots.payments.ExistingPaymentMethodsRobot.PaymentMethodElement.paymentMethod
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class ExistingPaymentMethodTests : BaseTest() {

    private val loginRobot = LoginRobot()

    @Before
    fun goToLogin() {
        jailUnban()
        AddAccountRobot().signIn()
    }

    @Test
    fun existingPaypalMethodDisplayed() {
        val userWithPaypal = users.getUser { it.paypal.isNotEmpty() }
        loginRobot
            .loginUser<CoreexampleRobot>(userWithPaypal)
            .upgradePrimary<ExistingPaymentMethodsRobot>()
            .verify { paymentMethodDisplayed("PayPal", userWithPaypal.paypal) }
    }

    @Test
    fun existingCreditCardMethodDisplayed() {
        val userWithCreditCard = users.getUser { it.cards.isNotEmpty() }
        val card = userWithCreditCard.cards[0]
        loginRobot
            .loginUser<CoreexampleRobot>(userWithCreditCard)
            .upgradePrimary<ExistingPaymentMethodsRobot>()
            .verify { paymentMethodDisplayed(card.details(), card.name) }
    }

    @Test
    fun existingCreditCardAndPayPalDisplayed() {
        val userWithCreditCardAndPaypal = users.getUser { it.paypal.isNotEmpty() && it.cards.isNotEmpty() }
        val card = userWithCreditCardAndPaypal.cards[0]
        loginRobot
            .loginUser<CoreexampleRobot>(userWithCreditCardAndPaypal)
            .upgradePrimary<ExistingPaymentMethodsRobot>()
            .verify {
                paymentMethodDisplayed(card.details(), card.name)
                paymentMethodDisplayed("PayPal", userWithCreditCardAndPaypal.paypal)
            }
    }

    @Test
    fun switchPaymentMethod() {
        val userWithCreditCardAndPaypal = users.getUser { it.paypal.isNotEmpty() && it.cards.isNotEmpty() }
        loginRobot
            .loginUser<CoreexampleRobot>(userWithCreditCardAndPaypal)
            .upgradePrimary<ExistingPaymentMethodsRobot>()
            .selectPaymentMethod(userWithCreditCardAndPaypal.paypal)
            .verify {
                paymentMethod(userWithCreditCardAndPaypal.paypal).checkEnabled()
                paymentMethod(userWithCreditCardAndPaypal.cards[0].details()).checkDisabled()
            }
    }
}

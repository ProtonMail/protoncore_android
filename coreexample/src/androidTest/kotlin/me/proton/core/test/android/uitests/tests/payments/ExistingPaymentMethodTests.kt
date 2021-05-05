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

import me.proton.core.payment.domain.entity.PaymentMethodType.CARD
import me.proton.core.payment.domain.entity.PaymentMethodType.PAYPAL
import me.proton.core.test.android.instrumented.data.User.Users.getUser
import me.proton.core.test.android.instrumented.robots.login.LoginRobot
import me.proton.core.test.android.instrumented.robots.payments.ExistingPaymentMethodsRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Test

class ExistingPaymentMethodTests : BaseTest() {

    private val loginRobot = LoginRobot()

    @Test
    fun existingPaypalMethodDisplayed() {
        val userWithPaypal = getUser { it.hasPaymentMethodType(PAYPAL) }
        loginRobot
            .loginUser<CoreexampleRobot>(userWithPaypal)
            .upgradePrimary<ExistingPaymentMethodsRobot>()
            .verify { paymentMethodIs("PayPal", "buyer@protonmail.com") }
    }

    @Test
    fun existingCreditCardMethodDisplayed() {
        val userWithCreditCard = getUser { it.hasPaymentMethodType(CARD) }
        loginRobot
            .loginUser<CoreexampleRobot>(userWithCreditCard)
            .upgradePrimary<ExistingPaymentMethodsRobot>()
            .verify { paymentMethodIs("Visa - 4242 (Exp 12/2034)", "Sunny Crooks") }
    }

    @Test
    fun existingCreditCardAndPayPalDisplayed() {
        val userWithCreditCard = getUser { it.hasPaymentMethodType(CARD) && it.hasPaymentMethodType(PAYPAL) }
        loginRobot
            .loginUser<CoreexampleRobot>(userWithCreditCard)
            .upgradePrimary<ExistingPaymentMethodsRobot>()
            .verify {
                paymentMethodIs("PayPal", "buyer@protonmail.com")
                paymentMethodIs("Visa - 4242 (Exp 12/2034)", "Mackenzie VonRueden")
            }
    }
}

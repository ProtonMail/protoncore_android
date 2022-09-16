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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.proton.core.domain.entity.AppStore
import me.proton.core.test.android.plugins.data.Card
import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.robots.payments.ExistingPaymentMethodsRobot
import me.proton.core.test.android.robots.payments.ExistingPaymentMethodsRobot.PaymentMethodElement.paymentMethod
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import me.proton.core.payment.presentation.R

@RunWith(Parameterized::class)
class ExistingPaymentMethodTests(
    private val appStore: AppStore,
    private val card: Boolean,
    private val paypal: Boolean,
    private val inApp: Boolean
) : BaseTest() {

    @Before
    fun setPaymentMethods() {
        quark.setPaymentMethods(appStore, card, paypal, inApp)
    }

    @After
    fun setDefaults() {
        quark.setDefaultPaymentMethods()
    }

    @Test
    @Ignore("Requires user with paypal account linked")
    fun existingPaypalMethodDisplayed() {
        login(userWithPaypal)

        CoreexampleRobot()
            .plansUpgrade()
            .upgradeToPlan<ExistingPaymentMethodsRobot>(Plan.Dev)
            .verify { paymentMethodDisplayed("PayPal", userWithPaypal.paypal) }
    }

    @Test
    fun existingCreditCardMethodDisplayed() {
        login(userWithCard)

        CoreexampleRobot()
            .plansUpgrade()
            .upgradeToPlan<ExistingPaymentMethodsRobot>(Plan.Dev)
            .verify {
                paymentMethodDisplayed(Card.default.details, Card.default.name)
                if (inApp) {
                    val context = ApplicationProvider.getApplicationContext<Context>()
                    googlePaymentMethodDisplayed("${context.getString(R.string.payments_method_google)}*")
                }
            }
    }

    @Test
    @Ignore("Requires user with paypal account linked")
    fun existingCreditCardAndPayPalDisplayed() {
        val user = users.getUser { it.paypal.isNotEmpty() && it.cards.isNotEmpty() && !it.isPaid }
        val card = user.cards[0]

        login(user)

        CoreexampleRobot()
            .plansUpgrade()
            .upgradeToPlan<ExistingPaymentMethodsRobot>(Plan.Dev)
            .verify {
                paymentMethodDisplayed(card.details, card.name)
                paymentMethodDisplayed("PayPal", user.paypal)
            }
    }

    @Test
    @Ignore("Requires user with paypal account linked")
    fun switchPaymentMethod() {
        val user = users.getUser { it.paypal.isNotEmpty() && it.cards.isNotEmpty() && !it.isPaid }

        CoreexampleRobot()
            .plansUpgrade()
            .upgradeToPlan<ExistingPaymentMethodsRobot>(Plan.Dev)
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

    companion object {
        val userWithCard by lazy { quark.seedUserWithCreditCard() }
        val userWithPaypal by lazy { users.getUser { it.paypal.isNotEmpty() } }

        @get:Parameterized.Parameters(name = "appStore={0}, card={1}, paypal={2}, inApp={3}")
        @get:JvmStatic
        val data = listOf(
            //      appStore,            card, paypal, inApp
            arrayOf(AppStore.GooglePlay, true, true, true),
            arrayOf(AppStore.GooglePlay, true, true, false),
            arrayOf(AppStore.GooglePlay, true, false, true),
            arrayOf(AppStore.GooglePlay, true, false, false)
        ).toList()
    }
}

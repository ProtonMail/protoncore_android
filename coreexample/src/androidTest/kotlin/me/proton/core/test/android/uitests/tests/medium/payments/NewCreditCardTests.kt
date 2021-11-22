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

import me.proton.core.payment.presentation.R
import me.proton.core.test.android.plugins.data.BillingCycle
import me.proton.core.test.android.plugins.data.Currency
import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class NewCreditCardTests : BaseTest() {
    private val newCreditCardRobot = AddCreditCardRobot()

    companion object {
        val userWithoutCard: User = quark.userCreate()
    }

    @Before
    fun goToPlanUpgrade() {

        login(userWithoutCard)

        CoreexampleRobot()
            .plansUpgrade()
            .changeCurrency(Currency.CHF)
            .upgradeToPlan<AddCreditCardRobot>(Plan.Dev)
            .verify { billingDetailsDisplayed(Plan.Dev, BillingCycle.Yearly, Currency.CHF.symbol) }
    }

    @Test
    fun backToPlanSelection() {
        newCreditCardRobot
            .close<SelectPlanRobot>()
            .verify { planDetailsDisplayed(Plan.Dev) }
    }

    @Test
    fun missingPaymentDetails() {
        newCreditCardRobot
            .pay<AddCreditCardRobot>()
            .verify {
                inputErrorDisplayed(R.string.payments_error_card_name)
                inputErrorDisplayed(R.string.payments_error_card_number)
                inputErrorDisplayed(R.string.payments_error_cvc)
            }
    }

    @Test
    fun invalidCreditCardNumber() {
        newCreditCardRobot
            .ccname("Test name")
            .country()
            .selectCountry<AddCreditCardRobot>("Angola")
            .expirationDate("1234")
            .cvc("123")
            .ccnumber("424242424242424")
            .postalCode("1234")
            .pay<AddCreditCardRobot>()
            .verify {
                errorSnackbarDisplayed("Your card has been declined. Please try a different card or contact your bank to authorize the charge")
            }
    }
}

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

package me.proton.core.test.android.uitests.tests.medium.payments

import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.presentation.R
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.quark.data.User
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.After
import org.junit.Ignore
import org.junit.Test

@Ignore("Replaced by DynamicNewCreditCardTests")
class NewCreditCardTests : BaseTest() {

    private val newCreditCardRobot = AddCreditCardRobot()

    @After
    fun setDefaults() {
        quark.setDefaultPaymentMethods()
    }

    private fun goToPlanUpgrade() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)

        login(userWithoutCard)

        CoreexampleRobot()
            .plansCurrent()
            .upgradeToPlan<AddCreditCardRobot>(Plan.Dev)
    }

    @Test
    fun backToPlanSelection() {
        goToPlanUpgrade()
        newCreditCardRobot
            .close<SelectPlanRobot>()
            .verify { planDetailsDisplayedInsideRecyclerView(Plan.Dev) }
    }

    @Test
    fun missingPaymentDetails() {
        goToPlanUpgrade()
        newCreditCardRobot
            .pay<AddCreditCardRobot>()
            .verify {
                inputErrorDisplayed(R.string.payments_error_card_name, scroll = true)
                inputErrorDisplayed(R.string.payments_error_card_number, scroll = true)
                inputErrorDisplayed(R.string.payments_error_cvc, scroll = true)
            }
    }

    @Test
    fun invalidCreditCardNumber() {
        goToPlanUpgrade()
        newCreditCardRobot
            .ccname("Test name")
            .country()
            .selectCountry<AddCreditCardRobot>("Angola")
            .expirationDate("1234")
            .cvc("123")
            .ccnumber("424242424242424")
            .postalCode("1234")
            .pay<AddCreditCardRobot>()
            .verify { errorSnackbarDisplayed("Your card has been declined") }
    }

    companion object {
        val userWithoutCard: User = quark.userCreate().first
    }
}

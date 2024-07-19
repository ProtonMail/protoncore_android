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

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.presentation.R
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.quark.data.User
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class DynamicNewCreditCardTests : BaseTest() {

    private val newCreditCardRobot = AddCreditCardRobot()

    @After
    fun setDefaults() {
        quark.setDefaultPaymentMethods()
    }

    private fun goToPlanUpgrade() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)

        login(userWithoutCard)

        CoreexampleRobot().plansCurrent()
        SubscriptionRobot.selectPlan(Plan.Dev)
    }

    @Test
    fun backToPlanSelection() {
        goToPlanUpgrade()
        newCreditCardRobot.close<CoreexampleRobot>()
        SubscriptionRobot.verifyAtLeastOnePlanIsShown()
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

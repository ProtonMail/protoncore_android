/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.test.android.robots.payments

import me.proton.core.payment.presentation.R
import me.proton.core.test.android.plugins.data.BillingCycle
import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

/**
 * [PaymentRobot] base class for payments actions and verifications implementation
 */
open class PaymentRobot : CoreRobot() {

    /**
     * Clicks 'pay' button
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> pay(): T = clickElement(R.id.payButton)

    class Verify : CoreVerify() {
        fun billingDetailsDisplayed(
            plan: Plan,
            billingCycle: BillingCycle,
            currency: String
        ) {
            val yearlyPriceString = String.format("%.2f", billingCycle.yearlyPrice)
            view.withId(R.id.planNameText).withText(plan.text).checkDisplayed()
            view.withId(R.id.billingPeriodText).withText("Billed ${billingCycle.toString().lowercase()}").checkDisplayed()
            view.withId(R.id.amountText).withText("$currency$yearlyPriceString").checkDisplayed()
        }

        fun paymentMethodDisplayed(title: String, details: String) {
            view.withText(title).withId(R.id.paymentMethodTitleText).checkDisplayed()
            view.withText(details).withId(R.id.paymentMethodSubtitleText).checkDisplayed()
        }

        fun addCreditCardElementsDisplayed() {
            view.withId(R.id.scrollContent).closeKeyboard()
            arrayOf(
                R.id.cardNameInput,
                R.id.cardNumberInput,
                R.id.expirationDateInput,
                R.id.cvcInput
            ).forEach {
                view.withId(it).checkDisplayed()
            }
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}

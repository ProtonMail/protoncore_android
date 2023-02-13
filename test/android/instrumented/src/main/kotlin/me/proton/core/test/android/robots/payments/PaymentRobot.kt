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

import me.proton.core.test.quark.data.Plan
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.payment.presentation.R

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

    inline fun <reified T: PaymentRobot> switchPaymentProvider(): T = clickElement(R.id.nextPaymentProviderButton)

    open class Verify : CoreVerify() {
        fun billingDetailsDisplayed(
            plan: Plan,
            currency: String,
            googleIAPAvailable: Boolean
        ) {
            view.withId(R.id.planNameText).withText(plan.text).checkDisplayed()
            view.withId(R.id.billingPeriodText).withText(R.string.payments_billing_yearly).checkDisplayed()
            view.withId(R.id.amountText).startsWith(currency).checkDisplayed()
            view.withId(R.id.payButton).checkDisplayed()
            if (googleIAPAvailable) {
                view.withId(R.id.nextPaymentProviderButton).checkDisplayed()
            }
        }

        fun paymentMethodDisplayed(title: String, details: String) {
            view.withText(title).withId(R.id.paymentMethodTitleText).checkDisplayed()
            view.withText(details).withId(R.id.paymentMethodSubtitleText).checkDisplayed()
        }

        fun googlePaymentMethodDisplayed(title: String) {
            view.withText(title).withId(R.id.paymentMethodTitleText).checkDisplayed()
            view.withText("").withId(R.id.paymentMethodSubtitleText).checkNotDisplayed()
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

        fun googleIAPElementsDisplayed() {
            arrayOf(
                me.proton.core.paymentiap.presentation.R.id.termsConditionsInfoText,
                me.proton.core.paymentiap.presentation.R.id.priceSurchargeInfoText
            ).forEach {
                view.withId(it).checkDisplayed()
            }
        }

        fun nextPaymentProviderButtonDisplayed() {
            view.withId(R.id.nextPaymentProviderButton).checkDisplayed()
        }

        fun nextPaymentProviderButtonNotDisplayed() {
            view.withId(R.id.nextPaymentProviderButton).checkNotDisplayed()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}

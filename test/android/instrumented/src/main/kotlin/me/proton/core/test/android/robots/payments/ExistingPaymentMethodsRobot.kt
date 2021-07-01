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

package me.proton.core.test.android.robots.payments

import me.proton.core.payment.presentation.R
import me.proton.core.test.android.instrumented.builders.OnView
import me.proton.core.test.android.robots.CoreRobot

/**
 * [ExistingPaymentMethodsRobot] class contains existing payment methods view actions and verifications implementation
 */
class ExistingPaymentMethodsRobot : PaymentRobot() {

    /**
     * Clicks 'add credit card' button
     * @return [AddCreditCardRobot]
     */
    fun addNewCard(): AddCreditCardRobot = clickElement(R.id.addCreditCardButton)

    /**
     * Clicks a payment method radio which has a sibling with text [details]
     * @return [ExistingPaymentMethodsRobot]
     */
    fun selectPaymentMethod(details: String): ExistingPaymentMethodsRobot = clickElement(paymentMethod(details))

    companion object PaymentMethodElement : CoreRobot() {
        fun paymentMethod(details: String): OnView =
            view
                .withId(R.id.paymentMethodRadio)
                .hasSibling(
                    view.withText(details)
                )
                .wait()
    }
}

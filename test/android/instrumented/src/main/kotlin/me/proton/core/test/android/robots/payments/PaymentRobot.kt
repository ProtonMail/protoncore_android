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
    inline fun <reified T> pay(): T {
        Thread.sleep(500) // https://jira.protontech.ch/browse/CP-1931
        clickElement<PaymentRobot>(R.id.payButton)
        return T::class.java.newInstance()
    }

    class Verify : CoreVerify() {
        fun billingDetailsDisplayed(planName: String, billingCycle: String, currency: String, amount: String) {
            view.withId(R.id.planNameText).wait().checkContains("Proton $planName")
            view.withId(R.id.billingPeriodText).wait().checkContains("Billed $billingCycle")
            view.withId(R.id.amountText).withText("$currency $amount").wait()
        }

        fun paymentMethodForUserDisplayed(methodTitle: String, details: String) {
            view.withText(methodTitle).withId(R.id.paymentMethodTitleText).wait()
            view.withText(details).withId(R.id.paymentMethodSubtitleText).wait()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}

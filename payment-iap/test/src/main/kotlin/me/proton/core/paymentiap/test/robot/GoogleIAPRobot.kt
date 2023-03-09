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

package me.proton.core.paymentiap.test.robot

import me.proton.core.payment.presentation.R
import me.proton.core.test.android.robots.payments.PaymentRobot

/**
 * [GoogleIAPRobot] class contains new credit card addition actions and verifications implementation
 */
public class GoogleIAPRobot : PaymentRobot() {

    public inline fun <reified T> payWithGPay(): T = clickElement(R.id.gPayButton)

    @Suppress("FINAL_UPPER_BOUND")
    public fun <V : Verify> verify(block: Verify.() -> Unit): Verify = Verify().apply(block)

    public class Verify : PaymentRobot.Verify() {
        public fun payWithCardButtonIsNotVisible() {
            view.withId(R.id.payButton).checkNotDisplayed()
        }

        public fun switchPaymentProviderButtonIsNotVisible() {
            view.withId(R.id.nextPaymentProviderButton).checkNotDisplayed()
        }

        public fun switchPaymentProviderButtonIsVisible() {
            view.withId(R.id.nextPaymentProviderButton).checkDisplayed()
        }

        public fun payWithCardButtonIsVisible() {
            view.withId(R.id.payButton).checkDisplayed()
        }

        public fun nextPaymentProviderButtonIsNotVisible() {
            view.withId(R.id.nextPaymentProviderButton).checkNotDisplayed()
        }

        public fun payWithGoogleButtonIsClickable() {
            view.withId(R.id.gPayButton)
                .checkDisplayed()
                .isEnabled()
                .isClickable()
        }

        public fun googleIAPElementsDisplayed() {
            arrayOf(
                me.proton.core.paymentiap.presentation.R.id.termsConditionsInfoText,
                me.proton.core.paymentiap.presentation.R.id.priceSurchargeInfoText
            ).forEach {
                view.withId(it).checkDisplayed()
            }
        }
    }
}

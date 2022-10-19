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

/**
 * [GoogleIAPRobot] class contains new credit card addition actions and verifications implementation
 */
class GoogleIAPRobot : PaymentRobot() {

    inline fun <reified T> payWithGPay(): T = clickElement(R.id.gPayButton)

    fun switchPaymentProvider(): GoogleIAPRobot = clickElement(R.id.nextPaymentProviderButton)

    @Suppress("FINAL_UPPER_BOUND")
    fun <V : Verify> verify(block: Verify.() -> Unit) = Verify().apply(block)

    class Verify : PaymentRobot.Verify() {
        fun payWithCardButtonIsNotVisible() {
            view.withId(R.id.payButton).checkNotDisplayed()
        }

        fun switchPaymentProviderButtonIsNotVisible() {
            view.withId(R.id.nextPaymentProviderButton).checkNotDisplayed()
        }

        fun switchPaymentProviderButtonIsVisible() {
            view.withId(R.id.nextPaymentProviderButton).checkDisplayed()
        }

        fun payWithGoogleButtonIsClickable() {
            view.withId(R.id.gPayButton)
                .checkDisplayed()
                .isEnabled()
                .isClickable()
        }
    }
}

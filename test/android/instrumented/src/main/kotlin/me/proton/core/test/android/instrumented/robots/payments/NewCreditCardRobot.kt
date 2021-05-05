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

package me.proton.core.test.android.instrumented.robots.payments

import android.widget.EditText
import android.widget.TextView
import me.proton.core.payment.presentation.R

class NewCreditCardRobot : PaymentRobot() {

    fun ccname(name: String): NewCreditCardRobot = setText(R.id.cardNameInput, name)
    fun ccnumber(number: String): NewCreditCardRobot = setText(R.id.cardNumberInput, number)
    fun expirationDate(date: String): NewCreditCardRobot = setText(R.id.expirationDateInput, date)
    fun cvc(securityCode: String): NewCreditCardRobot = setText(R.id.cvcInput, securityCode)
    fun postalCode(number: String): NewCreditCardRobot = setText(R.id.postalCodeInput, number)

    fun country(country: String?): NewCreditCardRobot {
        clickElement<NewCreditCardRobot>(R.id.countriesText, EditText::class.java)
        clickElement<NewCreditCardRobot>(country!!, TextView::class.java)
        return NewCreditCardRobot()
    }
}

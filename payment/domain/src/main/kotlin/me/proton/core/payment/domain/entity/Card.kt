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

package me.proton.core.payment.domain.entity

sealed class Card(
    open val expirationMonth: String,
    open val expirationYear: String,
    open val name: String,
    open val country: String,
    open val zip: String
) {
    data class CardWithPaymentDetails(
        val number: String,
        val cvc: String,
        override val expirationMonth: String,
        override val expirationYear: String,
        override val name: String,
        override val country: String,
        override val zip: String
    ) : Card(expirationMonth, expirationYear, name, country, zip)

    data class CardReadOnly(
        val brand: String,
        val last4: String,
        override val expirationMonth: String,
        override val expirationYear: String,
        override val name: String,
        override val country: String,
        override val zip: String
    ) : Card(expirationMonth, expirationYear, name, country, zip)
}

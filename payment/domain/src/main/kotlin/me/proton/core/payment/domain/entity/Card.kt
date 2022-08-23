/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.payment.domain.entity

public sealed class Card(
    public open val expirationMonth: String,
    public open val expirationYear: String,
    public open val name: String,
    public open val country: String,
    public open val zip: String
) {
    public data class CardWithPaymentDetails(
        val number: String,
        val cvc: String,
        override val expirationMonth: String,
        override val expirationYear: String,
        override val name: String,
        override val country: String,
        override val zip: String
    ) : Card(expirationMonth, expirationYear, name, country, zip)

    public data class CardReadOnly(
        val brand: String,
        val last4: String,
        override val expirationMonth: String,
        override val expirationYear: String,
        override val name: String,
        override val country: String,
        override val zip: String
    ) : Card(expirationMonth, expirationYear, name, country, zip)
}

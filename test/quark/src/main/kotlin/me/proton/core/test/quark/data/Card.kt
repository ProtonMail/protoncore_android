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

package me.proton.core.test.quark.data

import kotlinx.serialization.Serializable
import me.proton.core.util.kotlin.random
import java.util.Calendar
import java.util.Locale

@Serializable
public data class Card(
    val number: String = "",
    val expMonth: String = "",
    val expYear: String = "",
    val name: String = "",
    val cvc: String = "",
    val country: String = "",
    val zip: String = ""
) {
    public val brand: Brand = when (number.take(1).toInt()) {
        3 -> Brand.AmericanExpress
        4 -> Brand.Visa
        5 -> Brand.Mastercard
        else -> Brand.Unknown
    }

    @Serializable
    public enum class Brand(public val value: String) {
        Mastercard("MasterCard"),
        AmericanExpress("American Express"),
        Visa("Visa"),
        Unknown(""),
    }

    public companion object {
        public val default: Card = Card(
            number = "4242424242424242",
            expMonth = String.format(Locale.ROOT, "%02d", Calendar.getInstance().get(Calendar.MONTH) + 1),
            expYear = (Calendar.getInstance().get(Calendar.YEAR) + 1).toString(),
            name = "Test Account",
            cvc = (111..999).random().toString(),
            country = "Angola",
            zip = String.random(length = 4)
        )
    }
}

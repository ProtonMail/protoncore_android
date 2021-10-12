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

package me.proton.core.presentation.utils

import java.lang.Long.numberOfLeadingZeros
import java.text.NumberFormat
import java.util.Locale

typealias Price = Double
const val PRICE_ZERO = 0.0
const val BYTE_DIVIDER = 1024

fun Price.formatPriceDefaultLocale(currency: String, fractionDigits: Int = 2): String {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    numberFormat.maximumFractionDigits = fractionDigits
    numberFormat.currency = java.util.Currency.getInstance(currency)
    return numberFormat.format(this)
}

fun Long.formatByteToHumanReadable(): String {
    if (this < BYTE_DIVIDER) return "$this B"
    val z = (63 - numberOfLeadingZeros(this)) / 10
    return String.format("%.0f %sB", toDouble() / (1L shl z * 10), " KMGTPE"[z])
}


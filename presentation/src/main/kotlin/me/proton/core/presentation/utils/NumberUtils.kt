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

import java.text.NumberFormat
import java.util.Locale

fun Double.formatPriceDefaultLocale(currency: String): String {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    numberFormat.maximumFractionDigits = 2
    numberFormat.currency = java.util.Currency.getInstance(currency)
    return numberFormat.format(this)
}

fun Int.formatPriceAndCurrencyDefaultLocale(currency: String): String {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    numberFormat.maximumFractionDigits = 0
    numberFormat.currency = java.util.Currency.getInstance(currency)
    return numberFormat.format(this)
}

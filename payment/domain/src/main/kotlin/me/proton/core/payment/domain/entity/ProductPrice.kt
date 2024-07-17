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

import me.proton.core.payment.domain.usecase.PaymentProvider

public open class ProductPrice(
    public open val provider: PaymentProvider,
    public open val priceAmountMicros: Long,
    public open val currency: String,
    public open val formattedPriceAndCurrency: String,
    public open val defaultPriceAmountMicros: Long? = null,
) {
    public val priceAmount : Double get() = priceAmountMicros / 1000000.0
    public val defaultPriceAmount : Double? get() = defaultPriceAmountMicros?.let { it / 1000000.0 }
    public val priceAmountCents : Int get() = (priceAmountMicros / GOOGLE_TO_PROTON_PRICE_DIVIDER).toInt()
    public val defaultPriceAmountCents: Int? get() = defaultPriceAmountMicros?.let { (it / GOOGLE_TO_PROTON_PRICE_DIVIDER).toInt() }
}

// Google Billing library returns price in micros where 1,000,000 micro-units equal one unit of the currency
// but our prices are expressed in cents, this is why we divide by 10000
internal const val GOOGLE_TO_PROTON_PRICE_DIVIDER = 10000

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

package me.proton.core.payment.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus

@Serializable
internal data class CheckSubscriptionResponse(
    @SerialName("Amount")
    val amount: Long,
    @SerialName("AmountDue")
    val amountDue: Long,
    @SerialName("Proration")
    val proration: Int,
    @SerialName("CouponDiscount")
    val couponDiscount: Long,
    @SerialName("Credit")
    val credit: Long,
    @SerialName("Currency")
    val currency: String,
    @SerialName("Cycle")
    val cycle: Int,
    @SerialName("Coupon")
    val coupon: CouponEntity? = null,
    @SerialName("Gift")
    val gift: Long? = null
) {
    fun toSubscriptionStatus(): SubscriptionStatus =
        SubscriptionStatus(
            amount = amount,
            amountDue = amountDue,
            proration = proration,
            couponDiscount = couponDiscount,
            coupon = coupon?.toCoupon(),
            credit = credit,
            currency = Currency.valueOf(currency),
            cycle = SubscriptionCycle.map[cycle] ?: SubscriptionCycle.YEARLY,
            gift = gift
        )
}

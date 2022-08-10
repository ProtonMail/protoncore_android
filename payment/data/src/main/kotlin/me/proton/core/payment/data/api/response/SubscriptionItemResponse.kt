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
import me.proton.core.paymentcommon.domain.entity.Subscription
import me.proton.core.paymentcommon.domain.entity.SubscriptionManagement

@Serializable
internal data class SubscriptionItemResponse(
    @SerialName("ID")
    val id: String,
    @SerialName("InvoiceID")
    val invoiceId: String,
    @SerialName("Cycle")
    val cycle: Int,
    @SerialName("PeriodStart")
    val periodStart: Long,
    @SerialName("PeriodEnd")
    val periodEnd: Long,
    @SerialName("CouponCode")
    val couponCode: String? = null,
    @SerialName("Currency")
    val currency: String,
    @SerialName("Amount")
    val amount: Long,
    @SerialName("External")
    val external: Int? = null,
    @SerialName("Plans")
    val plans: List<PlanResponse>
) {
    fun toSubscription(): Subscription = Subscription(
        id = id,
        invoiceId = invoiceId,
        cycle = cycle,
        periodStart = periodStart,
        periodEnd = periodEnd,
        couponCode = couponCode,
        currency = currency,
        amount = amount,
        external = SubscriptionManagement.map[external],
        plans = plans.map { it.toPlan() }
    )
}

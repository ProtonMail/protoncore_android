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

package me.proton.core.payment.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.payment.domain.entity.Subscription

@Serializable
internal data class SubscriptionEntity(
    @SerialName("ID")
    private val id: String,
    @SerialName("InvoiceID")
    private val invoiceId: String,
    @SerialName("Cycle")
    private val cycle: Int,
    @SerialName("PeriodStart")
    private val periodStart: Long,
    @SerialName("PeriodEnd")
    private val periodEnd: Long,
    @SerialName("CouponCode")
    private val couponCode: String? = null,
    @SerialName("Currency")
    private val currency: String,
    @SerialName("Amount")
    private val amount: Long,
    @SerialName("Plans")
    private val plans: List<PlanEntity>
) {
    fun toSubscription(): Subscription = Subscription(
        id, invoiceId, cycle, periodStart, periodEnd, couponCode, currency, amount, plans.map { it.toPlan() }
    )
}

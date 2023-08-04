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

package me.proton.core.payment.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.payment.domain.entity.DynamicSubscription
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.plan.data.api.response.EntitlementResource
import me.proton.core.plan.data.api.response.PlanDecorationResource
import me.proton.core.plan.data.api.response.toDynamicPlanDecoration
import me.proton.core.plan.data.api.response.toDynamicPlanEntitlement
import java.time.Instant

@Serializable
internal data class DynamicSubscriptionItemResponse(
    @SerialName("Name")
    val name: String? = null,
    @SerialName("Description")
    val description: String? = null,
    @SerialName("ID")
    val id: String,
    @SerialName("ParentMetaPlanID")
    val parentMetaPlanID: String? = null,
    @SerialName("Type")
    val type: Int,
    @SerialName("Title")
    val title: String,
    @SerialName("Cycle")
    val cycle: Int? = null,
    @SerialName("CycleDescription")
    val cycleDescription: String? = null,
    @SerialName("Currency")
    val currency: String,
    @SerialName("Amount")
    val amount: Long,
    @SerialName("Offer")
    val offer: String, //??
    @SerialName("PeriodStart")
    val periodStart: Long,
    @SerialName("PeriodEnd")
    val periodEnd: Long,
    @SerialName("CreateTime")
    val createTime: Long,
    @SerialName("CouponCode")
    val couponCode: String? = null,
    @SerialName("Discount")
    val discount: Long,
    @SerialName("RenewDiscount")
    val renewDiscount: Long,
    @SerialName("RenewAmount")
    val renewAmount: Long,
    @SerialName("Renew")
    val renew: Boolean,
    @SerialName("External")
    val external: Boolean,
    @SerialName("Decorations")
    val decorations: List<PlanDecorationResource>? = null,
    @SerialName("Entitlements")
    val entitlements: List<EntitlementResource>? = null
) {
    fun toDynamicSubscription(): DynamicSubscription = DynamicSubscription(
        name = name,
        description = description,
        id = id,
        parentPlanId = parentMetaPlanID,
        type = type,
        title = title,
        cycleMonths = cycle,
        cycleDescription = cycleDescription,
        currency = currency,
        amount = amount,
        offer = offer,
        periodStart = Instant.ofEpochSecond(periodStart),
        periodEnd = Instant.ofEpochSecond(periodEnd),
        createTime = Instant.ofEpochSecond(createTime),
        couponCode = couponCode,
        discount = discount,
        renewDiscount = renewDiscount,
        renewAmount = renewAmount,
        renew = renew,
        external = if (external) SubscriptionManagement.GOOGLE_MANAGED else SubscriptionManagement.PROTON_MANAGED, // TODO: check this
        decorations = decorations?.mapNotNull { it.toDynamicPlanDecoration() } ?: emptyList(),
        entitlements = entitlements?.mapNotNull { it.toDynamicPlanEntitlement() } ?: emptyList()
    )
}

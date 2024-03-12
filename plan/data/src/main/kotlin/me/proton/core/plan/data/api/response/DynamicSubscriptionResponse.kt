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

package me.proton.core.plan.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.util.kotlin.toBooleanOrFalse
import java.time.Instant

@Serializable
internal data class DynamicSubscriptionResponse(
    @SerialName("Name")
    val name: String? = null,
    @SerialName("Title")
    val title: String,
    @SerialName("Description")
    val description: String,
    @SerialName("ParentMetaPlanID")
    val parentMetaPlanID: String? = null,
    @SerialName("Type")
    val type: Int? = null,
    @SerialName("Cycle")
    val cycle: Int? = null,
    @SerialName("CycleDescription")
    val cycleDescription: String? = null,
    @SerialName("Currency")
    val currency: String? = null,
    @SerialName("Amount")
    val amount: Long? = null,
    @SerialName("Offer")
    val offer: JsonElement? = null,
    @SerialName("PeriodStart")
    val periodStart: Long? = null,
    @SerialName("PeriodEnd")
    val periodEnd: Long? = null,
    @SerialName("CreateTime")
    val createTime: Long? = null,
    @SerialName("CouponCode")
    val couponCode: String? = null,
    @SerialName("Discount")
    val discount: Long? = null,
    @SerialName("RenewDiscount")
    val renewDiscount: Long? = null,
    @SerialName("RenewAmount")
    val renewAmount: Long? = null,
    @SerialName("Renew")
    val renew: Int? = null,
    @SerialName("External")
    val external: Int? = null,
    @SerialName("Decorations")
    val decorations: List<DynamicDecorationResource>? = null,
    @SerialName("Entitlements")
    val entitlements: List<DynamicEntitlementResource>? = null,
    @SerialName("CustomerID")
    val customerId: String? = null
) {
    fun toDynamicSubscription(iconsEndpoint: String): DynamicSubscription = DynamicSubscription(
        name = name,
        description = description,
        parentPlanId = parentMetaPlanID,
        type = type,
        title = title,
        cycleMonths = cycle,
        cycleDescription = cycleDescription,
        currency = currency,
        amount = amount,
        periodStart = periodStart?.let { Instant.ofEpochSecond(it) },
        periodEnd = periodEnd?.let { Instant.ofEpochSecond(it) },
        createTime = createTime?.let { Instant.ofEpochSecond(it) },
        couponCode = couponCode,
        discount = discount,
        renewDiscount = renewDiscount,
        renewAmount = renewAmount,
        renew = renew?.toBooleanOrFalse(),
        external = SubscriptionManagement.map[external],
        decorations = decorations?.mapNotNull { it.toDynamicPlanDecoration() } ?: emptyList(),
        entitlements = entitlements?.mapNotNull { it.toDynamicPlanEntitlement(iconsEndpoint) } ?: emptyList(),
        customerId = customerId
    )
}

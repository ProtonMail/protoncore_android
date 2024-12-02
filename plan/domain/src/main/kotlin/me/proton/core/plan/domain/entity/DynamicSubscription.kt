/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.domain.entity

import java.time.Instant

public data class DynamicSubscription(
    val name: String?,
    val title: String,
    val description: String,

    val parentPlanId: String? = null,
    val type: Int? = null,

    val cycleMonths: Int? = null,
    val cycleDescription: String? = null,
    val currency: String? = null,
    val amount: Long? = null,

    val periodStart: Instant? = null,
    val periodEnd: Instant? = null,
    val createTime: Instant? = null,

    val couponCode: String? = null,
    val discount: Long? = null,
    val renewDiscount: Long? = null,
    val renewAmount: Long? = null,

    val renew: Boolean? = null,
    val external: SubscriptionManagement? = null,
    val deeplink: String? = null,

    val decorations: List<DynamicDecoration> = emptyList(),
    val entitlements: List<DynamicEntitlement> = emptyList(),

    val customerId: String? = null
)

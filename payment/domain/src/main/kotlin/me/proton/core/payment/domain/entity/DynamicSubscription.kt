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

package me.proton.core.payment.domain.entity

import me.proton.core.plan.domain.entity.DynamicPlanEntitlement
import me.proton.core.plan.domain.entity.DynamicPlanDecoration
import java.time.Instant

public data class DynamicSubscription(
    val id: String,
    val amount: Int,
    val createTime: Instant,
    val currency: String,
    val cycleDescription: String,
    val cycleMonths: Int,
    val discount: Int,
    val external: SubscriptionManagement,
    val periodStart: Instant,
    val periodEnd: Instant,
    val renew: Int,
    val renewDiscount: Int,
    val renewAmount: Int,

    val couponCode: String? = null,
    val decorations: List<DynamicPlanDecoration> = emptyList(),
    val entitlements: List<DynamicPlanEntitlement> = emptyList(),
)

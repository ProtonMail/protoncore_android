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

import me.proton.core.plan.domain.entity.Plan

public data class Subscription constructor(
    val id: String,
    val invoiceId: String,
    val cycle: Int,
    val periodStart: Long,
    val periodEnd: Long,
    val couponCode: String?,
    val currency: String,
    val amount: Long,
    val discount: Long,
    val renewDiscount: Long,
    val renewAmount: Long,
    val external: SubscriptionManagement?,
    val plans: List<Plan>,
    val customerId: String?
)

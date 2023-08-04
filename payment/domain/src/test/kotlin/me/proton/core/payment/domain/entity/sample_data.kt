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

import me.proton.core.plan.domain.entity.DynamicDecoration
import me.proton.core.plan.domain.entity.DynamicEntitlement
import java.time.Instant

val dynamicSubscription = DynamicSubscription(
    name = "free",
    title = "Proton Free",
    description = "Current Plan",
    type = 1,
    createTime = Instant.ofEpochSecond(1_570_708_458),
    currency = "CHF",
    cycleDescription = "1 year",
    cycleMonths = 12,
    discount = -28_788,
    amount = 0,
    external = false,
    periodStart = Instant.ofEpochSecond(1_665_402_858),
    periodEnd = Instant.ofEpochSecond(1_696_938_858),
    renew = true,
    renewDiscount = -28_788,
    renewAmount = 0,
    couponCode = "COUPON123",
    decorations = listOf(
        DynamicDecoration.Star(iconName = "tick")
    ),
    entitlements = listOf(
        DynamicEntitlement.Description(
            iconUrl = "tick",
            text = "Up to 1 GB storage",
            hint = "Start with 500 MB and unlock more storage along the way."
        )
    ),
)

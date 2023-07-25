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

private const val PLAN_ICON_SVG = """
<svg width="204" height="204" xmlns="http://www.w3.org/2000/svg">
    <g><ellipse stroke-width="4" stroke="#000" ry="100" rx="100" id="svg_1" cy="102" cx="102" fill="#fff"/></g>
</svg>
"""

val dynamicSubscription = DynamicSubscription(
    id = "DgauUA2dU6_ufQculMB1b_wecb3D2PraQlfPbknlonENxSm88iiMOkMBPfa0gKEhtdbv_gu4t_CRN6PEu0DQuw==",
    amount = 0,
    createTime = Instant.ofEpochSecond(1_570_708_458),
    currency = "CHF",
    cycleDescription = "1 year",
    cycleMonths = 12,
    discount = -28_788,
    external = SubscriptionManagement.PROTON_MANAGED,
    periodStart = Instant.ofEpochSecond(1_665_402_858),
    periodEnd = Instant.ofEpochSecond(1_696_938_858),
    renew = 1,
    renewDiscount = -28_788,
    renewAmount = 0,
    couponCode = "COUPON123",
    decorations = listOf(
        DynamicPlanDecoration.Star(iconSvg = PLAN_ICON_SVG)
    ),
    entitlements = listOf(
        DynamicPlanEntitlement.Description(
            iconBase64 = PLAN_ICON_SVG,
            iconName = "tick",
            text = "500 GB storage",
            hint = "Lots of storage."
        )
    ),
)

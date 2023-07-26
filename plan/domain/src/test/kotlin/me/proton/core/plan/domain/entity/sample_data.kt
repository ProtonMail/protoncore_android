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

import me.proton.core.domain.type.IntEnum
import java.util.Base64
import java.util.Calendar
import java.util.EnumSet

private const val PLAN_ICON_SVG = """
<svg width="204" height="204" xmlns="http://www.w3.org/2000/svg">
    <g><ellipse stroke-width="4" stroke="#000" ry="100" rx="100" id="svg_1" cy="102" cx="102" fill="#fff"/></g>
</svg>
"""

private val planIconBase64 =
    Base64.getEncoder().encode(PLAN_ICON_SVG.toByteArray()).decodeToString()

val freePlan = DynamicPlan(
    id = "PRoqzg34",
    name = "free",
    order = 10,
    state = DynamicPlanState.Unavailable,
    title = "Free",
    type = null,
    description = "The no-cost starter account designed to empower everyone with privacy by default.",
    entitlements = listOf(
        DynamicPlanEntitlement.Description(
            iconBase64 = planIconBase64,
            iconName = "tick",
            text = "Up to 1 GB storage",
            hint = "Start with 500 MB and unlock more storage along the way."
        )
    ),
)

val mailPlusPlan = DynamicPlan(
    id = "l8vWAXHBQmv",
    name = "mail2022",
    order = 5,
    state = DynamicPlanState.Available,
    title = "Mail Plus",
    type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary),
    features = EnumSet.of(DynamicPlanFeature.CatchAll),
    services = EnumSet.of(DynamicPlanService.Mail, DynamicPlanService.Calendar)
)

val unlimitedPlan = DynamicPlan(
    id = "lY2ZCYkVNfl_osze70PRoqzg34MQI64mE3-pLc-yMp_6KXthkV1paUsyS276OdNwucz9zKoWKZL_TgtKxOPb0w==",
    name = "bundle2022",
    order = 0,
    state = DynamicPlanState.Available,
    title = "Proton Unlimited",
    type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary),

    decorations = listOf(DynamicPlanDecoration.Star(planIconBase64)),
    description = null,
    entitlements = listOf(
        DynamicPlanEntitlement.Description(
            iconBase64 = planIconBase64,
            iconName = "tick",
            text = "500 GB storage",
            hint = "Storage space is shared across Proton Mail, Proton Calendar, and Proton Drive."
        ),
        DynamicPlanEntitlement.Description(
            iconBase64 = planIconBase64,
            iconName = "tick",
            text = "Unlimited folders, labels, and filters."
        )
    ),
    features = EnumSet.allOf(DynamicPlanFeature::class.java),
    instances = listOf(
        DynamicPlanInstance(
            id = "aJZHNZE_fd_rWygalcsahpc8ihMinUHUFGWXq8K0eoGH72CbCXp2KS82uvTPwdOw04ufbLk8zDyRDj7oNPrQCA==",
            months = 1,
            description = "For 1 month",
            periodEnd = Calendar.getInstance().let {
                it.add(Calendar.MONTH, 1)
                it.toInstant()
            },
            price = listOf(
                DynamicPlanPrice(
                    currency = "CHF",
                    current = 499,
                    default = 499
                )
            )
        )
    ),
    offers = listOf(
        DynamicPlanOffer(
            name = "Next month's offer",
            startTime = Calendar.getInstance().let {
                it.roll(Calendar.MONTH, 1)
                it.toInstant()
            },
            endTime = Calendar.getInstance().let {
                it.roll(Calendar.MONTH, 2)
                it.toInstant()
            },
            months = 1,
            price = listOf(
                DynamicPlanPrice(
                    currency = "CHF",
                    current = 1199
                )
            )
        )
    ),
    services = EnumSet.allOf(DynamicPlanService::class.java)
)

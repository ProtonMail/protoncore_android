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

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.type.IntEnum
import java.util.Calendar
import java.util.EnumSet

val freePlan = DynamicPlan(
    name = "free",
    order = 10,
    state = DynamicPlanState.Unavailable,
    title = "Free",
    type = null,
    description = "The no-cost starter account designed to empower everyone with privacy by default.",
    entitlements = listOf(
        DynamicEntitlement.Description(
            iconUrl = "tick",
            text = "Up to 1 GB storage",
            hint = "Start with 500 MB and unlock more storage along the way."
        )
    ),
)

val mailPlusPlan = DynamicPlan(
    name = "mail2022",
    order = 5,
    state = DynamicPlanState.Available,
    title = "Mail Plus",
    type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary),
    features = EnumSet.of(DynamicPlanFeature.CatchAll),
    services = EnumSet.of(DynamicPlanService.Mail, DynamicPlanService.Calendar)
)

val unlimitedPlan = DynamicPlan(
    name = "bundle2022",
    order = 0,
    state = DynamicPlanState.Available,
    title = "Proton Unlimited",
    type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary),
    decorations = listOf(DynamicDecoration.Starred("tick")),
    description = null,
    entitlements = listOf(
        DynamicEntitlement.Description(
            iconUrl = "tick",
            text = "500 GB storage",
            hint = "Storage space is shared across Proton Mail, Proton Calendar, and Proton Drive."
        ),
        DynamicEntitlement.Description(
            iconUrl = "tick",
            text = "Unlimited folders, labels, and filters."
        )
    ),
    features = EnumSet.allOf(DynamicPlanFeature::class.java),
    instances = listOf(
        DynamicPlanInstance(
            cycle = 1,
            description = "For 1 month",
            periodEnd = Calendar.getInstance().let {
                it.add(Calendar.MONTH, 1)
                it.toInstant()
            },
            price = listOf(
                DynamicPlanPrice(
                    id = "aJZHNZE_fd_rWygalcsahpc8ihMinUHUFGWXq8K0eoGH72CbCXp2KS82uvTPwdOw04ufbLk8zDyRDj7oNPrQCA==",
                    currency = "CHF",
                    current = 499,
                    default = 499
                )
            ).associateBy { it.currency },
            vendors = mapOf(
                AppStore.GooglePlay to DynamicPlanVendor(
                    productId = "googlemail_plus_1_renewing",
                    customerId = "cus_google_fAx9TIdL63UmeYDmUo3l"
                )
            )
        )
    ).associateBy { it.cycle },
    /*
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
                    id = "aJZHNZE_fd_rWygalcsahpc8ihMinUHUFGWXq8K0eoGH72CbCXp2KS82uvTPwdOw04ufbLk8zDyRDj7oNPrQCA==",
                    currency = "CHF",
                    current = 1199
                )
            )
        )
    ),*/
    services = EnumSet.allOf(DynamicPlanService::class.java)
)

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

package me.proton.core.plan.presentation.entity

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.type.IntEnum
import me.proton.core.plan.domain.entity.DynamicDecoration
import me.proton.core.plan.domain.entity.DynamicEntitlement
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanFeature
import me.proton.core.plan.domain.entity.DynamicPlanInstance
import me.proton.core.plan.domain.entity.DynamicPlanPrice
import me.proton.core.plan.domain.entity.DynamicPlanService
import me.proton.core.plan.domain.entity.DynamicPlanState
import me.proton.core.plan.domain.entity.DynamicPlanType
import me.proton.core.plan.domain.entity.DynamicPlanVendor
import java.util.Calendar
import java.util.EnumSet

val mailPlusPlan = DynamicPlan(
    name = "mail2022",
    order = 5,
    state = DynamicPlanState.Available,
    title = "Mail Plus",
    type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary),
    features = EnumSet.of(DynamicPlanFeature.CatchAll),
    services = EnumSet.of(DynamicPlanService.Mail, DynamicPlanService.Calendar)
)

val bundlePlan = DynamicPlan(
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
                ),
                DynamicPlanPrice(
                    id = "bJZHNZE_fd_rWygalcsahpc8ihMinUHUFGWXq8K0eoGH72CbCXp2KS82uvTPwdOw04ufbLk8zDyRDj7oNPrQCA==",
                    currency = "EUR",
                    current = 499,
                    default = 499
                ),
                DynamicPlanPrice(
                    id = "cJZHNZE_fd_rWygalcsahpc8ihMinUHUFGWXq8K0eoGH72CbCXp2KS82uvTPwdOw04ufbLk8zDyRDj7oNPrQCA==",
                    currency = "USD",
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
        ), DynamicPlanInstance(
            cycle = 12,
            description = "For 12 months",
            periodEnd = Calendar.getInstance().let {
                it.add(Calendar.MONTH, 12)
                it.toInstant()
            },
            price = listOf(
                DynamicPlanPrice(
                    id = "dJZHNZE_fd_rWygalcsahpc8ihMinUHUFGWXq8K0eoGH72CbCXp2KS82uvTPwdOw04ufbLk8zDyRDj7oNPrQCA==",
                    currency = "CHF",
                    current = 4990,
                    default = 4990
                ),
                DynamicPlanPrice(
                    id = "eJZHNZE_fd_rWygalcsahpc8ihMinUHUFGWXq8K0eoGH72CbCXp2KS82uvTPwdOw04ufbLk8zDyRDj7oNPrQCA==",
                    currency = "EUR",
                    current = 4990,
                    default = 4990
                ),
                DynamicPlanPrice(
                    id = "fJZHNZE_fd_rWygalcsahpc8ihMinUHUFGWXq8K0eoGH72CbCXp2KS82uvTPwdOw04ufbLk8zDyRDj7oNPrQCA==",
                    currency = "USD",
                    current = 4990,
                    default = 4990
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
    services = EnumSet.allOf(DynamicPlanService::class.java)
)

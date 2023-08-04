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

package me.proton.core.plan.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.plan.domain.entity.DynamicPlanInstance
import java.time.Instant

@Serializable
internal data class DynamicPlanInstanceResource(
    @SerialName("ID")
    val id: String,

    @SerialName("Cycle")
    val cycle: Int,

    @SerialName("Description")
    val description: String,

    @SerialName("PeriodEnd")
    val periodEnd: Long,

    @SerialName("Price")
    val price: List<PriceResource>,

    @SerialName("Vendors")
    val vendors: Map<String, PlanVendorResponse> = emptyMap()
)

internal fun DynamicPlanInstanceResource.toDynamicPlanInstance(): DynamicPlanInstance =
    DynamicPlanInstance(
        id = id,
        cycle = cycle,
        description = description,
        periodEnd = Instant.ofEpochSecond(periodEnd),
        price = price.associate { it.currency to it.toDynamicPlanPrice() },
        vendors = vendors.toPlanVendorDataMap(),
    )

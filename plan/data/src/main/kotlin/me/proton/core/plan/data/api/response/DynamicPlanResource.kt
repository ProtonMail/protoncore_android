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
import me.proton.core.domain.type.IntEnum
import me.proton.core.domain.type.StringEnum
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanFeature
import me.proton.core.plan.domain.entity.DynamicPlanLayout
import me.proton.core.plan.domain.entity.DynamicPlanService
import me.proton.core.plan.domain.entity.DynamicPlanState
import me.proton.core.plan.domain.entity.DynamicPlanType
import me.proton.core.util.kotlin.hasFlag
import java.util.EnumSet

@Serializable
internal data class DynamicPlanResource(
    @SerialName("Name")
    val name: String,

    @SerialName("State")
    val state: Int,

    @SerialName("Title")
    val title: String,

    @SerialName("Decorations")
    val decorations: List<DynamicDecorationResource> = emptyList(),

    @SerialName("Description")
    val description: String? = null,

    @SerialName("Entitlements")
    val entitlements: List<DynamicEntitlementResource> = emptyList(),

    @SerialName("Features")
    val features: Int? = null,

    @SerialName("Instances")
    val instances: List<DynamicPlanInstanceResource> = emptyList(),

    @SerialName("Layout")
    val layout: String? = null,

    @SerialName("Offers")
    val offers: List<DynamicOfferResource> = emptyList(),

    @SerialName("ParentMetaPlanID")
    val parentMetaPlanID: String? = null,

    @SerialName("Services")
    val services: Int? = null,

    @SerialName("Type")
    val type: Int? = null
)

internal fun DynamicPlanResource.toDynamicPlan(iconsEndpoint: String, order: Int): DynamicPlan = DynamicPlan(
    name = name,
    order = order,
    state = when {
        state.hasFlag(DynamicPlanState.Available.code) -> DynamicPlanState.Available
        else -> DynamicPlanState.Unavailable
    },
    title = title,
    entitlements = entitlements.mapNotNull { it.toDynamicPlanEntitlement(iconsEndpoint) },
    decorations = decorations.mapNotNull { it.toDynamicPlanDecoration() },
    description = description,
    features = features?.let { features ->
        EnumSet.copyOf(DynamicPlanFeature.values().filter { features.hasFlag(it.code) })
    } ?: EnumSet.noneOf(DynamicPlanFeature::class.java),
    instances = instances.associate { it.cycle to it.toDynamicPlanInstance() },
    layout = layout?.let { layout ->
        StringEnum(layout, DynamicPlanLayout.values().firstOrNull { it.code == layout })
    } ?: StringEnum(DynamicPlanLayout.Default.code, DynamicPlanLayout.Default),
    offers = offers.map { it.toDynamicPlanOffer() },
    parentMetaPlanID = parentMetaPlanID,
    services = services?.let { services ->
        EnumSet.copyOf(DynamicPlanService.values().filter { services.hasFlag(it.code) })
    } ?: EnumSet.noneOf(DynamicPlanService::class.java),
    type = type?.let { IntEnum(it, DynamicPlanType.from(it)) }
)

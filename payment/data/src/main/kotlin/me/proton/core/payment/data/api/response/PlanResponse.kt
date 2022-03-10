/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.payment.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.util.kotlin.toBoolean

@Serializable
internal data class PlanResponse(
    @SerialName("ID")
    val id: String,
    @SerialName("Type")
    val type: Int,
    @SerialName("Cycle")
    val cycle: Int,
    @SerialName("Name")
    val name: String,
    @SerialName("Title")
    val title: String,
    @SerialName("Currency")
    val currency: String,
    @SerialName("Amount")
    val amount: Int,
    @SerialName("MaxDomains")
    val maxDomains: Int,
    @SerialName("MaxAddresses")
    val maxAddresses: Int,
    @SerialName("MaxCalendars")
    val maxCalendars: Int,
    @SerialName("MaxSpace")
    val maxSpace: Long,
    @SerialName("MaxMembers")
    val maxMembers: Int,
    @SerialName("MaxVPN")
    val maxVPN: Int,
    @SerialName("Services")
    val services: Int,
    @SerialName("Features")
    val features: Int,
    @SerialName("Quantity")
    val quantity: Int,
    @SerialName("MaxTier")
    val maxTier: Int,
    @SerialName("State")
    val state: Int? = null
) {
    fun toPlan(): Plan = Plan(
        id = id,
        type = type,
        cycle = cycle,
        name = name,
        title = title,
        currency = currency,
        amount = amount,
        maxDomains = maxDomains,
        maxAddresses = maxAddresses,
        maxCalendars = maxCalendars,
        maxSpace = maxSpace,
        maxMembers = maxMembers,
        maxVPN = maxVPN,
        services = services,
        features = features,
        quantity = quantity,
        maxTier = maxTier,
        state = state?.toBoolean() ?: true
    )
}

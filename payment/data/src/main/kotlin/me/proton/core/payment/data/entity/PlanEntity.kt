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

package me.proton.core.payment.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.payment.domain.entity.Plan

@Serializable
internal data class PlanEntity(
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
    val maxTier: Int
) {
    fun toPlan(): Plan = Plan(
        id, type, cycle, name, title, currency, amount, maxDomains, maxAddresses, maxSpace, maxMembers, maxVPN,
        services, features, quantity, maxTier
    )
}

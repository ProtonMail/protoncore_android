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

package me.proton.core.plan.domain.entity

data class Plan(
    val id: String,
    val type: Int,
    val cycle: Int,
    val name: String,
    val title: String,
    val currency: String,
    val amount: Int,
    val maxDomains: Int,
    val maxAddresses: Int,
    val maxCalendars: Int,
    val maxSpace: Long,
    val maxMembers: Int,
    val maxVPN: Int,
    val services: Int,
    val features: Int,
    val quantity: Int,
    val maxTier: Int,
    val state: Int?,
    val pricing: PlanPricing? = null
)

data class PlanPricing(
    val monthly: Int,
    val yearly: Int,
    val twoYearly: Int? = null
)

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

import me.proton.core.domain.entity.Product
import me.proton.core.domain.type.IntEnum
import me.proton.core.domain.type.StringEnum
import java.util.EnumSet

data class DynamicPlan(
    val id: String,
    val name: String, // code name
    val order: Int,
    val state: DynamicPlanState,
    val title: String,
    val type: IntEnum<DynamicPlanType>?,

    val decorations: List<DynamicPlanDecoration> = emptyList(),
    val description: String? = null,
    val entitlements: List<DynamicPlanEntitlement> = emptyList(),
    val features: EnumSet<DynamicPlanFeature> = EnumSet.noneOf(DynamicPlanFeature::class.java),
    val instances: List<DynamicPlanInstance> = emptyList(),
    val layout: StringEnum<DynamicPlanLayout> = StringEnum(
        DynamicPlanLayout.Default.code,
        DynamicPlanLayout.Default
    ),
    val offers: List<DynamicPlanOffer> = emptyList(),
    val parentMetaPlanID: String? = null,
    val services: EnumSet<DynamicPlanService> = EnumSet.noneOf(DynamicPlanService::class.java)
)

fun DynamicPlan.hasServiceFor(product: Product, exclusive: Boolean): Boolean {
    val service = when (product) {
        Product.Calendar -> DynamicPlanService.Calendar
        Product.Drive -> DynamicPlanService.Drive
        Product.Mail -> DynamicPlanService.Mail
        Product.Vpn -> DynamicPlanService.Vpn
        Product.Pass -> DynamicPlanService.Pass
    }
    return when (exclusive) {
        true -> services.map { it.code }.toSet() == setOf(service.code)
        false -> services.contains(service)
    }
}

fun DynamicPlan.isFree(): Boolean = type == null

enum class DynamicPlanFeature(val code: Int) {
    CatchAll(1)
}

enum class DynamicPlanLayout(val code: String) {
    Default("default")
}

enum class DynamicPlanService(val code: Int) {
    Mail(1),
    Calendar(Mail.code),
    Drive(2),
    Vpn(4),
    Pass(8),
}

enum class DynamicPlanState(val code: Int) {
    Unavailable(0),
    Available(1)
}

enum class DynamicPlanType(val code: Int) {
    Secondary(0),
    Primary(1);

    companion object {
        fun from(code: Int): DynamicPlanType? = when (code) {
            Primary.code -> Primary
            Secondary.code -> Secondary
            else -> null
        }
    }
}

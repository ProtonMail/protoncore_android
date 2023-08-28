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

import me.proton.core.domain.type.IntEnum
import me.proton.core.domain.type.StringEnum
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanFeature
import me.proton.core.plan.domain.entity.DynamicPlanLayout
import me.proton.core.plan.domain.entity.DynamicPlanService
import me.proton.core.plan.domain.entity.DynamicPlanState
import me.proton.core.plan.domain.entity.DynamicPlanType
import me.proton.core.util.kotlin.deserialize
import java.util.EnumSet
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicPlanResourceTest {
    @Test
    fun fromJsonToResource() {
        assertEquals(
            DynamicPlanResource(
                name = "name",
                state = 1,
                title = "title",
                entitlements = emptyList(),
                decorations = emptyList(),
                description = "description",
                features = 1,
                instances = emptyList(),
                layout = "default",
                offers = emptyList(),
                parentMetaPlanID = "parentId",
                services = 15,
                type = 1,
            ),
            """
                {
                "ID": "123abc",
                "Name": "name",
                "State": 1,
                "Title": "title",
                "Decorations": [],
                "Description": "description",
                "Entitlements": [],
                "Features": 1,
                "Instances": [],
                "Layout": "default",
                "Offers": [],
                "ParentMetaPlanID": "parentId",
                "Services": 15,
                "Type": 1
                }
            """.trimIndent().deserialize()
        )
    }

    @Test
    fun fromResourceToDomain() {
        assertEquals(
            DynamicPlan(
                name = "name",
                order = 5,
                state = DynamicPlanState.Available,
                title = "title",
                entitlements = emptyList(),
                decorations = emptyList(),
                description = "description",
                features = EnumSet.of(DynamicPlanFeature.CatchAll),
                instances = emptyMap(),
                layout = StringEnum("default", DynamicPlanLayout.Default),
                offers = emptyList(),
                parentMetaPlanID = "parentId",
                services = EnumSet.allOf(DynamicPlanService::class.java),
                type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary)
            ),
            DynamicPlanResource(
                name = "name",
                state = 1,
                title = "title",
                entitlements = emptyList(),
                decorations = emptyList(),
                description = "description",
                features = 1,
                instances = emptyList(),
                layout = "default",
                offers = emptyList(),
                parentMetaPlanID = "parentId",
                services = 15,
                type = 1
            ).toDynamicPlan("endpoint", 5)
        )
    }

    @Test
    fun fromResourceToDomainWithUnknownEnums() {
        assertEquals(
            DynamicPlan(
                name = "name",
                order = 5,
                state = DynamicPlanState.Available,
                title = "title",
                entitlements = emptyList(),
                decorations = emptyList(),
                description = "description",
                features = EnumSet.noneOf(DynamicPlanFeature::class.java),
                instances = emptyMap(),
                layout = StringEnum("unknown", null),
                offers = emptyList(),
                parentMetaPlanID = "parentId",
                services = EnumSet.noneOf(DynamicPlanService::class.java),
                type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary)
            ),
            DynamicPlanResource(
                name = "name",
                state = 1,
                title = "title",
                entitlements = emptyList(),
                decorations = emptyList(),
                description = "description",
                features = 0,
                instances = emptyList(),
                layout = "unknown",
                offers = emptyList(),
                parentMetaPlanID = "parentId",
                services = 0,
                type = 1
            ).toDynamicPlan("endpoint", 5)
        )
    }
}

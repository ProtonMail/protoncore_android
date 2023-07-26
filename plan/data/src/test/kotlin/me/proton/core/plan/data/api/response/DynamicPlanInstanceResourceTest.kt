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

import me.proton.core.plan.domain.entity.DynamicPlanInstance
import me.proton.core.util.kotlin.deserialize
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicPlanInstanceResourceTest {
    @Test
    fun fromJsonToResource() {
        assertEquals(
            DynamicPlanInstanceResource(
                id = "123abc",
                months = 1,
                description = "description",
                periodEnd = 100,
                price = emptyList(),
                vendors = emptyMap()
            ),
            """
                {
                "ID": "123abc",
                "Months": 1,
                "Description": "description",
                "PeriodEnd": 100,
                "Price": [],
                "Vendors": {}
                }
            """.trimIndent().deserialize()
        )
    }

    @Test
    fun fromResourceToDomain() {
        assertEquals(
            DynamicPlanInstance(
                id = "123abc",
                months = 1,
                description = "description",
                periodEnd = Instant.ofEpochSecond(100),
                price = emptyList(),
                vendors = emptyMap()
            ),
            DynamicPlanInstanceResource(
                id = "123abc",
                months = 1,
                description = "description",
                periodEnd = 100,
                price = emptyList(),
                vendors = emptyMap()
            ).toDynamicPlanInstance()
        )
    }
}

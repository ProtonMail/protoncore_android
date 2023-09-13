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

import me.proton.core.plan.domain.entity.DynamicPlanOffer
import me.proton.core.util.kotlin.deserialize
import org.junit.Ignore
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicOfferResourceTest {
    @Test
    @Ignore("Not ready yet")
    fun fromJsonToResource() {
        assertEquals(
            DynamicOfferResource(
                name = "name",
                startTime = 1,
                endTime = 100,
                months = 1,
                price = emptyList()
            ),
            """
                {
                "Name": "name",
                "StartTime": 1,
                "EndTime": 100,
                "Months": 1,
                "Price": []
                }
            """.trimIndent().deserialize()
        )
    }

    @Test
    @Ignore("Not ready yet")
    fun fromResourceToDomain() {
        assertEquals(
            DynamicPlanOffer(
                name = "name",
                startTime = Instant.ofEpochSecond(1),
                endTime = Instant.ofEpochSecond(100),
                months = 1,
                price = emptyList()
            ),
            DynamicOfferResource(
                name = "name",
                startTime = 1,
                endTime = 100,
                months = 1,
                price = emptyList()
            ).toDynamicPlanOffer()
        )
    }
}
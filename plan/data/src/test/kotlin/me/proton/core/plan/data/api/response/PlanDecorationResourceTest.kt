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

import android.util.Base64
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import me.proton.core.plan.domain.entity.DynamicPlanDecoration
import me.proton.core.util.kotlin.deserialize
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlanDecorationResourceTest {
    @BeforeTest
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any()) } answers {
            firstArg<String>().toByteArray()
        }
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic(Base64::class)
    }

    @Test
    fun fromJsonToResource() {
        assertEquals(
            PlanDecorationResource.Star(icon = "icon"),
            """
                {
                "Type": "Star",
                "Icon": "icon"
                }
            """.trimIndent().deserialize<PlanDecorationResource>()
        )

        assertEquals(
            PlanDecorationResource.Unknown(type = "custom"),
            """
                {
                "Type": "custom",
                "Color": "red"
                }
            """.trimIndent().deserialize<PlanDecorationResource>()
        )
    }

    @Test
    fun fromResourceToDomain() {
        assertEquals(
            DynamicPlanDecoration.Star(iconBase64 = "icon"),
            PlanDecorationResource.Star(icon = "icon").toDynamicPlanDecoration()
        )

        assertNull(
            PlanDecorationResource.Unknown(type = "custom").toDynamicPlanDecoration()
        )
    }
}

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
import me.proton.core.plan.domain.entity.DynamicPlanEntitlement
import me.proton.core.util.kotlin.deserialize
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EntitlementResourceTest {
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
            EntitlementResource.Description(
                icon = "icon",
                iconName = "icon-name",
                text = "text",
                hint = "hint"
            ),
            """
                {
                "Icon": "icon",
                "IconName": "icon-name",
                "Text": "text",
                "Type": "description",
                "Hint": "hint"
                }
            """.trimIndent().deserialize<EntitlementResource>()
        )

        assertEquals(
            EntitlementResource.Storage(
                current = 128,
                max = 1024
            ),
            """
                {
                "Current": 128,
                "Max": 1024,
                "Type": "storage"
                }
            """.trimIndent().deserialize<EntitlementResource>()
        )
    }

    @Test
    fun unknownEntitlementType() {
        assertEquals(
            EntitlementResource.Unknown(
                type = "custom"
            ),
            """
                {
                "CustomProperty1": "some-value",
                "CustomProperty2": "another-value",
                "Text": "text",
                "Type": "custom"
                }
            """.trimIndent().deserialize<EntitlementResource>()
        )
    }

    @Test
    fun fromResourceToDomain() {
        assertEquals(
            DynamicPlanEntitlement.Description(
                iconBase64 = "icon",
                iconName = "tick",
                text = "Entitlements text",
                hint = "Entitlements hint"
            ),
            EntitlementResource.Description(
                icon = "icon",
                iconName = "tick",
                text = "Entitlements text",
                hint = "Entitlements hint"
            ).toDynamicPlanEntitlement()
        )

        assertEquals(
            DynamicPlanEntitlement.Storage(
                currentMBytes = 128,
                maxMBytes = 1024
            ),
            EntitlementResource.Storage(
                current = 128,
                max = 1024
            ).toDynamicPlanEntitlement()
        )

        assertNull(
            EntitlementResource.Unknown("custom").toDynamicPlanEntitlement()
        )
    }
}

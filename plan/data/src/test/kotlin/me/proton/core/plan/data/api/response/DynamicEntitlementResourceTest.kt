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
import me.proton.core.plan.domain.entity.DynamicEntitlement
import me.proton.core.util.kotlin.deserialize
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DynamicEntitlementResourceTest {
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
            DynamicEntitlementResource.Description(
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
            """.trimIndent().deserialize<DynamicEntitlementResource>()
        )

        assertEquals(
            DynamicEntitlementResource.Storage(
                current = 128,
                max = 1024
            ),
            """
                {
                "Current": 128,
                "Max": 1024,
                "Type": "storage"
                }
            """.trimIndent().deserialize<DynamicEntitlementResource>()
        )
    }

    @Test
    fun unknownEntitlementType() {
        assertEquals(
            DynamicEntitlementResource.Unknown(
                type = "custom"
            ),
            """
                {
                "CustomProperty1": "some-value",
                "CustomProperty2": "another-value",
                "Text": "text",
                "Type": "custom"
                }
            """.trimIndent().deserialize<DynamicEntitlementResource>()
        )
    }

    @Test
    fun fromResourceToDomain() {
        assertEquals(
            DynamicEntitlement.Description(
                iconUrl = "endpoint/tick",
                text = "Entitlements text",
                hint = "Entitlements hint"
            ),
            DynamicEntitlementResource.Description(
                iconName = "tick",
                text = "Entitlements text",
                hint = "Entitlements hint"
            ).toDynamicPlanEntitlement("endpoint")
        )

        assertEquals(
            DynamicEntitlement.Storage(
                currentBytes = 128,
                maxBytes = 1024
            ),
            DynamicEntitlementResource.Storage(
                current = 128,
                max = 1024
            ).toDynamicPlanEntitlement("endpoint")
        )

        assertNull(
            DynamicEntitlementResource.Unknown("custom").toDynamicPlanEntitlement("endpoint")
        )
    }
}

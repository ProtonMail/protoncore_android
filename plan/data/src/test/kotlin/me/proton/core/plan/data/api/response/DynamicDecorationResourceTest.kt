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

import me.proton.core.domain.type.StringEnum
import me.proton.core.plan.domain.entity.DynamicDecoration
import me.proton.core.plan.domain.entity.DynamicDecorationAnchor
import me.proton.core.util.kotlin.deserialize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DynamicDecorationResourceTest {

    @Test
    fun fromJsonToResource() {
        assertEquals(
            DynamicDecorationResource.Starred(iconName = "icon"),
            """
                {
                "Type": "starred",
                "IconName": "icon"
                }
            """.trimIndent().deserialize<DynamicDecorationResource>()
        )

        assertEquals(
            DynamicDecorationResource.Badge(text = "text", anchor = "title"),
            """
                {
                "Type": "badge",
                "Text": "text",
                "Anchor": "title"
                }
            """.trimIndent().deserialize<DynamicDecorationResource>()
        )

        assertEquals(
            DynamicDecorationResource.Unknown(type = "custom"),
            """
                {
                "Type": "custom",
                "Color": "red"
                }
            """.trimIndent().deserialize<DynamicDecorationResource>()
        )
    }

    @Test
    fun fromResourceToDomain() {
        assertEquals(
            DynamicDecoration.Starred(iconName = "icon"),
            DynamicDecorationResource.Starred(iconName = "icon").toDynamicPlanDecoration()
        )

        assertEquals(
            DynamicDecoration.Badge(text = "text", anchor = StringEnum("title", DynamicDecorationAnchor.Title)),
            DynamicDecorationResource.Badge(text = "text", anchor = "title").toDynamicPlanDecoration()
        )

        assertNull(
            DynamicDecorationResource.Unknown(type = "custom").toDynamicPlanDecoration()
        )
    }
}

/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.label.data.remote.resource

import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import org.junit.Assert.assertEquals
import org.junit.Test

internal class LabelResourceMapperTest {

    @Test
    fun `map label resource of "system folder" type to label`() {
        // Given
        val labelIdRaw = "label id"
        val labelName = "label name"
        val labelResource = LabelResource(
            id = labelIdRaw,
            parentId = null,
            name = labelName,
            path = "",
            type = 4,
            color = "",
            order = 0,
            notify = null,
            expanded = null,
            sticky = null
        )
        val userId = UserId("user id")

        // When
        val actual = labelResource.toLabel(userId)

        // Then
        val expected = Label(
            userId = userId,
            labelId = LabelId(labelIdRaw),
            parentId = null,
            name = labelName,
            type = LabelType.SystemFolder,
            path = "",
            color = "",
            order = 0,
            isNotified = null,
            isExpanded = null,
            isSticky = null
        )
        assertEquals(expected, actual)
    }
}
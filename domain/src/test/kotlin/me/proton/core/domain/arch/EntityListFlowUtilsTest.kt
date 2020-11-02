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

package me.proton.core.domain.arch

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.arch.extension.onEntityChanged
import org.junit.Test
import kotlin.test.assertEquals

class EntityListFlowUtilsTest {

    data class Entity(val id: Int, val property1: String, val property2: String)

    @Test
    fun `on entity changed`() = runBlockingTest {

        val flowOfLists = flowOf(
            listOf(
                Entity(1, "a", "a"),
                Entity(2, "a", "a"),
                Entity(3, "a", "a")
            ),
            listOf(
                Entity(1, "b", "b"),
                Entity(2, "a", "b"),
                Entity(3, "a", "b")
            ),
            listOf(
                Entity(1, "a", "c"),
                Entity(2, "a", "c"),
                Entity(3, "a", "c")
            )
        ).onEntityChanged(
            getEntityKey = { entity -> entity.id },
            emitNewEntity = false
        )

        val result = mutableListOf<Entity>()
        flowOfLists.collect { result.add(it) }

        val keys = result.map { it.id }

        assertEquals(listOf(1, 2, 3, 1, 2, 3), keys)
    }

    @Test
    fun `on property changed`() = runBlockingTest {

        val flowOfLists = flowOf(
            listOf(
                Entity(1, "a", "a"),
                Entity(2, "a", "a"),
                Entity(3, "a", "a")
            ),
            listOf(
                Entity(1, "b", "b"),
                Entity(2, "a", "b"),
                Entity(3, "a", "b")
            ),
            listOf(
                Entity(1, "a", "c"),
                Entity(2, "a", "c"),
                Entity(3, "a", "c")
            )
        ).onEntityChanged(
            getEntityKey = { entity -> entity.id },
            equalPredicate = { previous, current ->
                previous.property1 == current.property1
            },
            emitNewEntity = false
        )

        val result = mutableListOf<Entity>()
        flowOfLists.collect { result.add(it) }

        val keys = result.map { it.id }

        assertEquals(listOf(1, 1), keys)
    }
}

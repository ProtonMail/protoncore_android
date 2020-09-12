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

package me.proton.core.util.android.workmanager

import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.workDataOf
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationExtensionsTest {

    @Test
    fun `can create WorkData with explicit serializer`() {
        val input = TestWorkInput("hello", 15)
        val result = input.toWorkData(TestWorkInput.serializer())
        assertEquals(
            """{"name":"hello","number":15}""",
            result.content
        )
    }

    @Test
    fun `can create WorkData with implicit serializer`() {
        val input = TestWorkInput("hello", 15)
        val result = input.toWorkData()
        assertEquals(
            """{"name":"hello","number":15}""",
            result.content
        )
    }

    @Test
    fun `can deserialize WorkData`() {
        val data = workDataOf(SERIALIZED_DATA_KEY to """{"name":"hello","number":15}""")
        val result = data.deserialize(TestWorkInput.serializer())
        assertEquals(TestWorkInput("hello", 15), result)
    }

    @Test
    fun `ListenableWorker can get proper input`() {
        val worker = mockk<ListenableWorker> {
            every { inputData } returns workDataOf(SERIALIZED_DATA_KEY to """{"name":"hello","number":15}""")
        }
        assertEquals(
            TestWorkInput("hello", 15),
            worker.input(TestWorkInput.serializer())
        )
    }

    private val Data.content get() = keyValueMap.values.first()
}

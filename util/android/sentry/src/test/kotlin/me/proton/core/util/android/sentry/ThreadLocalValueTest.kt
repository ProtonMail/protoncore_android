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

package me.proton.core.util.android.sentry

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ThreadLocalValueTest {
    @Test
    fun `get value from main thread`() {
        val tested = ThreadLocalValue(10, clone = { it + 1 })
        assertEquals(10, tested())
        assertEquals(10, tested())
    }

    @Test
    fun `get value from different thread`() {
        val mainThreadId = Thread.currentThread().id
        val tested =
            ThreadLocalValue(listOf(mainThreadId), clone = { it + Thread.currentThread().id })
        assertContentEquals(listOf(mainThreadId), tested())

        runBlocking {
            val dispatcher1 = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            val dispatcher2 = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

            launch(dispatcher1) {
                val threadId = Thread.currentThread().id
                assertContentEquals(listOf(mainThreadId, threadId), tested())
                assertNotEquals(mainThreadId, threadId)
            }
            launch(dispatcher2) {
                val threadId = Thread.currentThread().id
                assertContentEquals(listOf(mainThreadId, threadId), tested())
                assertNotEquals(mainThreadId, threadId)
            }
        }

        assertContentEquals(listOf(mainThreadId), tested())
    }
}

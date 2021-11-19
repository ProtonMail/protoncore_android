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

package me.proton.core.test.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.proton.core.util.kotlin.DispatcherProvider
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CoroutinesTestTest : CoroutinesTest {

    @Test
    fun `verify test completes correctly with injected scope`() = coroutinesTest {
        val time = measureTimeMillis {
            val sut = StructuredConcurrencyTestClass(dispatchers, this)
            advanceUntilIdle()
            assertEquals(15, sut.result)
        }
        assertTrue(time < 1000)
    }

    @Test
    fun `verify test completes correctly with standalone scope`() = coroutinesTest {
        val time = measureTimeMillis {
            val sut = StructuredConcurrencyTestClass(dispatchers)
            advanceUntilIdle()
            assertEquals(15, sut.result)
        }
        assertTrue(time < 1000)
    }
}

private class StructuredConcurrencyTestClass(
    private val dispatchers: DispatcherProvider,
    scope: CoroutineScope = CoroutineScope(dispatchers.Main)
) {

    var result = 0

    init {
        scope.launch {
            delay(DELAY)

            launch(dispatchers.Io) {
                delay(DELAY)
                result = run()
            }
        }
    }

    @Suppress("RedundantAsync") // For test purpose
    private suspend fun run(): Int {
        return coroutineScope {
            async(dispatchers.Comp) {
                delay(DELAY)
                15
            }.await()
        }
    }

    private companion object {
        const val DELAY = 10_000_000L
    }
}

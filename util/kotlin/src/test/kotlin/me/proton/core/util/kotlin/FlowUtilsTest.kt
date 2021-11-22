/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.util.kotlin

import app.cash.turbine.test
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FlowUtilsTest {
    @Test
    fun `catches known error`() = runBlockingTest {
        flow {
            emit(1)
            throw TestError
        }.catchWhen({ it is TestError }) {
            emit(2)
        }.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `does not catch unknown error`() = runBlockingTest {
        flow {
            emit(1)
            error("random error")
        }.catchWhen({ it is TestError }) {
            emit(2)
        }.test {
            assertEquals(1, awaitItem())
            assertEquals("random error", awaitError().message)
        }
    }

    @Test
    fun `re-catches unknown error`() = runBlockingTest {
        flow {
            emit(1)
            error("random error")
        }.catchWhen({ it is TestError }) {
            emit(2)
        }.catch {
            emit(3)
        }.test {
            assertEquals(1, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `retries after detecting known error`() = runBlockingTest {
        var retryCount = 0
        flow {
            emit(1)
            throw TestError
        }.retryOnceWhen({ it is TestError }) {
            retryCount += 1
        }.test {
            assertEquals(1, awaitItem())
            assertEquals(1, awaitItem())
            assertEquals(TestError, awaitError())
        }
        assertEquals(1, retryCount)
    }

    @Test
    fun `does not retry after encountering unknown error`() = runBlockingTest {
        var retryCount = 0
        flow {
            emit(1)
            error("random error")
        }.retryOnceWhen({ it is TestError }) {
            retryCount += 1
        }.test {
            assertEquals(1, awaitItem())
            assertEquals("random error", awaitError().message)
        }
        assertEquals(0, retryCount)
    }

    private object TestError : Throwable()
}

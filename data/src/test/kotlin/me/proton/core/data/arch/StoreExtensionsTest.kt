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

package me.proton.core.data.arch

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StoreExtensionsTest {

    class TestException(message: String) : Exception(message)

    private val fetcher = mockk<Fetcher<String, String>> {
        coEvery { this@mockk.invoke(any()) } returns flowOf(FetcherResult.Data("value"))
    }

    private val reader = mockk<(String) -> Flow<String?>> {
        coEvery { this@mockk.invoke(any()) } returns flowOf("value")
    }

    private val writer = mockk<suspend (String, String) -> Unit> {
        coEvery { this@mockk.invoke(any(), any()) } returns Unit
    }

    private lateinit var store: ProtonStore<String, String>

    @BeforeTest
    fun setUp() {
        val dispatcher = StandardTestDispatcher()
        val scopeProvider = TestCoroutineScopeProvider(TestDispatcherProvider(dispatcher))
        Dispatchers.setMain(dispatcher)
        store = StoreBuilder
            .from(
                fetcher = fetcher,
                sourceOfTruth = SourceOfTruth.of(
                    reader = reader,
                    writer = writer,
                    delete = { },
                    deleteAll = { },
                )
            )
            .buildProtonStore(scopeProvider)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getValueWithoutException() = runTest {
        // When
        val value = store.get("key")
        assertEquals(expected = "value", actual = value)
    }

    @Test
    fun getThrowInnerException() = runTest {
        // Given
        coEvery { reader.invoke(any()) } throws TestException("test")

        // Then
        assertFailsWith<TestException> {
            // When
            store.get("key")
        }
    }

    @Test
    fun freshThrowInnerException() = runTest {
        // Given
        coEvery { fetcher.invoke(any()) } returns flowOf(FetcherResult.Error.Exception(TestException("test")))

        // Then
        assertFailsWith<TestException> {
            // When
            store.fresh("key")
        }
    }

    @Test
    fun freshThrowInnerException2() = runTest {
        // Given
        coEvery { fetcher.invoke(any()) } throws TestException("test")

        // Then
        assertFailsWith<TestException> {
            // When
            store.fresh("key")
        }
    }
}

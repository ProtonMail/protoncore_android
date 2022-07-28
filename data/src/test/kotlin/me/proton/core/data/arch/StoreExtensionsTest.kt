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

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.FetcherResult
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.Test

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

    private val store: ProtonStore<String, String> = StoreBuilder.from(
        fetcher = fetcher,
        sourceOfTruth = SourceOfTruth.of(
            reader = reader,
            writer = writer,
            delete = { },
            deleteAll = { },
        )
    )
        .scope(TestCoroutineScope())
        .buildProtonStore()

    @Test
    fun getValueWithoutException() = runBlockingTest {
        // When
        val value = store.get("key")
        assertEquals(expected = "value", actual = value)
    }

    @Test
    fun getThrowInnerException() = runBlockingTest {
        // Given
        coEvery { reader.invoke(any()) } throws TestException("test")

        // Then
        assertFailsWith<TestException> {
            // When
            store.get("key")
        }
    }

    @Test
    fun freshThrowInnerException() = runBlockingTest {
        // Given
        coEvery { fetcher.invoke(any()) } returns flowOf(FetcherResult.Error.Exception(TestException("test")))

        // Then
        assertFailsWith<TestException> {
            // When
            store.fresh("key")
        }
    }

    @Test
    fun freshThrowInnerException2() = runBlockingTest {
        // Given
        coEvery { fetcher.invoke(any()) } throws TestException("test")

        // Then
        assertFailsWith<TestException> {
            // When
            store.fresh("key")
        }
    }
}

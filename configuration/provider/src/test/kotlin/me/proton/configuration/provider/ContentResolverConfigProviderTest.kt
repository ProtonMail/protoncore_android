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

package me.proton.configuration.provider

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import me.proton.core.configuration.entity.ConfigFieldProvider
import me.proton.core.configuration.provider.ContentResolverConfigProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ContentResolverConfigProviderTest {

    private val mockContext: Context = mockk(relaxed = true)

    private val mockContentResolver: ContentResolver = mockk(relaxed = true)

    private val mockCursor: Cursor = mockk(relaxed = true)

    private val defaultConfigImpl: ConfigFieldProvider = mockk(relaxed = true)

    private val mockUri = mockk<Uri>()

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockUri
        every { mockContext.contentResolver } returns mockContentResolver
    }

    @Test
    fun testFallBackOnNullContentResolver() {
        every { mockContentResolver.query(any(), any(), any(), any(), any()) } returns null

        val provider = ContentResolverConfigProvider(
            mockContext,
            defaultConfigImpl
        )

        assertEquals(defaultConfigImpl.configData, provider.configData)
    }

    @Test
    fun testFallBackOnNullCursor() {
        every { mockContentResolver.query(any(), any(), any(), any(), any()) } returns mockCursor
        every { mockCursor.columnNames } returns emptyArray()

        val provider = ContentResolverConfigProvider(
            mockContext,
            defaultConfigImpl
        )

        assertEquals(defaultConfigImpl.configData, provider.configData)
    }

    @Test
    fun testValuesFromContentResolverAreUsed() {
        val mockData = mapOf(
            "host" to "testHost",
            "useDefaultPins" to true.toString(),
            "apiPrefix" to null.toString()
        )

        every { mockContentResolver.query(any(), any(), any(), any(), any()) } returns mockCursor
        every { mockCursor.columnNames } returns mockData.keys.toTypedArray()
        every { mockCursor.moveToFirst() } returns true

        mockData.entries.forEachIndexed { index, entry ->
            every { mockCursor.getColumnIndex(entry.key) } returns index
            every { mockCursor.getString(index) } returns entry.value
        }

        val provider = ContentResolverConfigProvider(
            mockContext,
            defaultConfigImpl
        )

        assertEquals(mockData, provider.configData)
    }
}

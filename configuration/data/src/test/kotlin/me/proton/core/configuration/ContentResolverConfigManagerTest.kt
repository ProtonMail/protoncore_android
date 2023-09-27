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

package me.proton.core.configuration

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentResolverConfigManagerTest {

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var configManager: ContentResolverConfigManager

    @Test
    fun `fetchConfigDataFromContentResolver returns correct data`() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)
        every { context.contentResolver } returns contentResolver

        configManager = ContentResolverConfigManager(context)

        val cursor: Cursor = mockk(relaxed = true)
        every { cursor.columnNames } returns arrayOf("key1", "key2")
        every { cursor.getColumnIndex("key1") } returns 0
        every { cursor.getColumnIndex("key2") } returns 1
        every { cursor.moveToFirst() } returns true
        every { cursor.getString(0) } returns "value1"
        every { cursor.getString(1) } returns "value2"
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns cursor

        val result = configManager.fetchConfigDataFromContentResolver()

        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), result)
    }
}

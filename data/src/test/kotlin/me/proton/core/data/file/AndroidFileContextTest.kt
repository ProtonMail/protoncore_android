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

package me.proton.core.data.file

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalProtonFileContext::class)
class AndroidFileContextTest {

    private val user1 = UserId("user1")
    private val user2 = UserId("user2")

    private val item1 = Item("item1")
    private val item2 = Item("item2")

    private val context = mockk<Context> {
        every { getDir(any(), any()) } returns Files.createTempDirectory("AndroidFileContextTest-").toFile()
    }

    data class Item(val itemId: String)

    private lateinit var tested: AndroidFileContext<UserId, Item>

    @BeforeTest
    fun setUp() {
        tested = AndroidFileContext("baseDir", context)
    }

    @AfterTest
    fun clean() {
        runBlocking { tested.deleteAll() }
    }

    @Test
    fun getFile() = runTest {
        assertNotNull(tested.getFile(user1, item1))
        assertNotNull(tested.getFile(user1, item2))
        assertNotNull(tested.getFile(user2, item1))
        assertNotNull(tested.getFile(user2, item2))
    }

    @Test
    fun deleteFile() = runTest {
        // WHEN
        assertNotNull(tested.writeText(user1, item1, "data11"))
        assertNotNull(tested.writeText(user1, item2, "data12"))
        assertNotNull(tested.writeText(user2, item1, "data21"))
        assertNotNull(tested.writeText(user2, item2, "data22"))

        // THEN
        assertTrue(tested.deleteFile(user1, item1))
        assertTrue(tested.deleteFile(user1, item2))
        assertTrue(tested.deleteFile(user2, item1))
        assertTrue(tested.deleteFile(user2, item2))

        assertFalse(tested.deleteFile(UserId("unknown"), item2))
    }

    @Test
    fun readTextNull() = runTest {
        assertEquals(expected = null, actual = tested.readText(user1, item1))
    }

    @Test
    fun writeAndReadText() = runTest {
        // WHEN
        assertNotNull(tested.writeText(user1, item1, "data11"))
        assertNotNull(tested.writeText(user1, item2, "data12"))
        assertNotNull(tested.writeText(user2, item1, "data21"))
        assertNotNull(tested.writeText(user2, item2, "data22"))

        // THEN
        assertEquals(expected = "data11", actual = tested.readText(user1, item1))
        assertEquals(expected = "data12", actual = tested.readText(user1, item2))
        assertEquals(expected = "data21", actual = tested.readText(user2, item1))
        assertEquals(expected = "data22", actual = tested.readText(user2, item2))
    }

    @Test
    fun writeAndDeleteText() = runTest {
        // WHEN
        assertNotNull(tested.writeText(user1, item1, "data11"))
        assertNotNull(tested.writeText(user1, item2, "data12"))
        assertNotNull(tested.writeText(user2, item1, "data21"))
        assertNotNull(tested.writeText(user2, item2, "data22"))

        // THEN
        assertTrue(tested.deleteText(user1, item2))
        assertTrue(tested.deleteText(user2, item2))

        assertEquals(expected = "data11", actual = tested.readText(user1, item1))
        assertEquals(expected = null, actual = tested.readText(user1, item2))
        assertEquals(expected = "data21", actual = tested.readText(user2, item1))
        assertEquals(expected = null, actual = tested.readText(user2, item2))
    }

    @Test
    fun writeAndDeleteDir() = runTest {
        // WHEN
        assertNotNull(tested.writeText(user1, item1, "data11"))
        assertNotNull(tested.writeText(user1, item2, "data12"))
        assertNotNull(tested.writeText(user2, item1, "data21"))
        assertNotNull(tested.writeText(user2, item2, "data22"))

        // THEN
        assertTrue(tested.deleteDir(user2))

        assertEquals(expected = "data11", actual = tested.readText(user1, item1))
        assertEquals(expected = "data12", actual = tested.readText(user1, item2))
        assertEquals(expected = null, actual = tested.readText(user2, item1))
        assertEquals(expected = null, actual = tested.readText(user2, item2))
    }

    @Test
    fun writeAndDeleteAll() = runTest {
        // WHEN
        assertNotNull(tested.writeText(user1, item1, "data11"))
        assertNotNull(tested.writeText(user1, item2, "data12"))
        assertNotNull(tested.writeText(user2, item1, "data21"))
        assertNotNull(tested.writeText(user2, item2, "data22"))

        // THEN
        assertTrue(tested.deleteAll())

        assertEquals(expected = null, actual = tested.readText(user1, item1))
        assertEquals(expected = null, actual = tested.readText(user1, item2))
        assertEquals(expected = null, actual = tested.readText(user2, item1))
        assertEquals(expected = null, actual = tested.readText(user2, item2))
    }

    @Test
    fun overwriteFile() = runTest {
        assertNotNull(tested.writeText(user1, item1, "data11"))
        assertEquals(expected = "data11", actual = tested.readText(user1, item1))

        assertNotNull(tested.writeText(user1, item1, "new-data11"))
        assertEquals(expected = "new-data11", actual = tested.readText(user1, item1))
    }
}

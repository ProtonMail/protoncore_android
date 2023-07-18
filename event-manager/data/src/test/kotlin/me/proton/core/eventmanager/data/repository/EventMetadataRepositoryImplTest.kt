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

package me.proton.core.eventmanager.data.repository

import android.content.Context
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.network.data.ApiProvider
import org.junit.Test
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class EventMetadataRepositoryImplTest {

    private val userId1 = UserId("id1")
    private val userId2 = UserId("id2")
    private val eventId = EventId("commonForAll")

    // Legit concurrent Config.
    private val user1CoreConfig = EventManagerConfig.Core(userId1)
    private val user2CoreConfig = EventManagerConfig.Core(userId2)
    private val user1Cal1Config = EventManagerConfig.Calendar(userId1, "id1")
    private val user1Cal2Config = EventManagerConfig.Calendar(userId1, "id2")
    private val user2Cal1Config = EventManagerConfig.Calendar(userId2, "id1")

    private val context = mockk<Context> {
        every { getDir(any(), any()) } returns Files.createTempDirectory("EventMetadataRepositoryImplTest-").toFile()
    }

    private val db = mockk<EventMetadataDatabase>(relaxed = true)
    private val apiProvider = mockk<ApiProvider>(relaxed = true)

    private lateinit var tested: EventMetadataRepositoryImpl

    private fun newMetadata(
        config: EventManagerConfig,
        eventId: EventId,
        response: EventsResponse? = EventsResponse(config.toString())
    ) = EventMetadata.newFrom(config, eventId).copy(response = response)

    @BeforeTest
    fun before() {
        tested = spyk(EventMetadataRepositoryImpl(context, db, apiProvider))
    }

    @AfterTest
    fun clean() {
        runBlocking { tested.deleteAll() }
    }

    @Test
    fun multipleConcurrentConfigWrite() = runTest {
        // WHEN
        tested.writeText(user1CoreConfig, eventId.id, "user1CoreConfig")
        tested.writeText(user2CoreConfig, eventId.id, "user2CoreConfig")
        tested.writeText(user1Cal1Config, eventId.id, "user1Calendar1Config")
        tested.writeText(user1Cal2Config, eventId.id, "user1Calendar2Config")
        tested.writeText(user2Cal1Config, eventId.id, "user2Calendar1Config")

        // THEN
        assertEquals(expected = "user1CoreConfig", actual = tested.readText(user1CoreConfig, eventId.id))
        assertEquals(expected = "user2CoreConfig", actual = tested.readText(user2CoreConfig, eventId.id))
        assertEquals(expected = "user1Calendar1Config", actual = tested.readText(user1Cal1Config, eventId.id))
        assertEquals(expected = "user1Calendar2Config", actual = tested.readText(user1Cal2Config, eventId.id))
        assertEquals(expected = "user2Calendar1Config", actual = tested.readText(user2Cal1Config, eventId.id))
    }

    @Test
    fun multipleConcurrentConfigDelete() = runTest {
        // WHEN
        tested.writeText(user1CoreConfig, eventId.id, "user1CoreConfig")
        tested.writeText(user2CoreConfig, eventId.id, "user2CoreConfig")
        tested.writeText(user1Cal1Config, eventId.id, "user1Cal1Config")
        tested.writeText(user1Cal2Config, eventId.id, "user1Cal2Config")
        tested.writeText(user2Cal1Config, eventId.id, "user2Cal1Config")

        tested.deleteDir(user1CoreConfig)
        tested.deleteDir(user2Cal1Config)

        // THEN
        assertEquals(expected = null, actual = tested.readText(user1CoreConfig, eventId.id))
        assertEquals(expected = "user2CoreConfig", actual = tested.readText(user2CoreConfig, eventId.id))
        assertEquals(expected = "user1Cal1Config", actual = tested.readText(user1Cal1Config, eventId.id))
        assertEquals(expected = "user1Cal2Config", actual = tested.readText(user1Cal2Config, eventId.id))
        assertEquals(expected = null, actual = tested.readText(user2Cal1Config, eventId.id))
    }

    @Test
    fun multipleConcurrentConfigUpdate() = runTest {
        // WHEN
        tested.update(newMetadata(user1CoreConfig, eventId))
        tested.update(newMetadata(user2CoreConfig, eventId))
        tested.update(newMetadata(user1Cal1Config, eventId))
        tested.update(newMetadata(user1Cal2Config, eventId))
        tested.update(newMetadata(user2Cal1Config, eventId))

        // THEN
        assertEquals(expected = user1CoreConfig.toString(), actual = tested.readText(user1CoreConfig, eventId.id))
        assertEquals(expected = user2CoreConfig.toString(), actual = tested.readText(user2CoreConfig, eventId.id))
        assertEquals(expected = user1Cal1Config.toString(), actual = tested.readText(user1Cal1Config, eventId.id))
        assertEquals(expected = user1Cal2Config.toString(), actual = tested.readText(user1Cal2Config, eventId.id))
        assertEquals(expected = user2Cal1Config.toString(), actual = tested.readText(user2Cal1Config, eventId.id))
    }

    @Test
    fun multipleConcurrentConfigUpdateAndDelete() = runTest {
        // WHEN
        tested.update(newMetadata(user1CoreConfig, eventId))
        tested.update(newMetadata(user2CoreConfig, eventId))
        tested.update(newMetadata(user1Cal1Config, eventId))
        tested.update(newMetadata(user1Cal2Config, eventId))
        tested.update(newMetadata(user2Cal1Config, eventId))

        tested.update(newMetadata(user1CoreConfig, eventId, response = null))
        tested.update(newMetadata(user2Cal1Config, eventId, response = null))

        // THEN
        assertEquals(expected = null, actual = tested.readText(user1CoreConfig, eventId.id))
        assertEquals(expected = user2CoreConfig.toString(), actual = tested.readText(user2CoreConfig, eventId.id))
        assertEquals(expected = user1Cal1Config.toString(), actual = tested.readText(user1Cal1Config, eventId.id))
        assertEquals(expected = user1Cal2Config.toString(), actual = tested.readText(user1Cal2Config, eventId.id))
        assertEquals(expected = null, actual = tested.readText(user2Cal1Config, eventId.id))
    }

    @Test
    fun deleteCallDeleteText() = runTest {
        // WHEN
        tested.delete(user1CoreConfig, eventId)

        // THEN
        coVerify { tested.deleteText(user1CoreConfig, eventId.id) }
    }

    @Test
    fun deleteAllCallDeleteDir() = runTest {
        // WHEN
        tested.deleteAll(user1CoreConfig)

        // THEN
        coVerify { tested.deleteDir(user1CoreConfig) }
    }

    @Test
    fun updateCallDeleteDirWriteText() = runTest {
        // WHEN
        tested.update(newMetadata(user1CoreConfig, eventId))

        // THEN
        coVerify { tested.deleteDir(user1CoreConfig) }
        coVerify { tested.writeText(user1CoreConfig, eventId.id, any()) }
    }
}

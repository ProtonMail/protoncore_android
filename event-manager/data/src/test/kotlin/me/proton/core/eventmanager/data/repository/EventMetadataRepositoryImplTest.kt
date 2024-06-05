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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.data.EventManagerQueryMapProvider
import me.proton.core.eventmanager.data.api.EventApi
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import org.junit.Test
import java.nio.file.Files
import java.util.Optional
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class EventMetadataRepositoryImplTest : CoroutinesTest by UnconfinedCoroutinesTest() {

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

    private val eventApi = mockk<EventApi>(relaxed = true)
    private val sessionProvider = mockk<SessionProvider> {
        coEvery { this@mockk.getSessionId(userId1) } returns SessionId("userId1-sessionId")
    }
    private val apiManagerFactory = mockk<ApiManagerFactory> {
        every { this@mockk.create( any(), interfaceClass = EventApi::class ) } returns TestApiManager(eventApi)
    }

    private val db = mockk<EventMetadataDatabase>(relaxed = true)
    private val eventManagerQueryMapProvider = mockk<EventManagerQueryMapProvider>(relaxed = true)

    private lateinit var apiProvider: ApiProvider
    private lateinit var tested: EventMetadataRepositoryImpl

    private fun newMetadata(
        config: EventManagerConfig,
        eventId: EventId
    ) = EventMetadata.newFrom(config).copy(eventId = eventId)

    private fun newEventsResponse(
        config: EventManagerConfig
    ) = EventsResponse(config.toString())

    @BeforeTest
    fun before() {
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, dispatchers)
        tested = spyk(EventMetadataRepositoryImpl(context, db, apiProvider, Optional.of(eventManagerQueryMapProvider)))
    }

    @AfterTest
    fun clean() {
        runBlocking { tested.deleteAll() }
    }

    @Test
    fun multipleConcurrentConfigWrite() = runTest {
        // WHEN
        tested.writeText(user1CoreConfig, eventId, "user1CoreConfig")
        tested.writeText(user2CoreConfig, eventId, "user2CoreConfig")
        tested.writeText(user1Cal1Config, eventId, "user1Calendar1Config")
        tested.writeText(user1Cal2Config, eventId, "user1Calendar2Config")
        tested.writeText(user2Cal1Config, eventId, "user2Calendar1Config")

        // THEN
        assertEquals(expected = "user1CoreConfig", actual = tested.readText(user1CoreConfig, eventId))
        assertEquals(expected = "user2CoreConfig", actual = tested.readText(user2CoreConfig, eventId))
        assertEquals(expected = "user1Calendar1Config", actual = tested.readText(user1Cal1Config, eventId))
        assertEquals(expected = "user1Calendar2Config", actual = tested.readText(user1Cal2Config, eventId))
        assertEquals(expected = "user2Calendar1Config", actual = tested.readText(user2Cal1Config, eventId))
    }

    @Test
    fun multipleConcurrentConfigDelete() = runTest {
        // WHEN
        tested.writeText(user1CoreConfig, eventId, "user1CoreConfig")
        tested.writeText(user2CoreConfig, eventId, "user2CoreConfig")
        tested.writeText(user1Cal1Config, eventId, "user1Cal1Config")
        tested.writeText(user1Cal2Config, eventId, "user1Cal2Config")
        tested.writeText(user2Cal1Config, eventId, "user2Cal1Config")

        tested.deleteDir(user1CoreConfig)
        tested.deleteDir(user2Cal1Config)

        // THEN
        assertEquals(expected = null, actual = tested.readText(user1CoreConfig, eventId))
        assertEquals(expected = "user2CoreConfig", actual = tested.readText(user2CoreConfig, eventId))
        assertEquals(expected = "user1Cal1Config", actual = tested.readText(user1Cal1Config, eventId))
        assertEquals(expected = "user1Cal2Config", actual = tested.readText(user1Cal2Config, eventId))
        assertEquals(expected = null, actual = tested.readText(user2Cal1Config, eventId))
    }

    @Test
    fun multipleConcurrentConfigUpdate() = runTest {
        // WHEN
        tested.update(newMetadata(user1CoreConfig, eventId), newEventsResponse(user1CoreConfig))
        tested.update(newMetadata(user2CoreConfig, eventId), newEventsResponse(user2CoreConfig))
        tested.update(newMetadata(user1Cal1Config, eventId), newEventsResponse(user1Cal1Config))
        tested.update(newMetadata(user1Cal2Config, eventId), newEventsResponse(user1Cal2Config))
        tested.update(newMetadata(user2Cal1Config, eventId), newEventsResponse(user2Cal1Config))

        // THEN
        assertEquals(expected = user1CoreConfig.toString(), actual = tested.readText(user1CoreConfig, eventId))
        assertEquals(expected = user2CoreConfig.toString(), actual = tested.readText(user2CoreConfig, eventId))
        assertEquals(expected = user1Cal1Config.toString(), actual = tested.readText(user1Cal1Config, eventId))
        assertEquals(expected = user1Cal2Config.toString(), actual = tested.readText(user1Cal2Config, eventId))
        assertEquals(expected = user2Cal1Config.toString(), actual = tested.readText(user2Cal1Config, eventId))
    }

    @Test
    fun multipleConcurrentConfigUpdateAndDelete() = runTest {
        // WHEN
        tested.update(newMetadata(user1CoreConfig, eventId), newEventsResponse(user1CoreConfig))
        tested.update(newMetadata(user2CoreConfig, eventId), newEventsResponse(user2CoreConfig))
        tested.update(newMetadata(user1Cal1Config, eventId), newEventsResponse(user1Cal1Config))
        tested.update(newMetadata(user1Cal2Config, eventId), newEventsResponse(user1Cal2Config))
        tested.update(newMetadata(user2Cal1Config, eventId), newEventsResponse(user2Cal1Config))

        tested.deleteDir(user1CoreConfig)
        tested.deleteDir(user2Cal1Config)

        // THEN
        assertEquals(expected = null, actual = tested.readText(user1CoreConfig, eventId))
        assertEquals(expected = user2CoreConfig.toString(), actual = tested.readText(user2CoreConfig, eventId))
        assertEquals(expected = user1Cal1Config.toString(), actual = tested.readText(user1Cal1Config, eventId))
        assertEquals(expected = user1Cal2Config.toString(), actual = tested.readText(user1Cal2Config, eventId))
        assertEquals(expected = null, actual = tested.readText(user2Cal1Config, eventId))
    }

    @Test
    fun deleteCallDeleteText() = runTest {
        // WHEN
        tested.delete(user1CoreConfig, eventId)

        // THEN
        coVerify { tested.deleteText(user1CoreConfig, eventId) }
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
        tested.update(newMetadata(user1CoreConfig, eventId), newEventsResponse(user1CoreConfig))

        // THEN
        coVerify { tested.deleteDir(user1CoreConfig) }
        coVerify { tested.writeText(user1CoreConfig, eventId, any()) }
    }

    @Test
    fun getEventsUsesQueryMapFromProviderWhenAvailable() = runTest {
        // GIVEN
        val endpoint = "core/events"
        coEvery { eventManagerQueryMapProvider.getQueryMap(user1CoreConfig) } returns mapOf("MessageCounts" to "1")

        // WHEN
        tested.getEvents(user1CoreConfig, eventId, endpoint)

        // THEN
        coVerify { eventManagerQueryMapProvider.getQueryMap(user1CoreConfig) }
    }
}

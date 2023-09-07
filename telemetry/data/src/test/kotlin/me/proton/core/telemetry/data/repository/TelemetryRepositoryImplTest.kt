/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.telemetry.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.telemetry.data.api.TelemetryApi
import me.proton.core.telemetry.data.db.TelemetryDao
import me.proton.core.telemetry.data.db.TelemetryDatabase
import me.proton.core.telemetry.data.entity.TelemetryEventEntity
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.repository.TelemetryRepository
import me.proton.core.test.android.api.TestApiManager
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TelemetryRepositoryImplTest {

    // region mocks
    private val db = mockk<TelemetryDatabase>(relaxed = true)
    private val dao = mockk<TelemetryDao>(relaxed = true)
    private val api = mockk<TelemetryApi>(relaxed = true)
    private lateinit var repository: TelemetryRepository
    // endregion

    private val userId = UserId("user-id")

    private val sampleEvent =
        TelemetryEvent(
            group = "group",
            name = "name",
            values = mapOf("key" to 0F),
            dimensions = mapOf("dimension" to "value")
        )

    @Before
    fun beforeEveryTest() {
        // GIVEN
        every { db.telemetryDao() } returns dao

        repository = TelemetryRepositoryImpl(
            TelemetryLocalDataSourceImpl(db),
            TelemetryRemoteDataSourceImpl(
                mockk {
                    coEvery { get(TelemetryApi::class, any()) } returns TestApiManager(api)
                }
            )
        )
    }

    @Test
    fun `add new event works properly`() = runTest {
        // GIVEN
        val eventSlot = slot<TelemetryEventEntity>()
        val event = TelemetryEvent(
            group = "group",
            name = "name",
            values = mapOf("key" to 0F),
            dimensions = mapOf("dimension" to "value"),
            timestamp = 0
        )
        // WHEN
        repository.addEvent(userId, event)
        // THEN
        coVerify(exactly = 1) { dao.insertOrUpdate(capture(eventSlot)) }
        val capturedEvent = eventSlot.captured
        assertEquals(
            TelemetryEventEntity(
                userId = userId,
                group = "group",
                name = "name",
                values = """{"key":0.0}""",
                dimensions = """{"dimension":"value"}""",
                timestamp = 0
            ),
            capturedEvent
        )
    }

    @Test
    fun `delete event works properly`() = runTest {
        // GIVEN
        val eventSlot = slot<TelemetryEventEntity>()
        val event = TelemetryEvent(
            group = "group",
            name = "name",
            timestamp = 0
        )
        // WHEN
        repository.deleteEvents(userId, listOf(event))
        // THEN
        coVerify(exactly = 1) { dao.delete(capture(eventSlot)) }
        val capturedEvent = eventSlot.captured
        assertEquals(
            TelemetryEventEntity(
                userId = userId,
                group = "group",
                name = "name",
                values = "{}",
                dimensions = "{}",
                timestamp = 0
            ),
            capturedEvent
        )
    }

    @Test
    fun `delete all events works properly`() = runTest {
        // WHEN
        repository.deleteAllEvents(userId)
        // THEN
        coVerify(exactly = 1) { dao.deleteAll(userId) }
    }

    @Test
    fun `delete multiple events works properly`() = runTest {
        // GIVEN
        val eventSlot = slot<TelemetryEventEntity>()
        val event1 = TelemetryEvent(
            id = 1,
            group = "group",
            name = "name",
            timestamp = 0
        )
        val event2 = TelemetryEvent(
            id = 2,
            group = "group",
            name = "name",
            timestamp = 0
        )
        // WHEN
        repository.deleteEvents(userId, listOf(event1, event2))
        // THEN
        coVerify(exactly = 1) { dao.delete(any(), capture(eventSlot)) }
        val captured = eventSlot.captured
        assertEquals(2, captured.id)
    }

    @Test
    fun `get events with limit works properly`() = runTest {
        // GIVEN
        val limit = 3
        val event1 = TelemetryEventEntity(
            userId = userId,
            id = 1,
            group = "group",
            name = "name",
            values = "{}",
            dimensions = "{}",
            timestamp = 0
        )
        val event2 = TelemetryEventEntity(
            userId = userId,
            id = 2,
            group = "group",
            name = "name",
            values = "{}",
            dimensions = "{}",
            timestamp = 0
        )
        every { dao.getAll(userId, limit) } returns listOf(event1, event2)
        // WHEN
        val result = repository.getEvents(userId, limit)
        // THEN
        coVerify(exactly = 1) { dao.getAll(userId, limit) }
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
    }



    @Test
    fun sendsEvents() = runTest {
        coEvery { api.postDataMetrics(any()) } returns GenericResponse(ResponseCodes.OK)
        repository.sendEvents(null, listOf(sampleEvent))
    }

    @Test
    fun rethrowsError() = runTest {
        coEvery { api.postDataMetrics(any()) } throws
            HttpException(
                Response.error<GenericResponse>(
                    HttpResponseCodes.HTTP_TOO_MANY_REQUESTS,
                    "429 Too many requests".toResponseBody()
                )
            )

        assertFailsWith<ApiException> {
            repository.sendEvents(null, listOf(sampleEvent))
        }
    }
}

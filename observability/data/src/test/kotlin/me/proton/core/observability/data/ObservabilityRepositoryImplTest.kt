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

package me.proton.core.observability.data

import io.mockk.coVerify
import io.mockk.core.ValueClassSupport.boxedValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import me.proton.core.observability.data.db.ObservabilityDao
import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.observability.data.entity.ObservabilityEventEntity
import me.proton.core.observability.data.testing.testObservabilityEventNewFileName
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotal
import me.proton.core.observability.domain.metrics.SignupScreenViewTotalV1
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.HttpStatusLabels
import me.proton.core.util.kotlin.ProtonCoreConfig
import me.proton.core.util.kotlin.serialize
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals

class ObservabilityRepositoryImplTest {

    // region mocks
    private val db = mockk<ObservabilityDatabase>(relaxed = true)
    private val dao = mockk<ObservabilityDao>(relaxed = true)
    private lateinit var repository: ObservabilityRepositoryImpl
//    private val json = mockk<Json>(relaxed = true)
    // endregion

    // region test data
    private val sampleData: ObservabilityData =
        SignupScreenViewTotalV1(SignupScreenViewTotalV1.ScreenId.chooseInternalEmail)
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        mockkObject(ProtonCoreConfig)
        mockkStatic("me.proton.core.util.kotlin.SerializationUtilsKt")
        every { db.observabilityDao() } returns dao
        repository = ObservabilityRepositoryImpl(db)
    }

    @After
    fun afterEveryTest() {
        unmockkObject(ProtonCoreConfig)
        unmockkStatic("me.proton.core.util.kotlin.SerializationUtilsKt")
    }

    @Test
    fun `add new event works properly`() = runTest {
        // GIVEN
        val eventSlot = slot<ObservabilityEventEntity>()
        val eventData = sampleData
        val event = ObservabilityEvent(
            id = 1, timestamp = Instant.MIN, data = eventData
        )
        // WHEN
        repository.addEvent(event)
        // THEN
        coVerify(exactly = 1) { dao.insertOrUpdate(capture(eventSlot)) }
        val capturedEvent = eventSlot.captured
        assertEquals(event.id, capturedEvent.id)
        assertEquals(event.name, capturedEvent.name)
        assertEquals(event.version, capturedEvent.version)
        assertEquals(event.timestamp, capturedEvent.timestamp)
        assertEquals(event.data.serialize(), capturedEvent.data)
    }

    @Test
    fun `delete event works properly`() = runTest {
        // GIVEN
        val eventSlot = slot<ObservabilityEventEntity>()
        val eventData = sampleData
        val event = ObservabilityEvent(
            id = 1, timestamp = Instant.MIN, data = eventData
        )
        // WHEN
        repository.deleteEvent(event)
        // THEN
        coVerify(exactly = 1) { dao.delete(capture(eventSlot)) }
        val capturedEvent = eventSlot.captured
        assertEquals(event.id, capturedEvent.id)
        assertEquals(event.name, capturedEvent.name)
        assertEquals(event.version, capturedEvent.version)
        assertEquals(event.timestamp, capturedEvent.timestamp)
        assertEquals(event.data.serialize(), capturedEvent.data)
    }

    @Test
    fun `delete all events works properly`() = runTest {
        // WHEN
        repository.deleteAllEvents()
        // THEN
        coVerify(exactly = 1) { dao.deleteAll() }
    }

    @Test
    fun `delete multiple events works properly`() = runTest {
        // GIVEN
        val eventSlot = slot<ObservabilityEventEntity>()
        val eventData = sampleData
        val event1 = ObservabilityEvent(
            id = 1, timestamp = Instant.MIN, data = eventData
        )
        val event2 = ObservabilityEvent(
            id = 2, timestamp = Instant.MIN, data = eventData
        )
        // WHEN
        repository.deleteEvents(listOf(event1, event2))
        // THEN
        coVerify(exactly = 1) { dao.delete(any(), capture(eventSlot)) }
        val captured = eventSlot.captured
        assertEquals(2, captured.id)
    }

    @Test
    fun `get events count works properly`() = runTest {
        // WHEN
        repository.getEventCount()
        // THEN
        coVerify(exactly = 1) { dao.getCount() }
    }

    @Test
    fun `get events no limit works properly`() = runTest {
        // GIVEN
        val limit: Int? = null
        val eventData = "test-data"
        val event1 = spyk(
            ObservabilityEventEntity(
                id = 1, name = "test-name-1", version = 1, timestamp = Instant.MIN.epochSecond, data = eventData
            )
        )
        every { event1.toObservabilityEvent() } returns ObservabilityEvent(
            id = 1,
            data = SignupScreenViewTotalV1(SignupScreenViewTotalV1.ScreenId.chooseInternalEmail),
            timestamp = Instant.MIN
        )
        val event2 = spyk(
            ObservabilityEventEntity(
                id = 2, name = "test-name-2", version = 1, timestamp = Instant.MIN.epochSecond, data = eventData
            )
        )
        every { event2.toObservabilityEvent() } returns ObservabilityEvent(
            id = 2,
            data = SignupScreenViewTotalV1(SignupScreenViewTotalV1.ScreenId.chooseInternalEmail),
            timestamp = Instant.MIN
        )
        every { dao.getAll() } returns listOf(event1, event2)
        // WHEN
        val result = repository.getEventsAndSanitizeDb(limit)
        // THEN
        coVerify(exactly = 1) { dao.getAll() }
        coVerify(exactly = 0) { dao.getAll(any()) }
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
    }

    @Test
    fun `get events with limit works properly`() = runTest {
        // GIVEN
        val limit = 3
        val eventData = "test-data"
        val event1 = spyk(
            ObservabilityEventEntity(
                id = 1, name = "test-name-1", version = 1, timestamp = Instant.MIN.epochSecond, data = eventData
            )
        )
        every { event1.toObservabilityEvent() } returns ObservabilityEvent(
            id = 1,
            data = SignupScreenViewTotalV1(SignupScreenViewTotalV1.ScreenId.chooseInternalEmail),
            timestamp = Instant.MIN
        )
        val event2 = spyk(
            ObservabilityEventEntity(
                id = 2, name = "test-name-2", version = 1, timestamp = Instant.MIN.epochSecond, data = eventData
            )
        )
        every { event2.toObservabilityEvent() } returns ObservabilityEvent(
            id = 2,
            data = SignupScreenViewTotalV1(SignupScreenViewTotalV1.ScreenId.chooseInternalEmail),
            timestamp = Instant.MIN
        )
        every { dao.getAll(limit) } returns listOf(event1, event2)
        // WHEN
        val result = repository.getEventsAndSanitizeDb(limit)
        // THEN
        coVerify(exactly = 0) { dao.getAll() }
        coVerify(exactly = 1) { dao.getAll(limit) }
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
    }

    @Test
    fun `new file name serializes properly`() = runTest {
        // GIVEN
        val event = spyk(testObservabilityEventNewFileName)
        every { dao.getAll() } returns listOf(event)
        every { event.toObservabilityEvent() } returns ObservabilityEvent(
            id = 2,
            timestamp = Instant.now(),
            data = SignupFetchDomainsTotal(
                Labels = HttpStatusLabels(status = HttpApiStatus.connectionError),
                Value = 1
            )
        )
        // WHEN
        val result = repository.getEventsAndSanitizeDb()
        // THEN
        assertEquals(1, result.size)
        val resultEvent = result[0]
        val valueLiteral = (resultEvent.data as JsonObject).getValue("Value").boxedValue

        assertEquals("1", valueLiteral.toString())
    }
}

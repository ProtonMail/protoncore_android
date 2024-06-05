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

package me.proton.core.eventmanager.domain

import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.util.kotlin.deserialize
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val emptyEventsResponse = """
    { "Events": [] }
""".trimIndent()

private val eventsResponse = """
    {
        "Events": [ 
            { "Id": "d", "Action": ${Action.Delete.value} },
            { "Id": "c", "Action": ${Action.Create.value} },
            { "Id": "u", "Action": ${Action.Update.value} },
            { "Id": "p", "Action": ${Action.Partial.value} }
        ]
    }
""".trimIndent()

class EventListenerTest {
    private val testUserId = UserId("user_id")
    private val testConfig = EventManagerConfig.Core(testUserId)
    private val testEventMetadata = EventMetadata.newFrom(testConfig)

    private lateinit var tested: EventListener<String, TestResource>

    @BeforeTest
    fun setUp() {
        tested = spyk<EventListener<String, TestResource>>(TestEventListener())
    }

    @Test
    fun notifyPrepare() = runTest {
        // WHEN
        tested.notifyPrepare(testConfig, testEventMetadata, EventsResponse(eventsResponse))

        // THEN
        coVerify { tested.onPrepare(testConfig, listOf(TestResource("c"), TestResource("u"))) }
        verifyMaps()
    }

    @Test
    fun notifyEvents() = runTest {
        // WHEN
        tested.notifyEvents(testConfig, testEventMetadata, EventsResponse(eventsResponse))

        // THEN
        coVerify { tested.onCreate(testConfig, listOf(TestResource("c"))) }
        coVerify { tested.onUpdate(testConfig, listOf(TestResource("u"))) }
        coVerify { tested.onPartial(testConfig, listOf(TestResource("p"))) }
        coVerify { tested.onDelete(testConfig, listOf("d")) }
        verifyMaps()
    }

    @Test
    fun `notifyEvents with empty response`() = runTest {
        // WHEN
        tested.notifyEvents(testConfig, testEventMetadata, EventsResponse(emptyEventsResponse))

        // THEN
        coVerify(exactly = 0) { tested.onCreate(any(), any()) }
        coVerify(exactly = 0) { tested.onUpdate(any(), any()) }
        coVerify(exactly = 0) { tested.onPartial(any(), any()) }
        coVerify(exactly = 0) { tested.onDelete(any(), any()) }

        assertEquals(testEventMetadata, tested.getEventMetadata(testConfig))
        assertTrue(tested.getActionMap(testConfig).isEmpty())
    }

    @Test
    fun notifyResetAll() = runTest {
        // WHEN
        tested.notifyResetAll(testConfig, testEventMetadata, EventsResponse(eventsResponse))

        // THEN
        coVerify { tested.onResetAll(testConfig) }
        verifyMaps()
    }

    @Test
    fun notifySuccess() = runTest {
        // WHEN
        tested.notifySuccess(testConfig, testEventMetadata, EventsResponse(eventsResponse))

        // THEN
        coVerify { tested.onSuccess(testConfig) }
        verifyMaps()
    }

    @Test
    fun notifyFailure() = runTest {
        // WHEN
        tested.notifyFailure(testConfig, testEventMetadata, EventsResponse(eventsResponse))

        // THEN
        coVerify { tested.onFailure(testConfig) }
        verifyMaps()
    }

    @Test
    fun notifyComplete() = runTest {
        // WHEN
        tested.notifyComplete(testConfig, testEventMetadata, EventsResponse(eventsResponse))

        // THEN
        coVerify { tested.onComplete(testConfig) }
        assertTrue(tested.getActionMap(testConfig).isEmpty())
        assertFailsWith<NoSuchElementException> { tested.getEventMetadata(testConfig) }
    }

    private fun verifyMaps() {
        assertEquals(testEventMetadata, tested.getEventMetadata(testConfig))

        assertEquals(
            mapOf(
                Action.Delete to listOf(Event(Action.Delete, "d", TestResource("d"))),
                Action.Create to listOf(Event(Action.Create, "c", TestResource("c"))),
                Action.Update to listOf(Event(Action.Update, "u", TestResource("u"))),
                Action.Partial to listOf(Event(Action.Partial, "p", TestResource("p")))
            ),
            tested.getActionMap(testConfig)
        )
    }
}

private class TestEventListener : EventListener<String, TestResource>() {
    override val type: Type = Type.Core
    override val order: Int = 0

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, TestResource>>? =
        response.body.deserialize<TestEvents>().resourceIds?.map {
            Event(requireNotNull(Action.map[it.action]), it.id, TestResource(it.id))
        }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = block()

    override suspend fun onPrepare(config: EventManagerConfig, entities: List<TestResource>) =
        super.onPrepare(config, entities)

    override suspend fun onCreate(config: EventManagerConfig, entities: List<TestResource>) =
        super.onCreate(config, entities)

    override suspend fun onUpdate(config: EventManagerConfig, entities: List<TestResource>) =
        super.onUpdate(config, entities)

    override suspend fun onPartial(config: EventManagerConfig, entities: List<TestResource>) =
        super.onPartial(config, entities)

    override suspend fun onDelete(config: EventManagerConfig, keys: List<String>) =
        super.onDelete(config, keys)

    override suspend fun onResetAll(config: EventManagerConfig) = super.onResetAll(config)
    override suspend fun onSuccess(config: EventManagerConfig) = super.onSuccess(config)
    override suspend fun onFailure(config: EventManagerConfig) = super.onFailure(config)
    override suspend fun onComplete(config: EventManagerConfig) = super.onComplete(config)
}

@Serializable
private data class TestEvents(
    @SerialName("Events")
    val resourceIds: List<TestEvent>? = null
)

@Serializable
private data class TestEvent(
    @SerialName("Id")
    val id: String,
    @SerialName("Action")
    val action: Int
)

private data class TestResource(val id: String)

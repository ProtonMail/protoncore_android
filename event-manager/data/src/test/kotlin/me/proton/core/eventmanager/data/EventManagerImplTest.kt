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

package me.proton.core.eventmanager.data

import android.database.SQLException
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.eventmanager.data.listener.CalendarEventListener
import me.proton.core.eventmanager.data.listener.ContactEventListener
import me.proton.core.eventmanager.data.listener.UserEventListener
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManager
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerConfigProvider
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.entity.EventIdResponse
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.eventmanager.domain.entity.RefreshType
import me.proton.core.eventmanager.domain.entity.State
import me.proton.core.eventmanager.domain.extension.asCalendar
import me.proton.core.eventmanager.domain.extension.asDrive
import me.proton.core.eventmanager.domain.repository.EventMetadataRepository
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes.APP_VERSION_BAD
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EventManagerImplTest {

    private lateinit var database: EventMetadataDatabase

    private lateinit var eventManagerFactor: EventManagerFactory
    private lateinit var eventManagerConfigProvider: EventManagerConfigProvider
    private lateinit var eventManagerProvider: EventManagerProvider

    private lateinit var appLifecycleProvider: AppLifecycleProvider
    private lateinit var accountManager: AccountManager
    private lateinit var eventWorkerManager: EventWorkerManager
    private lateinit var eventMetadataRepository: EventMetadataRepository

    private lateinit var userEventListener: UserEventListener
    private lateinit var contactEventListener: ContactEventListener
    private lateinit var calendarEventListener: CalendarEventListener
    private lateinit var listeners: Set<EventListener<*, *>>

    private val user1 = Account(
        userId = UserId("user1"),
        username = "user1",
        email = "user1@protonmail.com",
        state = AccountState.Ready,
        sessionId = null,
        sessionState = null,
        details = AccountDetails(null, null)
    )
    private val user2 = Account(
        userId = UserId("user2"),
        username = "user2",
        email = "user2@protonmail.com",
        state = AccountState.Ready,
        sessionId = null,
        sessionState = null,
        details = AccountDetails(null, null)
    )
    private val accounts = listOf(user1, user2)

    private val user1Config = EventManagerConfig.Core(user1.userId)
    private val user2Config = EventManagerConfig.Core(user2.userId)

    private val calendarId = "calendarId"
    private val calendarConfig = EventManagerConfig.Calendar(user1.userId, calendarId)

    private val eventId = "eventId"
    private val appState = MutableStateFlow(AppLifecycleProvider.State.Foreground)

    private lateinit var user1Manager: EventManager
    private lateinit var user2Manager: EventManager
    private lateinit var calendarManager: EventManager

    @Before
    fun before() = runTest {
        database = mockk(relaxed = true) {
            coEvery { inTransaction(captureCoroutine<suspend () -> Any>()) } coAnswers {
                coroutine<suspend () -> Any>().coInvoke()
            }
        }
        userEventListener = spyk(UserEventListener())
        contactEventListener = spyk(ContactEventListener())
        calendarEventListener = spyk(CalendarEventListener())
        listeners = setOf(userEventListener, contactEventListener, calendarEventListener)

        appLifecycleProvider = mockk {
            every { state } returns appState
        }
        accountManager = mockk {
            val userIdSlot = slot<UserId>()
            every { getAccount(capture(userIdSlot)) } answers {
                flowOf(accounts.firstOrNull { it.userId == userIdSlot.captured })
            }
        }
        eventWorkerManager = spyk()
        eventMetadataRepository = spyk()
        eventManagerFactor = mockk {
            val deserializerSlot = slot<EventDeserializer>()
            every { create(capture(deserializerSlot)) } answers {
                EventManagerImpl(
                    TestCoroutineScopeProvider(),
                    appLifecycleProvider,
                    accountManager,
                    eventWorkerManager,
                    database,
                    eventMetadataRepository,
                    deserializerSlot.captured
                )
            }
        }

        eventManagerConfigProvider = EventManagerConfigProviderImpl(eventMetadataRepository)
        eventManagerProvider = EventManagerProviderImpl(eventManagerFactor, eventManagerConfigProvider, listeners)
        user1Manager = eventManagerProvider.get(user1Config)
        user2Manager = eventManagerProvider.get(user2Config)
        calendarManager = eventManagerProvider.get(calendarConfig)

        coEvery { eventWorkerManager.isRunning(any()) } returns true

        coEvery { eventMetadataRepository.getLatestEventId(any(), any()) } returns
            EventIdResponse("{ \"EventID\": \"$eventId\" }")

        coEvery { eventMetadataRepository.getEvents(any(), any(), any()) } returns
            EventsResponse(TestEvents.coreFullEventsResponse)

        coEvery { eventMetadataRepository.getEvents(any(), any()) } returns
            EventsResponse(TestEvents.coreFullEventsResponse)

        coEvery { eventMetadataRepository.update(any(), any()) } returns Unit
        coEvery { eventMetadataRepository.updateMetadata(any()) } returns Unit
        coEvery { eventMetadataRepository.updateState(any(), any(), any()) } returns Unit

        // GIVEN
        coEvery { eventMetadataRepository.get(user1Config) } returns
            listOf(EventMetadata(user1.userId, EventId(eventId), user1Config, createdAt = 1))

        coEvery { eventMetadataRepository.get(user2Config) } returns
            listOf(EventMetadata(user2.userId, EventId(eventId), user2Config, createdAt = 1))

        coEvery { eventMetadataRepository.get(calendarConfig) } returns
            listOf(EventMetadata(user1.userId, EventId(eventId), calendarConfig, createdAt = 1))
    }

    @Test
    fun callCorrectPrepareUpdateDeleteCreateForUser1() = runTest {
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(exactly = 1) { userEventListener.inTransaction(any()) }
        coVerify(exactly = 1) { contactEventListener.inTransaction(any()) }
        coVerify(ordering = Ordering.ORDERED) {
            userEventListener.onPrepare(user1Config, any())
            userEventListener.onUpdate(user1Config, any())
            userEventListener.onSuccess(user1Config)
            userEventListener.onComplete(user1Config)
        }
        coVerify(exactly = 0) {
            userEventListener.onDelete(user1Config, any())
            userEventListener.onCreate(user1Config, any())
            userEventListener.onPartial(user1Config, any())
            userEventListener.onFailure(user1Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            contactEventListener.onPrepare(user1Config, any())
            contactEventListener.onCreate(user1Config, any())
            contactEventListener.onSuccess(user1Config)
            contactEventListener.onComplete(user1Config)
        }
        coVerify(exactly = 0) {
            contactEventListener.onUpdate(user1Config, any())
            contactEventListener.onDelete(user1Config, any())
            contactEventListener.onPartial(user1Config, any())
            contactEventListener.onFailure(user1Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user1Config, any(), State.Persisted)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyPrepare)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyEvents)
            eventMetadataRepository.updateState(user1Config, any(), State.Success)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifySuccess)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyComplete)
            eventMetadataRepository.updateState(user1Config, any(), State.Completed)
        }
    }

    @Test
    fun callCorrectPrepareUpdateDeleteCreateForUser2() = runTest {
        // WHEN
        user2Manager.process()
        // THEN
        coVerify(exactly = 1) { userEventListener.inTransaction(any()) }
        coVerify(exactly = 1) { contactEventListener.inTransaction(any()) }
        coVerify(ordering = Ordering.ORDERED) {
            userEventListener.onPrepare(user2Config, any())
            userEventListener.onUpdate(user2Config, any())
            userEventListener.onSuccess(user2Config)
            userEventListener.onComplete(user2Config)
        }
        coVerify(exactly = 0) {
            userEventListener.onDelete(user2Config, any())
            userEventListener.onCreate(user2Config, any())
            userEventListener.onPartial(user2Config, any())
            userEventListener.onFailure(user2Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            contactEventListener.onPrepare(user2Config, any())
            contactEventListener.onCreate(user2Config, any())
            contactEventListener.onSuccess(user2Config)
            contactEventListener.onComplete(user2Config)

        }
        coVerify(exactly = 0) {
            contactEventListener.onUpdate(user2Config, any())
            contactEventListener.onDelete(user2Config, any())
            contactEventListener.onPartial(user2Config, any())
            contactEventListener.onFailure(user2Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user2Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user2Config, any(), State.Persisted)
            eventMetadataRepository.updateState(user2Config, any(), State.NotifyPrepare)
            eventMetadataRepository.updateState(user2Config, any(), State.NotifyEvents)
            eventMetadataRepository.updateState(user2Config, any(), State.Success)
            eventMetadataRepository.updateState(user2Config, any(), State.NotifySuccess)
            eventMetadataRepository.updateState(user2Config, any(), State.NotifyComplete)
            eventMetadataRepository.updateState(user2Config, any(), State.Completed)
        }
    }

    @Test
    fun callCorrectSuccess() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.get(user1Config) } returns listOf(
            EventMetadata(
                user1.userId, EventId(eventId), user1Config,
                createdAt = 1,
                state = State.Success
            )
        )
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            userEventListener.onSuccess(user1Config)
            contactEventListener.onSuccess(user1Config)
            userEventListener.onComplete(user1Config)
            contactEventListener.onComplete(user1Config)
        }
        coVerify(exactly = 0) {
            userEventListener.onPrepare(user1Config, any())
            userEventListener.onUpdate(user1Config, any())
            userEventListener.onDelete(user1Config, any())
            userEventListener.onCreate(user1Config, any())
            userEventListener.onPartial(user1Config, any())
            userEventListener.onFailure(user1Config)

            contactEventListener.onPrepare(user1Config, any())
            contactEventListener.onCreate(user1Config, any())
            contactEventListener.onUpdate(user1Config, any())
            contactEventListener.onDelete(user1Config, any())
            contactEventListener.onPartial(user1Config, any())
            contactEventListener.onFailure(user1Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.NotifySuccess)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyComplete)
            eventMetadataRepository.updateState(user1Config, any(), State.Completed)
        }
    }

    @Test
    fun callNotifyFailure() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.get(user1Config) } returns listOf(
            EventMetadata(
                user1.userId, EventId(eventId), user1Config,
                createdAt = 1,
                state = State.NotifyPrepare,
                retry = EventManagerImpl.retriesBeforeNotifyFailure + 1
            )
        )
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            userEventListener.onFailure(user1Config)
            contactEventListener.onFailure(user1Config)
            userEventListener.onResetAll(user1Config)
            contactEventListener.onResetAll(user1Config)
            userEventListener.onComplete(user1Config)
            contactEventListener.onComplete(user1Config)
        }
        coVerify(exactly = 0) {
            userEventListener.onPrepare(user1Config, any())
            userEventListener.onUpdate(user1Config, any())
            userEventListener.onDelete(user1Config, any())
            userEventListener.onCreate(user1Config, any())
            userEventListener.onPartial(user1Config, any())
            userEventListener.onSuccess(user1Config)

            contactEventListener.onPrepare(user1Config, any())
            contactEventListener.onCreate(user1Config, any())
            contactEventListener.onUpdate(user1Config, any())
            contactEventListener.onDelete(user1Config, any())
            contactEventListener.onPartial(user1Config, any())
            contactEventListener.onSuccess(user1Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyFailure)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyResetAll)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyComplete)
            eventMetadataRepository.updateState(user1Config, any(), State.Completed)
        }
    }

    @Test
    fun callNotifyResetAll() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.get(user1Config) } returns listOf(
            EventMetadata(
                user1.userId, EventId(eventId), user1Config,
                createdAt = 1,
                state = State.Fetching,
                retry = EventManagerImpl.retriesBeforeNotifyResetAll + 1
            )
        )
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            userEventListener.onResetAll(user1Config)
            contactEventListener.onResetAll(user1Config)
            userEventListener.onComplete(user1Config)
            contactEventListener.onComplete(user1Config)
        }
        coVerify(exactly = 0) {
            userEventListener.onPrepare(user1Config, any())
            userEventListener.onUpdate(user1Config, any())
            userEventListener.onDelete(user1Config, any())
            userEventListener.onCreate(user1Config, any())
            userEventListener.onPartial(user1Config, any())
            userEventListener.onSuccess(user1Config)
            userEventListener.onFailure(user1Config)

            contactEventListener.onPrepare(user1Config, any())
            contactEventListener.onCreate(user1Config, any())
            contactEventListener.onUpdate(user1Config, any())
            contactEventListener.onDelete(user1Config, any())
            contactEventListener.onPartial(user1Config, any())
            contactEventListener.onSuccess(user1Config)
            contactEventListener.onFailure(user1Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyResetAll)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyComplete)
            eventMetadataRepository.updateState(user1Config, any(), State.Completed)
        }
    }

    @Test
    fun callDeleteAllMetadata() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.get(user1Config) } returns listOf(
            EventMetadata(
                user1.userId, EventId(eventId), user1Config,
                createdAt = 1,
                state = State.Fetching,
                retry = EventManagerImpl.retriesBeforeDeleteAllMetadata + 1
            )
        )
        // WHEN
        user1Manager.process()
        // THEN
        coVerify { eventMetadataRepository.deleteAll(user1Config) }
    }

    @Test
    fun callCorrectFetchPrepareUpdateCreateForUser1() = runTest {
        // GIVEN
        val event = EventMetadata(user1.userId, EventId(eventId), user1Config, createdAt = 1, state = State.Fetching)
        coEvery { eventMetadataRepository.get(user1Config) } returns listOf(event)
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            userEventListener.onPrepare(user1Config, any())
            userEventListener.onUpdate(user1Config, any())
            userEventListener.onSuccess(user1Config)
            userEventListener.onComplete(user1Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            contactEventListener.onPrepare(user1Config, any())
            contactEventListener.onCreate(user1Config, any())
            contactEventListener.onSuccess(user1Config)
            contactEventListener.onComplete(user1Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user1Config, any(), State.Persisted)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyPrepare)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyEvents)
            eventMetadataRepository.updateState(user1Config, any(), State.Success)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifySuccess)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyComplete)
            eventMetadataRepository.updateState(user1Config, any(), State.Completed)
        }
    }

    @Test
    fun callFetchThrowException() = runTest {
        // GIVEN
        val event = EventMetadata(user1.userId, EventId(eventId), user1Config, createdAt = 1, state = State.Fetching)
        coEvery { eventMetadataRepository.get(user1Config) } returns listOf(event)
        coEvery { eventMetadataRepository.getEvents(any(), any(), any()) } throws IllegalStateException("NotRetryable")
        coEvery { eventMetadataRepository.getEvents(any(), any()) }  throws IllegalStateException("NotRetryable")
        // WHEN
        assertFailsWith<IllegalStateException> {
            user1Manager.process()
        }
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user1Config, any(), State.Enqueued)
        }
    }

    @Test
    fun callOnPrepareThrowException() = runTest {
        // GIVEN
        coEvery { userEventListener.onPrepare(user1Config, any()) } throws Exception("IOException")
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(exactly = 0) { userEventListener.inTransaction(any()) }
        coVerify(exactly = 0) { userEventListener.onUpdate(user1Config, any()) }
        coVerify(exactly = 0) { userEventListener.onSuccess(user1Config) }
        coVerify(atLeast = 1) { eventMetadataRepository.updateState(any(), any(), State.NotifyPrepare) }
        coVerify(exactly = 0) { eventMetadataRepository.updateState(any(), any(), State.NotifyEvents) }
        coVerify(exactly = 0) { eventMetadataRepository.updateState(any(), any(), State.Success) }
        coVerify(atLeast = 1) { eventWorkerManager.enqueue(any(), true) }
    }

    @Test
    fun callOnUpdateThrowException() = runTest {
        // GIVEN
        coEvery { userEventListener.onUpdate(user1Config, any()) } throws Exception("SqlForeignKeyException")
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(atLeast = 1) { userEventListener.inTransaction(any()) }
        coVerify(atLeast = 1) { userEventListener.onUpdate(user1Config, any()) }
        coVerify(exactly = 0) { userEventListener.onSuccess(user1Config) }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(any(), any(), State.NotifyPrepare)
            eventMetadataRepository.updateState(any(), any(), State.NotifyEvents)
        }
        coVerify(exactly = 0) { eventMetadataRepository.updateState(any(), any(), State.Success) }
        coVerify(atLeast = 1) { eventMetadataRepository.updateMetadata(any()) }
        coVerify(atLeast = 1) { eventWorkerManager.enqueue(any(), true) }
    }

    @Test
    fun callOnSuccessThrowException() = runTest {
        // GIVEN
        coEvery { userEventListener.onSuccess(user1Config) } throws ApiException(ApiResult.Error.Connection())
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(atLeast = 1) { userEventListener.inTransaction(any()) }
        coVerify(atLeast = 1) { userEventListener.onUpdate(user1Config, any()) }
        coVerify(exactly = 1) { userEventListener.onSuccess(user1Config) }
        coVerify(exactly = 0) { userEventListener.onComplete(user1Config) }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(any(), any(), State.NotifyPrepare)
            eventMetadataRepository.updateState(any(), any(), State.NotifyEvents)
            eventMetadataRepository.updateState(any(), any(), State.Success)
            eventMetadataRepository.updateState(any(), any(), State.NotifySuccess)
            eventMetadataRepository.updateState(any(), any(), State.Success)
        }
        coVerify(exactly = 0) { eventMetadataRepository.updateState(any(), any(), State.Completed) }
        coVerify(atLeast = 1) { eventMetadataRepository.updateMetadata(any()) }
        coVerify(atLeast = 1) { eventWorkerManager.enqueue(any(), true) }
    }

    @Test
    fun callOnCompleteThrowException() = runTest {
        // GIVEN
        coEvery { userEventListener.onComplete(user1Config) } throws ApiException(ApiResult.Error.Connection())
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(atLeast = 1) { userEventListener.inTransaction(any()) }
        coVerify(atLeast = 1) { userEventListener.onUpdate(user1Config, any()) }
        coVerify(exactly = 1) { userEventListener.onSuccess(user1Config) }
        coVerify(exactly = 1) { userEventListener.onComplete(user1Config) }
        coVerify(exactly = 0) { userEventListener.onResetAll(user1Config) }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(any(), any(), State.NotifyPrepare)
            eventMetadataRepository.updateState(any(), any(), State.NotifyEvents)
            eventMetadataRepository.updateState(any(), any(), State.Success)
            eventMetadataRepository.updateState(any(), any(), State.NotifySuccess)
            eventMetadataRepository.updateState(any(), any(), State.NotifyComplete)
            eventMetadataRepository.updateState(any(), any(), State.Completed)
        }
        coVerify(atLeast = 1) { eventMetadataRepository.updateMetadata(any()) }
        coVerify(atLeast = 1) { eventWorkerManager.enqueue(any(), immediately = false) }
    }

    @Test
    fun callOnResetAllThrowException() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.get(user1Config) } returns listOf(
            EventMetadata(
                user1.userId, EventId(eventId), user1Config,
                createdAt = 1,
                state = State.Persisted,
                refresh = RefreshType.All
            )
        )
        coEvery { userEventListener.onResetAll(user1Config) } throws Exception()
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(atLeast = 1) { eventMetadataRepository.updateState(any(), any(), State.NotifyResetAll) }
        coVerify(exactly = 0) { eventMetadataRepository.updateState(any(), any(), State.NotifyComplete) }
        coVerify(exactly = 0) { eventMetadataRepository.updateState(any(), any(), State.Completed) }
        coVerify(atLeast = 1) { eventWorkerManager.enqueue(any(), true) }
    }

    @Test
    fun callOnResetAllCallFirstGetLatestEventId() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.get(user1Config) } returns listOf(
            EventMetadata(
                user1.userId, EventId(eventId), user1Config,
                nextEventId = null,
                createdAt = 1,
                state = State.NotifyResetAll,
                refresh = RefreshType.All
            )
        )
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.getLatestEventId(any(), any())
            eventMetadataRepository.updateState(any(), any(), State.NotifyComplete)
            eventMetadataRepository.updateState(any(), any(), State.Completed)
        }
    }

    @Test
    fun callOnResetAllDoNotCallFirstGetLatestEventIdIfNextEventIdIsNotNull() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.get(user1Config) } returns listOf(
            EventMetadata(
                user1.userId, EventId(eventId), user1Config,
                nextEventId = EventId("nextEventId"),
                createdAt = 1,
                state = State.NotifyResetAll,
                refresh = RefreshType.All
            )
        )
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(exactly = 0) { eventMetadataRepository.getLatestEventId(any(), any()) }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(any(), any(), State.NotifyResetAll)
            eventMetadataRepository.updateState(any(), any(), State.NotifyComplete)
            eventMetadataRepository.updateState(any(), any(), State.Completed)
        }
    }

    @Test
    fun preventMultiSubscribe() = runTest {
        // GIVEN
        user1Manager.subscribe(userEventListener)
        user1Manager.subscribe(userEventListener)
        user1Manager.subscribe(userEventListener)
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(exactly = 1) { userEventListener.onPrepare(user1Config, any()) }
        coVerify(exactly = 1) { userEventListener.onUpdate(user1Config, any()) }
        coVerify(exactly = 0) { userEventListener.onDelete(user1Config, any()) }
        coVerify(exactly = 0) { userEventListener.onCreate(user1Config, any()) }
        coVerify(exactly = 0) { userEventListener.onPartial(user1Config, any()) }
    }

    @Test
    fun preventEventIfNoUser() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.get(user1Config) } returns emptyList()
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(exactly = 0) { userEventListener.onPrepare(any(), any()) }
        coVerify(exactly = 0) { userEventListener.onUpdate(any(), any()) }
        coVerify(exactly = 0) { userEventListener.onDelete(any(), any()) }
        coVerify(exactly = 0) { userEventListener.onCreate(any(), any()) }
        coVerify(exactly = 0) { userEventListener.onPartial(any(), any()) }
    }

    @Test
    fun tryCastEventManagerConfig() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.getEvents(any(), any(), any()) } returns
            EventsResponse(TestEvents.calendarFullEventsResponse)
        coEvery { eventMetadataRepository.getEvents(any(), any()) } returns
            EventsResponse(TestEvents.calendarFullEventsResponse)
        // WHEN
        calendarManager.process()
        // THEN
        coVerify(exactly = 1) { calendarEventListener.onPrepare(any(), any()) }
        coVerify(exactly = 0) { calendarEventListener.onUpdate(any(), any()) }
        coVerify(exactly = 0) { calendarEventListener.onDelete(any(), any()) }
        coVerify(exactly = 1) { calendarEventListener.onCreate(any(), any()) }
        coVerify(exactly = 0) { calendarEventListener.onPartial(any(), any()) }
        assertFailsWith(ClassCastException::class) {
            calendarEventListener.config.asDrive()
        }
        assertEquals(calendarId, calendarEventListener.config.asCalendar().calendarId)
    }

    @Test
    fun fetchThrowApiExceptionForceUpdate() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.getEvents(any(), any(), any()) } throws ApiException(
            ApiResult.Error.Http(400, "Bad Request", ApiResult.Error.ProtonData(APP_VERSION_BAD, "Please Update"))
        )
        // WHEN
        assertFailsWith<ApiException> {
            user1Manager.process()
        }
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user1Config, any(), State.Enqueued)
        }
        // Worker will retry.
    }

    @Test
    fun fetchThrowApiExceptionIsUnauthorized() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.getEvents(any(), any(), any()) } throws ApiException(
            ApiResult.Error.Http(401, "Unauthorized")
        )
        // WHEN
        assertFailsWith<ApiException> {
            user1Manager.process()
        }
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user1Config, any(), State.Enqueued)
        }
        // Worker handle it as a retry, but will be cancelled on user force logout.
    }

    @Test
    fun fetchThrowApiExceptionRetryable() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.getEvents(any(), any(), any()) } throws ApiException(
            ApiResult.Error.Connection()
        )
        // WHEN
        assertFailsWith<ApiException> {
            user1Manager.process()
        }
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user1Config, any(), State.Enqueued)
        }
        // Worker will retry.
    }

    @Test
    fun fetchThrowApiExceptionRetryableMisdirected() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.getEvents(any(), any(), any()) } throws ApiException(
            ApiResult.Error.Http(421, "Misdirected Request")
        )
        // WHEN
        assertFailsWith<ApiException> {
            user1Manager.process()
        }
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user1Config, any(), State.Enqueued)
        }
        // Worker will retry.
    }

    @Test
    fun fetchThrowException() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.getEvents(any(), any(), any()) } throws Exception()
        // WHEN
        assertFailsWith<Exception> {
            user1Manager.process()
        }
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user1Config, any(), State.Enqueued)
        }
        // Worker will retry.
    }

    @Test
    fun fetchThrowApiExceptionNotRetryable() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.getEvents(any(), any(), any()) } throws ApiException(
            ApiResult.Error.Http(404, "Not Found")
        )
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            userEventListener.onResetAll(user1Config)
            userEventListener.onComplete(user1Config)
        }
    }

    @Test
    fun getNextActionUpdateMetadataThrowExceptionThenNoEnqueue() = runTest {
        // GIVEN
        coEvery { eventMetadataRepository.updateMetadata(any()) } throws SQLException("Error")
        // WHEN
        user1Manager.process()
        // THEN
        coVerify(exactly = 1) { userEventListener.inTransaction(any()) }
        coVerify(exactly = 1) { contactEventListener.inTransaction(any()) }
        coVerify(ordering = Ordering.ORDERED) {
            userEventListener.onPrepare(user1Config, any())
            userEventListener.onUpdate(user1Config, any())
            userEventListener.onSuccess(user1Config)
            userEventListener.onComplete(user1Config)
        }
        coVerify(ordering = Ordering.ORDERED) {
            eventMetadataRepository.updateState(user1Config, any(), State.Fetching)
            eventMetadataRepository.updateState(user1Config, any(), State.Persisted)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyPrepare)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyEvents)
            eventMetadataRepository.updateState(user1Config, any(), State.Success)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifySuccess)
            eventMetadataRepository.updateState(user1Config, any(), State.NotifyComplete)
            eventMetadataRepository.updateState(user1Config, any(), State.Completed)
        }
        coVerify(exactly = 0) {
            eventWorkerManager.enqueue(any(), any())
        }
    }
}

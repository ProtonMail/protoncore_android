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

package me.proton.core.observability.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.observability.domain.ObservabilityRepository
import me.proton.core.observability.domain.ObservabilityWorkerManager
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import me.proton.core.observability.domain.usecase.SendObservabilityEvents
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class ObservabilityWorkerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var hiltWorkerFactory: HiltWorkerFactory

    @BindValue
    internal lateinit var isObservabilityEnabled: IsObservabilityEnabled

    @BindValue
    internal lateinit var observabilityWorkerManager: ObservabilityWorkerManager

    @BindValue
    internal lateinit var repository: ObservabilityRepository

    @BindValue
    internal lateinit var sendObservabilityEvents: SendObservabilityEvents

    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        isObservabilityEnabled = mockk()
        observabilityWorkerManager = mockk(relaxUnitFun = true)
        repository = mockk(relaxUnitFun = true)
        sendObservabilityEvents = mockk(relaxUnitFun = true)
    }


    @Test
    fun observabilityIsDisabled() {
        coEvery { isObservabilityEnabled.invoke() } returns false

        val result = makeAndRunWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 0) { sendObservabilityEvents.invoke(any()) }
        coVerify { observabilityWorkerManager.setLastSentNow() }
        coVerify { repository.deleteAllEvents() }
    }

    @Test
    fun noEvents() {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { repository.getEvents(any()) } returns emptyList()

        val result = makeAndRunWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 0) { sendObservabilityEvents.invoke(any()) }
    }

    @Test
    fun singleEvent() {
        val events = listOf(mockk<ObservabilityEvent>())

        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { repository.getEvents(any()) } returns events andThen emptyList()

        val result = makeAndRunWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { sendObservabilityEvents.invoke(events) }
        coVerify(exactly = 1) { repository.deleteEvents(events) }
        coVerify(exactly = 1) { observabilityWorkerManager.setLastSentNow() }
    }

    @Test
    fun batching() {
        val events1 = listOf(mockk<ObservabilityEvent>())
        val events2 = listOf(mockk<ObservabilityEvent>())

        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { repository.getEvents(any()) }.returnsMany(events1, events2, emptyList())

        val result = makeAndRunWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { sendObservabilityEvents.invoke(events1) }
        coVerify(exactly = 1) { repository.deleteEvents(events1) }
        coVerify(exactly = 1) { sendObservabilityEvents.invoke(events2) }
        coVerify(exactly = 1) { repository.deleteEvents(events2) }

        coVerify(exactly = 2) { sendObservabilityEvents.invoke(any()) }
        coVerify(exactly = 2) { repository.deleteEvents(any()) }

        coVerify(exactly = 1) { observabilityWorkerManager.setLastSentNow() }
    }

    @Test
    fun retryableError() {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { repository.getEvents(any()) } returns listOf(mockk())
        coEvery { sendObservabilityEvents.invoke(any()) } throws
            ApiException(ApiResult.Error.Http(HttpResponseCodes.HTTP_TOO_MANY_REQUESTS, "Error"))

        val result = makeAndRunWorker()
        assertEquals(ListenableWorker.Result.retry(), result)

        coVerify(exactly = 0) { observabilityWorkerManager.setLastSentNow() }
    }

    @Test
    fun unrecoverableHttpError() {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { repository.getEvents(any()) } returns listOf(mockk())
        coEvery { sendObservabilityEvents.invoke(any()) } throws
            ApiException(ApiResult.Error.Http(HttpResponseCodes.HTTP_BAD_REQUEST, "Error"))

        val result = makeAndRunWorker()
        assertIs<ListenableWorker.Result.Failure>(result)

        coVerify(exactly = 0) { observabilityWorkerManager.setLastSentNow() }
    }

    @Test
    fun unrecoverableError() {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { repository.getEvents(any()) } returns listOf(mockk())
        coEvery { sendObservabilityEvents.invoke(any()) } throws Throwable("Unknown error")

        val result = makeAndRunWorker()
        assertIs<ListenableWorker.Result.Failure>(result)

        coVerify(exactly = 0) { observabilityWorkerManager.setLastSentNow() }
    }

    private fun makeWorker(): ObservabilityWorker = TestListenableWorkerBuilder<ObservabilityWorker>(context)
        .setWorkerFactory(hiltWorkerFactory)
        .build()

    private fun makeAndRunWorker(): ListenableWorker.Result = runBlocking { makeWorker().doWork() }
}

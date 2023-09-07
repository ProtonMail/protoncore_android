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

package me.proton.core.telemetry.data.worker

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
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.telemetry.domain.repository.TelemetryRepository
import me.proton.core.telemetry.domain.TelemetryWorkerManager
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.usecase.IsTelemetryEnabled
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
class TelemetryWorkerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var hiltWorkerFactory: HiltWorkerFactory

    @BindValue
    internal lateinit var isTelemetryEnabled: IsTelemetryEnabled

    @BindValue
    internal lateinit var telemetryWorkerManager: TelemetryWorkerManager

    @BindValue
    internal lateinit var repository: TelemetryRepository

    private lateinit var context: Context

    private val userId = UserId("user-id")

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        isTelemetryEnabled = mockk()
        telemetryWorkerManager = mockk(relaxUnitFun = true)
        repository = mockk(relaxUnitFun = true)
    }


    @Test
    fun telemetryIsDisabled() {
        coEvery { isTelemetryEnabled(userId) } returns false

        val result = makeAndRunWorker(userId)
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 0) { repository.sendEvents(userId, any()) }
        coVerify { repository.deleteAllEvents(userId) }
    }

    @Test
    fun noEvents() {
        coEvery { isTelemetryEnabled(userId) } returns true
        coEvery { repository.getEvents(userId, any()) } returns emptyList()

        val result = makeAndRunWorker(userId)
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 0) { repository.sendEvents(userId, any()) }
    }

    @Test
    fun singleEvent() {
        val events = listOf(mockk<TelemetryEvent>())

        coEvery { isTelemetryEnabled(userId) } returns true
        coEvery { repository.getEvents(userId, any()) } returns events andThen emptyList()

        val result = makeAndRunWorker(userId)
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { repository.sendEvents(userId, events) }
        coVerify(exactly = 1) { repository.deleteEvents(userId, events) }
    }

    @Test
    fun unAuthSingleEvent() {
        val events = listOf(mockk<TelemetryEvent>())

        coEvery { isTelemetryEnabled(null) } returns true
        coEvery { repository.getEvents(null, limit = any()) } returns events andThen emptyList()

        val result = makeAndRunWorker(null)
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { repository.sendEvents(null, events) }
        coVerify(exactly = 1) { repository.deleteEvents(null, events) }
    }

    @Test
    fun batching() {
        val events1 = listOf(mockk<TelemetryEvent>())
        val events2 = listOf(mockk<TelemetryEvent>())

        coEvery { isTelemetryEnabled(userId) } returns true
        coEvery { repository.getEvents(userId, any()) }.returnsMany(events1, events2, emptyList())

        val result = makeAndRunWorker(userId)
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { repository.sendEvents(userId, events1) }
        coVerify(exactly = 1) { repository.deleteEvents(userId, events1) }
        coVerify(exactly = 1) { repository.sendEvents(userId, events2) }
        coVerify(exactly = 1) { repository.deleteEvents(userId, events2) }

        coVerify(exactly = 2) { repository.sendEvents(userId, any()) }
        coVerify(exactly = 2) { repository.deleteEvents(userId, any()) }
    }

    @Test
    fun retryableError() {
        coEvery { isTelemetryEnabled(userId) } returns true
        coEvery { repository.getEvents(userId, any()) } returns listOf(mockk())
        coEvery { repository.sendEvents(userId, any()) } throws
            ApiException(ApiResult.Error.Http(HttpResponseCodes.HTTP_TOO_MANY_REQUESTS, "Error"))

        val result = makeAndRunWorker(userId)
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun unrecoverableHttpError() {
        coEvery { isTelemetryEnabled(userId) } returns true
        coEvery { repository.getEvents(userId, any()) } returns listOf(mockk())
        coEvery { repository.sendEvents(userId, any()) } throws
            ApiException(ApiResult.Error.Http(HttpResponseCodes.HTTP_BAD_REQUEST, "Error"))

        val result = makeAndRunWorker(userId)
        assertIs<ListenableWorker.Result.Failure>(result)
    }

    @Test
    fun unrecoverableError() {
        coEvery { isTelemetryEnabled(userId) } returns true
        coEvery { repository.getEvents(userId, any()) } returns listOf(mockk())
        coEvery { repository.sendEvents(userId, any()) } throws Throwable("Unknown error")

        val result = makeAndRunWorker(userId)
        assertIs<ListenableWorker.Result.Failure>(result)
    }

    private fun makeWorker(userId: UserId?): TelemetryWorker = TestListenableWorkerBuilder<TelemetryWorker>(context)
        .setWorkerFactory(hiltWorkerFactory)
        .setInputData(TelemetryWorker.makeInputData(userId))
        .build()

    private fun makeAndRunWorker(userId: UserId?): ListenableWorker.Result = runBlocking { makeWorker(userId).doWork() }
}

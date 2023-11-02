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

package me.proton.core.eventmanager.data.work

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.utils.futures.SettableFuture
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.data.R
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.presentation.app.AppLifecycleProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@Suppress("MaxLineLength")
internal class EventWorkerManagerImplTest {

    @get:Rule
    val mockKRule = MockKRule(this)

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var workManager: WorkManager

    @MockK
    lateinit var appLifecycleProvider: AppLifecycleProvider

    @MockK
    lateinit var usageStatsManager: UsageStatsManager

    @InjectMockKs
    lateinit var manager: EventWorkerManagerImpl

    private val config = EventManagerConfig.Core(UserId("user-id"))
    private val futureWorkInfoList = SettableFuture.create<MutableList<WorkInfo>>()

    private val futureOperation = SettableFuture.create<Operation.State.SUCCESS>()
    private val operation = mockk<Operation> { coEvery { this@mockk.result } returns futureOperation }

    private fun createWorkInfo(state: WorkInfo.State) = WorkInfo(
        /* id = */ UUID.randomUUID(),
        /* state = */ state,
        /* outputData = */ Data.EMPTY,
        /* tags = */ emptyList(),
        /* progress = */ Data.EMPTY,
        /* runAttemptCount = */ 0,
        /* generation = */ 0
    )

    @Before
    fun setup() {
        every { context.resources } returns mockk {
            every { getInteger(R.integer.core_feature_event_manager_worker_immediate_minimum_initial_delay_seconds) } returns 0
            every { getBoolean(R.bool.core_feature_event_manager_worker_repeat_internal_background_by_bucket) } returns false
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_background_seconds) } returns 1800
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_foreground_seconds) } returns 30
            every { getInteger(R.integer.core_feature_event_manager_worker_backoff_delay_seconds) } returns 30
            every { getBoolean(R.bool.core_feature_event_manager_worker_requires_storage_not_low) } returns false
            every { getBoolean(R.bool.core_feature_event_manager_worker_requires_battery_not_low) } returns false
        }
        every { context.getSystemService(Context.USAGE_STATS_SERVICE) } returns usageStatsManager
        every { workManager.getWorkInfosForUniqueWork(config.id) } returns futureWorkInfoList
        every { workManager.enqueueUniquePeriodicWork(config.id, any(), any()) } returns operation
        every { workManager.cancelAllWorkByTag(any()) } returns operation
        every { workManager.cancelUniqueWork(any()) } returns operation
        every { appLifecycleProvider.state } returns MutableStateFlow(AppLifecycleProvider.State.Background)

        futureOperation.set(Operation.SUCCESS)
    }

    @Test
    fun `given empty work infos when isRunning then returns false`() = runTest {
        // GIVEN
        futureWorkInfoList.set(mutableListOf())
        // THEN
        assertFalse(manager.isRunning(config))
    }

    @Test
    fun `given empty work infos when isEnqueued then returns false`() = runTest {
        // GIVEN
        futureWorkInfoList.set(mutableListOf())
        // THEN
        assertFalse(manager.isEnqueued(config))
    }

    @Test
    fun `given work infos with first running when isRunning then returns true`() = runTest {
        // GIVEN
        futureWorkInfoList.set(
            mutableListOf(
                createWorkInfo(WorkInfo.State.RUNNING),
                createWorkInfo(WorkInfo.State.CANCELLED),
            )
        )
        // THEN
        assertTrue(manager.isRunning(config))
    }

    @Test
    fun `given work infos with first not running when isRunning then returns false`() = runTest {
        // GIVEN
        futureWorkInfoList.set(
            mutableListOf(
                createWorkInfo(WorkInfo.State.CANCELLED),
                createWorkInfo(WorkInfo.State.RUNNING),
            )
        )
        // THEN
        assertFalse(manager.isRunning(config))
    }

    @Test
    fun returnWhenAlreadyEnqueuedAndNotImmediately() = runTest {
        // GIVEN
        futureWorkInfoList.set(mutableListOf(createWorkInfo(WorkInfo.State.ENQUEUED)))
        // WHEN
        manager.enqueue(config, immediately = false)
        // THEN
        verify(exactly = 0) {
            workManager.cancelAllWorkByTag(any())
            workManager.enqueueUniquePeriodicWork(any(), any(), any())
        }
    }

    @Test
    fun cancelBeforeEnqueuing() = runTest {
        // GIVEN
        futureWorkInfoList.set(mutableListOf())
        // WHEN
        manager.enqueue(config, immediately = false)
        // THEN
        verify(ordering = Ordering.ORDERED) {
            workManager.cancelAllWorkByTag(any())
            workManager.enqueueUniquePeriodicWork(any(), any(), any())
        }
    }

    @Test
    fun enqueueCorrectBackgroundDurations() = runTest {
        // GIVEN
        futureWorkInfoList.set(mutableListOf())
        every { appLifecycleProvider.state } returns MutableStateFlow(AppLifecycleProvider.State.Background)
        val expectedIntervalDuration = 1800.seconds.inWholeMilliseconds
        val expectedInitialDelay = 1800.seconds.inWholeMilliseconds
        // WHEN
        manager.enqueue(config, immediately = false)
        // THEN
        verify {
            workManager.enqueueUniquePeriodicWork(config.id, ExistingPeriodicWorkPolicy.REPLACE, match { actual ->
                actual.workSpec.intervalDuration == expectedIntervalDuration &&
                    actual.workSpec.initialDelay == expectedInitialDelay
            })
        }
    }

    @Test
    fun enqueueCorrectForegroundDurations() = runTest {
        // GIVEN
        futureWorkInfoList.set(mutableListOf())
        every { appLifecycleProvider.state } returns MutableStateFlow(AppLifecycleProvider.State.Foreground)
        val expectedIntervalDuration = 1800.seconds.inWholeMilliseconds
        val expectedInitialDelay = 30.seconds.inWholeMilliseconds
        // WHEN
        manager.enqueue(config, immediately = false)
        // THEN
        verify {
            workManager.enqueueUniquePeriodicWork(config.id, ExistingPeriodicWorkPolicy.REPLACE, match { actual ->
                actual.workSpec.intervalDuration == expectedIntervalDuration &&
                    actual.workSpec.initialDelay == expectedInitialDelay
            })
        }
    }

    @Test
    fun enqueueCorrectForegroundDurationsWhenImmediately() = runTest {
        // GIVEN
        futureWorkInfoList.set(mutableListOf())
        every { appLifecycleProvider.state } returns MutableStateFlow(AppLifecycleProvider.State.Foreground)
        val expectedIntervalDuration = 1800.seconds.inWholeMilliseconds
        val expectedInitialDelay = 0.seconds.inWholeMilliseconds
        // WHEN
        manager.enqueue(config, immediately = true)
        // THEN
        verify {
            workManager.enqueueUniquePeriodicWork(config.id, ExistingPeriodicWorkPolicy.REPLACE, match { actual ->
                actual.workSpec.intervalDuration == expectedIntervalDuration &&
                    actual.workSpec.initialDelay == expectedInitialDelay
            })
        }
    }

    @Test
    fun enqueueCorrectDurationsWhenImmediateMinimumInitialDelayIsBiggerThenOneMinute() = runTest {
        // GIVEN
        futureWorkInfoList.set(mutableListOf())
        every { context.resources } returns mockk {
            every { getInteger(R.integer.core_feature_event_manager_worker_immediate_minimum_initial_delay_seconds) } returns 61
            every { getBoolean(R.bool.core_feature_event_manager_worker_repeat_internal_background_by_bucket) } returns false
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_background_seconds) } returns 1800
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_foreground_seconds) } returns 30
            every { getInteger(R.integer.core_feature_event_manager_worker_backoff_delay_seconds) } returns 30
            every { getBoolean(R.bool.core_feature_event_manager_worker_requires_storage_not_low) } returns false
            every { getBoolean(R.bool.core_feature_event_manager_worker_requires_battery_not_low) } returns false
        }
        val expectedIntervalDuration = 1800.seconds.inWholeMilliseconds
        val expectedInitialDelay = 61.seconds.inWholeMilliseconds
        // WHEN
        manager.enqueue(config, immediately = true)
        // THEN
        verify {
            workManager.enqueueUniquePeriodicWork(config.id, ExistingPeriodicWorkPolicy.REPLACE, match { actual ->
                actual.workSpec.intervalDuration == expectedIntervalDuration &&
                    actual.workSpec.initialDelay == expectedInitialDelay
            })
        }
    }

    @Test
    fun cancelCallCancelAllWorkByTagAndCancelUniqueWork() = runTest {
        // GIVEN
        val requestTag = EventWorker.getRequestTagFor(config)
        val uniqueWorkName = config.id
        // WHEN
        manager.cancel(config)
        // THEN
        verify(ordering = Ordering.ORDERED) {
            workManager.cancelAllWorkByTag(requestTag)
            workManager.cancelUniqueWork(uniqueWorkName)
        }
    }
}

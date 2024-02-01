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

package me.proton.core.eventmanager.data.work

import android.app.usage.UsageStatsManager
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_ACTIVE
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_FREQUENT
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_RARE
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_RESTRICTED
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_WORKING_SET
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import me.proton.core.eventmanager.data.R
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.LogTag
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.presentation.app.AppLifecycleProvider.State.Background
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Singleton
class EventWorkerManagerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val workManager: WorkManager,
    private val appLifecycleProvider: AppLifecycleProvider
) : EventWorkerManager {

    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    private fun getUniqueWorkName(config: EventManagerConfig) = config.id

    override suspend fun enqueue(
        config: EventManagerConfig,
        immediately: Boolean
    ) {
        if (isEnqueued(config) && !immediately) {
            CoreLogger.i(LogTag.DEFAULT, "EventWorkerManager already enqueued: $config")
            return
        }
        val isBackground = appLifecycleProvider.state.value == Background
        val uniqueWorkName = getUniqueWorkName(config)
        val requiresBatteryNotLow = requiresBatteryNotLow() && isBackground
        val requiresStorageNotLow = requiresStorageNotLow() && isBackground
        val immediateMinimumInitialDelay = getImmediateMinimumInitialDelay()
        val request = EventWorker.getRequestFor(
            config = config,
            backoffDelay = getBackoffDelay(),
            repeatInterval = getRepeatIntervalBackground(),
            initialDelay = when (immediately) {
                true -> when {
                    // WorkManager do not respect setInitialDelay < 1 minute.
                    immediateMinimumInitialDelay < 1.minutes -> {
                        delay(immediateMinimumInitialDelay)
                        Duration.ZERO
                    }
                    else -> immediateMinimumInitialDelay
                }
                else -> when (isBackground) {
                    true -> getRepeatIntervalBackground()
                    false -> getRepeatIntervalForeground()
                }
            },
            requiresBatteryNotLow = requiresBatteryNotLow,
            requiresStorageNotLow = requiresStorageNotLow
        )
        val requestTag = EventWorker.getRequestTagFor(config)
        try {
            // Cancel any previous corresponding config EventWorker.
            workManager.cancelAllWorkByTag(requestTag).await()
        } finally {
            CoreLogger.i(LogTag.DEFAULT, "EventWorkerManager enqueue: $config")
            workManager.enqueueUniquePeriodicWork(uniqueWorkName, REPLACE, request).await()
        }
    }

    override suspend fun cancel(
        config: EventManagerConfig
    ) {
        val uniqueWorkName = getUniqueWorkName(config)
        val requestTag = EventWorker.getRequestTagFor(config)
        try {
            // Cancel any previous corresponding config EventWorker.
            workManager.cancelAllWorkByTag(requestTag).await()
        } finally {
            CoreLogger.i(LogTag.DEFAULT, "EventWorkerManager cancel: $config")
            workManager.cancelUniqueWork(uniqueWorkName).await()
        }
    }

    override suspend fun isRunning(config: EventManagerConfig): Boolean {
        val uniqueWorkName = getUniqueWorkName(config)
        val info = workManager.getWorkInfosForUniqueWork(uniqueWorkName).await().firstOrNull()
        return info?.state == WorkInfo.State.RUNNING
    }

    override suspend fun isEnqueued(config: EventManagerConfig): Boolean {
        val uniqueWorkName = getUniqueWorkName(config)
        val info = workManager.getWorkInfosForUniqueWork(uniqueWorkName).await().firstOrNull()
        return info?.state == WorkInfo.State.ENQUEUED
    }

    override fun getImmediateMinimumInitialDelay(): Duration = context.resources.getInteger(
        R.integer.core_feature_event_manager_worker_immediate_minimum_initial_delay_seconds
    ).toDuration(DurationUnit.SECONDS)

    override fun getRepeatIntervalForeground(): Duration = context.resources.getInteger(
        R.integer.core_feature_event_manager_worker_repeat_internal_foreground_seconds
    ).toDuration(DurationUnit.SECONDS)

    override fun repeatIntervalBackgroundByAppStandbyBucket(): Boolean = context.resources.getBoolean(
        R.bool.core_feature_event_manager_worker_repeat_internal_background_by_bucket
    )

    override fun getRepeatIntervalBackground(): Duration = with(context.resources) {
        if (repeatIntervalBackgroundByAppStandbyBucket() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val bucket = usageStatsManager.appStandbyBucket
            when {
                bucket < STANDBY_BUCKET_ACTIVE -> exemptedSeconds
                bucket > STANDBY_BUCKET_RESTRICTED -> restrictedSeconds
                else -> when (bucket) {
                    STANDBY_BUCKET_ACTIVE -> activeSeconds
                    STANDBY_BUCKET_WORKING_SET -> workingSetSeconds
                    STANDBY_BUCKET_FREQUENT -> frequentSeconds
                    STANDBY_BUCKET_RARE -> rareSeconds
                    STANDBY_BUCKET_RESTRICTED -> restrictedSeconds
                    else -> restrictedSeconds
                }
            }
        } else {
            backgroundSeconds
        }
    }.toDuration(DurationUnit.SECONDS)

    override fun getBackoffDelay(): Duration = context.resources.getInteger(
        R.integer.core_feature_event_manager_worker_backoff_delay_seconds
    ).toDuration(DurationUnit.SECONDS)

    override fun requiresBatteryNotLow(): Boolean = context.resources.getBoolean(
        R.bool.core_feature_event_manager_worker_requires_battery_not_low
    )

    override fun requiresStorageNotLow(): Boolean = context.resources.getBoolean(
        R.bool.core_feature_event_manager_worker_requires_storage_not_low
    )

    private val Resources.backgroundSeconds get() = getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_background_seconds)
    private val Resources.exemptedSeconds get() = getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_exempted_seconds)
    private val Resources.activeSeconds get() = getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_active_seconds)
    private val Resources.workingSetSeconds get() = getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_working_set_seconds)
    private val Resources.frequentSeconds get() = getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_frequent_seconds)
    private val Resources.rareSeconds get() = getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_rare_seconds)
    private val Resources.restrictedSeconds get() = getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_restricted_seconds)
}

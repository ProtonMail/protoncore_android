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

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.eventmanager.data.R
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.presentation.app.AppLifecycleProvider
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class EventWorkerManagerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val workManager: WorkManager,
    private val appLifecycleProvider: AppLifecycleProvider
) : EventWorkerManager {

    private fun getUniqueWorkName(config: EventManagerConfig) = config.id

    override fun enqueue(config: EventManagerConfig, immediately: Boolean) {
        val uniqueWorkName = getUniqueWorkName(config)
        val initialDelay =
            if (immediately) Duration.ZERO else when (appLifecycleProvider.state.value) {
                AppLifecycleProvider.State.Background -> getRepeatIntervalBackground()
                AppLifecycleProvider.State.Foreground -> getRepeatIntervalForeground()
            }
        val request = EventWorker.getRequestFor(this, config, initialDelay)
        workManager.enqueueUniquePeriodicWork(uniqueWorkName, REPLACE, request)
    }

    override fun cancel(config: EventManagerConfig) {
        val uniqueWorkName = getUniqueWorkName(config)
        workManager.cancelUniqueWork(uniqueWorkName)
    }

    override suspend fun isRunning(config: EventManagerConfig): Boolean {
        val uniqueWorkName = getUniqueWorkName(config)
        val info = workManager.getWorkInfosForUniqueWork(uniqueWorkName).await().firstOrNull()
        return info?.state == WorkInfo.State.RUNNING
    }

    override fun getRepeatIntervalForeground(): Duration = context.resources.getInteger(
        R.integer.core_feature_event_manager_worker_repeat_internal_foreground_seconds
    ).toDuration(DurationUnit.SECONDS)

    override fun getRepeatIntervalBackground(): Duration = context.resources.getInteger(
        R.integer.core_feature_event_manager_worker_repeat_internal_background_seconds
    ).toDuration(DurationUnit.SECONDS)

    override fun getBackoffDelay(): Duration = context.resources.getInteger(
        R.integer.core_feature_event_manager_worker_backoff_delay_seconds
    ).toDuration(DurationUnit.SECONDS)
}

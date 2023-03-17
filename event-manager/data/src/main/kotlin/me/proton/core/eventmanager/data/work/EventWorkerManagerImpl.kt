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

package me.proton.core.eventmanager.data.work

import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import kotlinx.coroutines.delay
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.presentation.app.AppLifecycleProvider
import javax.inject.Inject
import kotlin.time.Duration

class EventWorkerManagerImpl @Inject constructor(
    private val workManager: WorkManager,
    private val appLifecycleProvider: AppLifecycleProvider
) : EventWorkerManager {

    private fun getUniqueWorkName(config: EventManagerConfig) = "$config"

    override fun enqueue(config: EventManagerConfig, immediately: Boolean) {
        val uniqueWorkName = getUniqueWorkName(config)
        val initialDelay = if (immediately) Duration.ZERO else when (appLifecycleProvider.state.value) {
            AppLifecycleProvider.State.Background -> EventWorkerManager.REPEAT_INTERVAL_BACKGROUND
            AppLifecycleProvider.State.Foreground -> EventWorkerManager.REPEAT_INTERVAL_FOREGROUND
        }
        val request = EventWorker.getRequestFor(config, initialDelay)
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
}

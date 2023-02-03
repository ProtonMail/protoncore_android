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

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.observability.domain.ObservabilityWorkerManager
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public class ObservabilityWorkerManagerImpl constructor(
    private val clockMillis: () -> Long,
    private val workManager: WorkManager
) : ObservabilityWorkerManager {
    private val lastSentAtMs = MutexValue<Long?>(null)

    override fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    override suspend fun getDurationSinceLastShipment(): Duration? =
        lastSentAtMs.getValue()?.let { clockMillis() - it }?.milliseconds

    override suspend fun setLastSentNow() {
        lastSentAtMs.setValue(clockMillis())
    }

    override fun schedule(delay: Duration) {
        val request = OneTimeWorkRequestBuilder<ObservabilityWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(delay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .build()
        val policy = when (delay) {
            // If there is no delay, replace currently scheduled worker, so we can run immediately.
            Duration.ZERO -> ExistingWorkPolicy.REPLACE

            // If there is a delay, keep the currently scheduled worker, since it'll likely run earlier.
            else -> ExistingWorkPolicy.KEEP
        }
        workManager.beginUniqueWork(WORK_NAME, policy, request).enqueue()
    }

    private class MutexValue<T>(initialValue: T) {
        private val mutex = Mutex()
        private var value: T = initialValue

        suspend fun getValue(): T = mutex.withLock { value }
        suspend fun setValue(newValue: T) = mutex.withLock { value = newValue }
    }

    private companion object {
        private const val WORK_NAME = "me.proton.core.observability.data.worker"
    }
}

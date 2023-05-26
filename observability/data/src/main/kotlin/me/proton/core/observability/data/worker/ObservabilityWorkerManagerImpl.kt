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
import me.proton.core.observability.domain.ObservabilityWorkerManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration

public class ObservabilityWorkerManagerImpl @Inject constructor(
    private val workManager: WorkManager
) : ObservabilityWorkerManager {

    override fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
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
        workManager.enqueueUniqueWork(WORK_NAME, policy, request)
    }

    private companion object {
        private const val WORK_NAME = "me.proton.core.observability.data.worker"
    }
}

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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.eventmanager.data.LogTag
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.serialize
import java.util.concurrent.TimeUnit
import me.proton.core.network.domain.ApiException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

@HiltWorker
open class EventWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val eventManagerProvider: EventManagerProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val config = requireNotNull(inputData.getString(KEY_INPUT_CONFIG)?.deserialize<EventManagerConfig>())
        val manager = eventManagerProvider.get(config)
        return runCatching { manager.process() }.fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                when (it) {
                    is CancellationException -> {
                        Result.failure()
                    }
                    is ApiException -> {
                        CoreLogger.d(LogTag.WORKER_ERROR, it, "EventManager non-critical error")
                        Result.retry()
                    }
                    else -> {
                        CoreLogger.e(LogTag.WORKER_ERROR, it)
                        Result.retry()
                    }
                }
            }
        )
    }

    companion object {
        private const val KEY_INPUT_CONFIG = "config"

        fun getRequestFor(config: EventManagerConfig, initialDelay: Duration): PeriodicWorkRequest {
            val initialDelaySeconds = initialDelay.inWholeSeconds
            val backoffDelaySeconds = EventWorkerManager.BACKOFF_DELAY.inWholeSeconds
            val repeatIntervalSeconds = EventWorkerManager.REPEAT_INTERVAL_BACKGROUND.inWholeSeconds
            val serializedConfig = config.serialize()
            return PeriodicWorkRequestBuilder<EventWorker>(repeatIntervalSeconds, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, backoffDelaySeconds, TimeUnit.SECONDS)
                .setInputData(workDataOf(KEY_INPUT_CONFIG to serializedConfig))
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                .addTag(serializedConfig)
                .addTag(config.listenerType.name)
                .addTag(config.userId.id)
                .build()
        }
    }
}

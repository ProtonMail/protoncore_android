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
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import me.proton.core.telemetry.domain.LogTag.DEFAULT
import me.proton.core.telemetry.domain.usecase.ProcessTelemetryEvents
import me.proton.core.util.kotlin.CoreLogger
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

@HiltWorker
internal class TelemetryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val processTelemetryEvents: ProcessTelemetryEvents
) : CoroutineWorker(appContext, params) {

    val userId = inputData.getString(INPUT_USER_ID)?.let { UserId(it) }

    override suspend fun doWork(): Result {
        return processTelemetryEvents.runCatching {
            invoke(userId)
            Result.success()
        }.recover {
            if (it is ApiException && it.isRetryable()) {
                Result.retry()
            } else {
                if (it !is ApiException && it !is CancellationException) {
                    CoreLogger.e(DEFAULT, it, "Could not send telemetry events.")
                } else {
                    CoreLogger.i(DEFAULT, it, "Could not send telemetry events.")
                }
                Result.failure(workDataOf("errorMessage" to it.message))
            }
        }.getOrThrow()
    }

    companion object {
        private const val INPUT_USER_ID = "arg.userId"

        fun makeInputData(userId: UserId?): Data {
            return workDataOf(
                INPUT_USER_ID to userId?.id
            )
        }

        fun getRequest(userId: UserId?, delay: Duration): OneTimeWorkRequest {
            val inputData = makeInputData(userId)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<TelemetryWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .setInitialDelay(delay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                .apply { userId?.let { addTag(it.id) } }
                .build()
        }

        fun getUniqueWorkName(userId: UserId?) = "${TelemetryWorker::class.simpleName}-process-$userId"
    }
}

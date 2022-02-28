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

package me.proton.core.metrics.data.remote.worker

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
import me.proton.core.metrics.domain.entity.Metrics
import me.proton.core.metrics.domain.repository.MetricsRepository
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.serialize

@HiltWorker
internal class PostMetricsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val metricsRepository: MetricsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val metricsJSON = inputData.getString(INPUT_METRICS) ?: return Result.failure()
        val metrics = metricsJSON.deserialize<Metrics>()
        val userId: UserId? = inputData.getString(INPUT_USER_ID)?.let { UserId(it) }
        return runCatching {
            metricsRepository.post(userId, metrics)
            Result.success()
        }.getOrElse {
            if (it is ApiException && it.isRetryable()) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val INPUT_USER_ID = "arg.userId"
        const val INPUT_METRICS = "arg.metrics"

        private fun makeInputData(userId: UserId?, metrics: Metrics): Data {
            return workDataOf(
                INPUT_METRICS to metrics.serialize(),
                INPUT_USER_ID to userId?.id
            )
        }

        fun getRequest(userId: UserId?, metrics: Metrics): OneTimeWorkRequest {
            val inputData = makeInputData(userId, metrics)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val builder = OneTimeWorkRequestBuilder<PostMetricsWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
            if (userId != null) {
                builder.addTag(userId.id)
            }
            return builder.build()
        }
    }
}

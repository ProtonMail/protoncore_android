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

package me.proton.core.featureflag.data.remote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints.Builder
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.usecase.FetchUnleashTogglesRemote
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.time.Duration

@HiltWorker
internal class FetchUnleashTogglesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val fetchUnleashTogglesRemote: FetchUnleashTogglesRemote,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(INPUT_USER_ID)?.let { UserId(it) }
        return runCatching {
            fetchUnleashTogglesRemote(userId)
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
        private const val INPUT_USER_ID = "arg.userId"

        private fun makeInputData(userId: UserId?): Data {
            return workDataOf(
                INPUT_USER_ID to userId?.id,
            )
        }

        fun getOneTimeWorkRequest(
            userId: UserId?,
        ) = OneTimeWorkRequestBuilder<FetchUnleashTogglesWorker>()
            .setInputData(makeInputData(userId))
            .setConstraints(Builder().setRequiredNetworkType(CONNECTED).build())
            .build()

        fun getPeriodicWorkRequest(
            userId: UserId?,
            repeatInterval: Duration
        ) = PeriodicWorkRequestBuilder<FetchUnleashTogglesWorker>(
            repeatInterval = repeatInterval.inWholeSeconds,
            repeatIntervalTimeUnit = SECONDS
        )
            .setInputData(makeInputData(userId))
            .setConstraints(Builder().setRequiredNetworkType(CONNECTED).build())
            .build()

        fun getOneTimeUniqueWorkName(userId: UserId?) =
            "${FetchUnleashTogglesWorker::class.simpleName}-one-time-$userId"

        fun getPeriodicUniqueWorkName(userId: UserId?) =
            "${FetchUnleashTogglesWorker::class.simpleName}-periodic-$userId"
    }
}

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

package me.proton.core.keytransparency.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.Constants.KT_SELF_AUDIT_INTERVAL_HOURS
import me.proton.core.keytransparency.domain.RunSelfAudit
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltWorker
public class SelfAuditWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val runSelfAudit: RunSelfAudit
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = runCatching {
        val userId: UserId? = inputData.getString(INPUT_USER_ID)?.let { UserId(it) }
        checkNotNull(userId) { "Self audit worker needs a user ID" }
        runSelfAudit(userId, forceRefresh = true)
        Result.success()
    }.getOrElse {
        if (it is ApiException && it.isRetryable()) {
            return Result.retry()
        }
        return Result.failure()
    }

    public class Scheduler @Inject constructor(
        private val workManager: WorkManager
    ) {

        private fun getWorkTag(userId: UserId) = "kt-self-audit-${userId.id}"

        internal fun scheduleSelfAudit(userId: UserId, delay: Long) {
            val workRequest = getPeriodicRequest(userId, delay)
            workManager.enqueueUniquePeriodicWork(
                getWorkTag(userId),
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }

        internal fun cancelSelfAudit(userId: UserId) {
            workManager.cancelAllWorkByTag(
                getWorkTag(userId)
            )
        }

        private fun getPeriodicRequest(userId: UserId, delay: Long): PeriodicWorkRequest {
            val inputData = makeInputData(userId)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val builder = PeriodicWorkRequestBuilder<SelfAuditWorker>(
                KT_SELF_AUDIT_INTERVAL_HOURS.toLong(),
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.SECONDS)
            builder.addTag(userId.id)
            builder.addTag(getWorkTag(userId))
            return builder.build()
        }

        private fun makeInputData(userId: UserId): Data {
            return workDataOf(
                INPUT_USER_ID to userId.id
            )
        }
    }

    internal companion object {
        const val INPUT_USER_ID = "arg.userId"
    }
}

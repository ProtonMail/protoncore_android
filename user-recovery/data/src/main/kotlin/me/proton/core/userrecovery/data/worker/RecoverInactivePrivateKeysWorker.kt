/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.userrecovery.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import me.proton.core.userrecovery.data.usecase.RecoverInactivePrivateKeys
import me.proton.core.userrecovery.domain.LogTag
import me.proton.core.util.kotlin.CoreLogger

@HiltWorker
class RecoverInactivePrivateKeysWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val recoverInactivePrivateKeys: RecoverInactivePrivateKeys
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = coroutineScope {
        val rawUserId = requireNotNull(inputData.getString(KEY_USER_ID))
        val userId = UserId(rawUserId)
        runCatching {
            recoverInactivePrivateKeys(userId)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                ensureActive() // rethrows CancellationException

                if (error is ApiException && error.isRetryable()) {
                    Result.retry()
                } else {
                    CoreLogger.e(LogTag.KEY_RECOVERY, error)
                    Result.failure()
                }
            }
        )
    }

    companion object {
        private const val KEY_USER_ID = "userId"

        internal fun getWorkData(userId: UserId) = workDataOf(KEY_USER_ID to userId.id)

        fun getRequest(userId: UserId): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<RecoverInactivePrivateKeysWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(getWorkData(userId))
                .build()
    }
}

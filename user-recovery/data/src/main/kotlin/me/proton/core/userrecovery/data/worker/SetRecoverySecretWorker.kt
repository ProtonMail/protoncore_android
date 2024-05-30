/*
 * Copyright (c) 2024 Proton AG
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
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.network.domain.ResponseCodes.NOT_NULL
import me.proton.core.network.domain.hasProtonErrorCode
import me.proton.core.network.domain.isHttpError
import me.proton.core.network.domain.isRetryable
import me.proton.core.user.domain.usecase.GetUser
import me.proton.core.userrecovery.domain.usecase.SetRecoverySecretRemote
import me.proton.core.userrecovery.domain.LogTag
import me.proton.core.util.kotlin.CoreLogger

@HiltWorker
class SetRecoverySecretWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val setRecoverySecretRemote: SetRecoverySecretRemote,
    private val getUser: GetUser
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val rawUserId = requireNotNull(inputData.getString(KEY_USER_ID))
        val userId = UserId(rawUserId)
        return runCatching {
            setRecoverySecretRemote(userId)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                when {
                    error is ApiException && error.isRecoverySecretSetError() -> {
                        getUser(userId,  refresh =true)
                        Result.success()
                    }
                    error is ApiException && error.isRetryable() -> Result.retry()
                    else -> {
                        CoreLogger.e(LogTag.DEFAULT, error)
                        Result.failure()
                    }
                }
            }
        )
    }

    companion object {
        private const val KEY_USER_ID = "userId"
        internal fun getWorkData(userId: UserId) = workDataOf(KEY_USER_ID to userId.id)
        fun getRequest(userId: UserId): OneTimeWorkRequest = OneTimeWorkRequestBuilder<SetRecoverySecretWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(getWorkData(userId))
            .build()
    }
}

private fun ApiException.isRecoverySecretSetError() =
    hasProtonErrorCode(NOT_NULL) && error.isHttpError(HTTP_UNPROCESSABLE)

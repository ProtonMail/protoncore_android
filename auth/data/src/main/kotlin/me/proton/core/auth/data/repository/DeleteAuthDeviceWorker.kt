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

package me.proton.core.auth.data.repository

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.usecase.DeleteAuthDeviceRemote
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable

@HiltWorker
class DeleteAuthDeviceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val deleteAuthDeviceRemote: DeleteAuthDeviceRemote
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val userId = UserId(requireNotNull(inputData.getString(INPUT_USER_ID)))
        val deviceId = AuthDeviceId(requireNotNull(inputData.getString(INPUT_DEVICE_ID)))
        return runCatching {
            deleteAuthDeviceRemote(deviceId, userId)
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
        private const val INPUT_DEVICE_ID = "arg.deviceId"

        internal fun makeInputData(deviceId: AuthDeviceId, userId: UserId): Data {
            return workDataOf(
                INPUT_DEVICE_ID to deviceId.id,
                INPUT_USER_ID to userId.id,
            )
        }

        fun makeWorkerRequest(deviceId: AuthDeviceId, userId: UserId): WorkRequest {
            val inputData = makeInputData(deviceId, userId)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<DeleteAuthDeviceWorker>()
                .setConstraints(constraints)
                .addTag(userId.id)
                .setInputData(inputData)
                .build()
        }
    }
}

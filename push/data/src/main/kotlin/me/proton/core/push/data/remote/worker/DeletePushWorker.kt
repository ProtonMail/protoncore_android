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

package me.proton.core.push.data.remote.worker

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
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.push.domain.usecase.DeletePushRemote

@HiltWorker
internal class DeletePushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pushRepository: PushRepository,
    private val deletePushRemote: DeletePushRemote,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = UserId(requireNotNull(inputData.getString(INPUT_USER_ID)))
        val pushId = PushId(requireNotNull(inputData.getString(INPUT_PUSH_ID)))
        val type = requireNotNull(PushObjectType.map[requireNotNull(inputData.getString(INPUT_PUSH_TYPE))])
        return runCatching {
            deletePushRemote(userId, pushId)
            Result.success()
        }.getOrElse {
            if (it is ApiException && it.isRetryable()) {
                Result.retry()
            } else {
                pushRepository.markAsStale(userId, type)
                Result.failure()
            }
        }
    }

    companion object {
        private const val INPUT_USER_ID = "arg.userId"
        private const val INPUT_PUSH_ID = "arg.pushId"
        private const val INPUT_PUSH_TYPE = "arg.pushObjectType"

        internal fun makeInputData(userId: UserId, pushId: PushId, type: String): Data {
            return workDataOf(
                INPUT_USER_ID to userId.id,
                INPUT_PUSH_ID to pushId.id,
                INPUT_PUSH_TYPE to type,
            )
        }

        fun makeWorkerRequest(userId: UserId, pushId: PushId, type: String): WorkRequest {
            val inputData = makeInputData(userId, pushId, type)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<DeletePushWorker>()
                .setConstraints(constraints)
                .addTag(userId.id)
                .setInputData(inputData)
                .build()
        }
    }
}

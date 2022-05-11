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

package me.proton.core.push.data.remote.worker

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
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.usecase.FetchPushesRemote

@HiltWorker
internal class FetchPushesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val fetchPushesRemote: FetchPushesRemote,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = UserId(requireNotNull(inputData.getString(INPUT_USER_ID)))
        val type = requireNotNull(PushObjectType.map[inputData.getString(INPUT_PUSH_TYPE)])
        return runCatching {
            fetchPushesRemote(userId, type)
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
        private const val INPUT_PUSH_TYPE = "arg.pushObjectType"

        private fun makeInputData(userId: UserId, type: PushObjectType): Data {
            return workDataOf(
                INPUT_USER_ID to userId.id,
                INPUT_PUSH_TYPE to type.value,
            )
        }

        fun getRequest(userId: UserId, type: PushObjectType): OneTimeWorkRequest {
            val inputData = makeInputData(userId, type)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<FetchPushesWorker>()
                .addTag(userId.id)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        }
    }
}

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

package me.proton.core.label.data.remote.worker

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
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.repository.LabelRepository
import me.proton.core.label.domain.usecase.DeleteLabelRemote
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable

@HiltWorker
internal class DeleteLabelWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val labelRepository: LabelRepository,
    private val deleteLabelRemote: DeleteLabelRemote,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = UserId(requireNotNull(inputData.getString(INPUT_USER_ID)))
        val type = requireNotNull(LabelType.map[inputData.getInt(INPUT_LABEL_TYPE, 0)])
        val labelId = LabelId(requireNotNull(inputData.getString(INPUT_LABEL_ID)))
        return runCatching {
            deleteLabelRemote(userId, labelId)
            Result.success()
        }.getOrElse {
            if (it is ApiException && it.isRetryable()) {
                Result.retry()
            } else {
                labelRepository.markAsStale(userId, type)
                Result.failure()
            }
        }
    }

    companion object {
        private const val INPUT_USER_ID = "arg.userId"
        private const val INPUT_LABEL_TYPE = "arg.labelType"
        private const val INPUT_LABEL_ID = "arg.labelId"

        private fun makeInputData(userId: UserId, type: LabelType, labelId: LabelId): Data {
            return workDataOf(
                INPUT_USER_ID to userId.id,
                INPUT_LABEL_TYPE to type.value,
                INPUT_LABEL_ID to labelId.id
            )
        }

        fun getRequest(userId: UserId, type: LabelType, labelId: LabelId): WorkRequest {
            val inputData = makeInputData(userId, type, labelId)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<DeleteLabelWorker>()
                .addTag(userId.id)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        }
    }
}

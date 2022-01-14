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
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.entity.UpdateLabel
import me.proton.core.label.domain.repository.LabelRepository
import me.proton.core.label.domain.usecase.UpdateLabelRemote
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable

@HiltWorker
internal class UpdateLabelWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val labelRepository: LabelRepository,
    private val updateLabelRemote: UpdateLabelRemote,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = UserId(
            id = requireNotNull(inputData.getString(INPUT_USER_ID))
        )
        val type = requireNotNull(LabelType.map[inputData.getInt(INPUT_LABEL_TYPE, 0)])
        val label = UpdateLabel(
            labelId = LabelId(requireNotNull(inputData.getString(INPUT_LABEL_ID))),
            parentId = inputData.getString(INPUT_PARENT_ID)?.let { LabelId(it) },
            name = requireNotNull(inputData.getString(INPUT_NAME)),
            color = requireNotNull(inputData.getString(INPUT_COLOR)),
            isNotified = inputData.getBoolean(INPUT_IS_NOTIFIED, true),
            isExpanded = inputData.getBoolean(INPUT_IS_EXPANDED, true),
            isSticky = inputData.getBoolean(INPUT_IS_STICKY, true),
        )
        return runCatching {
            updateLabelRemote(userId, label)
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
        private const val INPUT_PARENT_ID = "arg.parentId"
        private const val INPUT_NAME = "arg.name"
        private const val INPUT_COLOR = "arg.color"
        private const val INPUT_IS_NOTIFIED = "arg.notified"
        private const val INPUT_IS_EXPANDED = "arg.expanded"
        private const val INPUT_IS_STICKY = "arg.sticky"

        private fun makeInputData(userId: UserId, type: LabelType, label: UpdateLabel): Data {
            return workDataOf(
                INPUT_USER_ID to userId.id,
                INPUT_LABEL_TYPE to type.value,
                INPUT_LABEL_ID to label.labelId.id,
                INPUT_PARENT_ID to label.parentId?.id,
                INPUT_NAME to label.name,
                INPUT_COLOR to label.color,
                INPUT_IS_NOTIFIED to label.isNotified,
                INPUT_IS_EXPANDED to label.isExpanded,
                INPUT_IS_STICKY to label.isSticky,
            )
        }

        fun getRequest(userId: UserId, type: LabelType, label: UpdateLabel): OneTimeWorkRequest {
            val inputData = makeInputData(userId, type, label)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<UpdateLabelWorker>()
                .addTag(userId.id)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        }
    }
}

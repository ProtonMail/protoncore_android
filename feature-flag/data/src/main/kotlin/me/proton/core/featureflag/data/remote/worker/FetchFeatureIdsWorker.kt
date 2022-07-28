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
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.usecase.FetchFeatureIdsRemote
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable

@HiltWorker
internal class FetchFeatureIdsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val fetchFeatureIdsRemote: FetchFeatureIdsRemote,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(INPUT_USER_ID)?.let { UserId(it) }
        val featureIds = inputData.getStringArray(INPUT_FEATURE_IDS)?.map { FeatureId(it) }
        return runCatching {
            fetchFeatureIdsRemote(userId, featureIds.orEmpty().toSet())
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
        private const val INPUT_FEATURE_IDS = "arg.featureIds"

        private fun makeInputData(userId: UserId?, featureIds: Set<FeatureId>): Data {
            return workDataOf(
                INPUT_USER_ID to userId?.id,
                INPUT_FEATURE_IDS to featureIds.map { it.id }.toTypedArray(),
            )
        }

        fun getRequest(userId: UserId?, featureIds: Set<FeatureId>): OneTimeWorkRequest {
            val inputData = makeInputData(userId, featureIds)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<FetchFeatureIdsWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        }
    }
}

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
import androidx.annotation.VisibleForTesting
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
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRemoteDataSource
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable

@HiltWorker
internal class UpdateFeatureFlagWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val remoteDataSource: FeatureFlagRemoteDataSource,
    private val localDataSource: FeatureFlagLocalDataSource
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(INPUT_USER_ID)?.let { UserId(it) }
        val featureId = inputData.getString(INPUT_FEATURE_ID)?.let(::FeatureId) ?: return Result.failure()
        val isEnabled = inputData.getBoolean(INPUT_FEATURE_VALUE, false)

        return runCatching {
            remoteDataSource.update(userId, featureId, isEnabled)
            Result.success()
        }.getOrElse { error ->
            if (error is ApiException && error.isRetryable()) {
                Result.retry()
            } else {
                rollbackLocalFeatureFlag(userId, featureId, isEnabled)
                Result.failure()
            }
        }
    }

    private suspend fun rollbackLocalFeatureFlag(
        userId: UserId?,
        featureId: FeatureId,
        value: Boolean
    ) = localDataSource.updateValue(userId, featureId, value.not())

    companion object {

        @VisibleForTesting
        const val INPUT_USER_ID = "arg.userId"

        @VisibleForTesting
        const val INPUT_FEATURE_ID = "arg.featureId"

        @VisibleForTesting
        const val INPUT_FEATURE_VALUE = "arg.featureValue"

        private fun makeInputData(
            userId: UserId?,
            featureId: FeatureId,
            value: Boolean
        ): Data {
            return workDataOf(
                INPUT_USER_ID to userId?.id,
                INPUT_FEATURE_ID to featureId.id,
                INPUT_FEATURE_VALUE to value
            )
        }

        fun getRequest(
            userId: UserId?,
            featureId: FeatureId,
            value: Boolean
        ): OneTimeWorkRequest {
            val inputData = makeInputData(userId, featureId, value)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<UpdateFeatureFlagWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        }
    }
}

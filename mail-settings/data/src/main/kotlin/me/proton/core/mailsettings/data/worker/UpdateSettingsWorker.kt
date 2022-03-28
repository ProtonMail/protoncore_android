/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.mailsettings.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.serialize
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val KEY_INPUT_RAW_USER_ID = "keyUserId"
private const val KEY_INPUT_SETTINGS_PROPERTY_SERIALIZED = "keySettingsPropertySerialized"

class UpdateSettingsWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }

    class Enqueuer @Inject constructor(
        private val workManager: WorkManager
    ) {

        fun enqueue(
            userId: UserId,
            settingsProperty: SettingsProperty
        ): ListenableFuture<WorkInfo> {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val updateSettingsRequest = OneTimeWorkRequestBuilder<UpdateSettingsWorker>()
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        KEY_INPUT_RAW_USER_ID to userId.id,
                        KEY_INPUT_SETTINGS_PROPERTY_SERIALIZED to settingsProperty.serialize(),
                    )
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

            workManager.enqueueUniqueWork(
                "updateSettingsWork-${userId.id}-${settingsProperty.javaClass.simpleName}",
                ExistingWorkPolicy.REPLACE,
                updateSettingsRequest
            )
            return workManager.getWorkInfoById(updateSettingsRequest.id)
        }
    }

}

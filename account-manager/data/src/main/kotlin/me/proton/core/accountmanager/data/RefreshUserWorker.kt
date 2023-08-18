/*
 * Copyright (c) 2023. Proton AG
 *
 * This file is part of ProtonVPN.
 *
 * ProtonVPN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonVPN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonVPN.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.accountmanager.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.serialize
import javax.inject.Inject

private const val INPUT_USER_ID = "userId"

public class RefreshUserWorkManager @Inject constructor(
    private val workManager: WorkManager
) {
    public fun enqueue(userId: UserId) {
        val request = OneTimeWorkRequestBuilder<RefreshUserWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(workDataOf(INPUT_USER_ID to userId.serialize()))
            .build()
        workManager.enqueue(request)
    }
}

@HiltWorker
internal class RefreshUserWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = requireNotNull(inputData.getString(INPUT_USER_ID)?.deserialize<UserId>())
        return userRepository.runCatching {
            getUser(userId, refresh = true)
            Result.success()
        }.recover {
            if ((it as? ApiException)?.isRetryable() == true) {
                Result.retry()
            }  else {
                Result.failure()
            }
        }.getOrThrow()
    }
}

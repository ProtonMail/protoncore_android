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

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import me.proton.core.domain.entity.UserId
import me.proton.core.userrecovery.domain.worker.UserRecoveryWorkerManager
import javax.inject.Inject

class UserRecoveryWorkerManagerImpl @Inject constructor(
    private val workManager: WorkManager
) : UserRecoveryWorkerManager {

    override suspend fun enqueueSetRecoverySecret(userId: UserId) {
        workManager.enqueueUniqueWork(
            "setRecoverySecretWork-${userId.id}",
            ExistingWorkPolicy.KEEP,
            SetRecoverySecretWorker.getRequest(userId)
        )
    }
}

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

package me.proton.core.userrecovery.data.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.userrecovery.data.entity.recoverySecretHash
import me.proton.core.userrecovery.domain.entity.RecoveryFile
import me.proton.core.userrecovery.domain.repository.DeviceRecoveryRepository
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.UtcClock
import javax.inject.Inject

class StoreRecoveryFile @Inject constructor(
    private val deviceRecoveryRepository: DeviceRecoveryRepository,
    @UtcClock private val clock: Clock,
    private val userManager: UserManager
) {
    suspend operator fun invoke(
        encodedRecoveryFile: String,
        userId: UserId
    ) {
        val user = userManager.getUser(userId)
        val primaryKey = requireNotNull(user.keys.firstOrNull { it.privateKey.isPrimary }) {
            "Primary key is missing."
        }
        val recoverySecretHash = primaryKey.recoverySecretHash()
        val recoveryFile = RecoveryFile(
            userId = userId,
            createdAtUtcMillis = clock.currentEpochMillis(),
            recoveryFile = encodedRecoveryFile,
            recoverySecretHash = requireNotNull(recoverySecretHash) {
                "Recovery secret is missing."
            }
        )
        deviceRecoveryRepository.insertRecoveryFile(recoveryFile)
    }
}

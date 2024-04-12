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
import me.proton.core.userrecovery.domain.repository.DeviceRecoveryRepository
import me.proton.core.userrecovery.domain.usecase.GetRecoveryInactiveUserKeys
import me.proton.core.userrecovery.domain.usecase.GetRecoveryPrivateKeys
import me.proton.core.userrecovery.domain.usecase.ShowDeviceRecoveryNotification
import javax.inject.Inject

class RecoverInactivePrivateKeys @Inject constructor(
    private val getRecoveryInactiveUserKeys: GetRecoveryInactiveUserKeys,
    private val getRecoveryPrivateKeys: GetRecoveryPrivateKeys,
    private val deviceRecoveryRepository: DeviceRecoveryRepository,
    private val showDeviceRecoveryNotification: ShowDeviceRecoveryNotification,
    private val userManager: UserManager
) {
    suspend operator fun invoke(userId: UserId) {
        var someKeysRecovered = false
        try {
            deviceRecoveryRepository.getRecoveryFiles(userId).forEach { recoveryFile ->
                val privateKeys = getRecoveryPrivateKeys(userId, recoveryFile.recoveryFile)
                getRecoveryInactiveUserKeys(userId, privateKeys).forEach { userKey ->
                    userManager.reactivateKey(userKey)
                    someKeysRecovered = true
                }
            }
        } finally {
            if (someKeysRecovered) {
                showDeviceRecoveryNotification(userId)
            }
        }
    }
}

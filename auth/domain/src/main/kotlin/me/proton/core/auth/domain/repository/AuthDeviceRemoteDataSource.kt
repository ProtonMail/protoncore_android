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

package me.proton.core.auth.domain.repository

import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.CreatedDevice
import me.proton.core.auth.domain.entity.UnprivatizationInfo
import me.proton.core.auth.domain.usecase.sso.Based64EncodedAeadEncryptedSecret
import me.proton.core.domain.entity.UserId

interface AuthDeviceRemoteDataSource {

    suspend fun createDevice(
        userId: UserId,
        name: String,
        activationToken: String?
    ): CreatedDevice

    suspend fun associateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        deviceToken: String
    ): Based64EncodedAeadEncryptedSecret

    suspend fun activateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        encryptedSecret: Based64EncodedAeadEncryptedSecret
    )

    suspend fun deleteDevice(
        deviceId: AuthDeviceId,
        userId: UserId
    )

    suspend fun getAuthDevices(
        userId: UserId
    ): List<AuthDevice>

    suspend fun rejectAuthDevice(
        userId: UserId,
        deviceId: AuthDeviceId
    )

    suspend fun requestAdminHelp(
        userId: UserId,
        deviceId: AuthDeviceId
    )

    suspend fun getUnprivatizationInfo(userId: UserId): UnprivatizationInfo
}

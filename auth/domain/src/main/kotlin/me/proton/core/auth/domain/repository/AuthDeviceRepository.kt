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

import kotlinx.coroutines.flow.Flow
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.CreatedDevice
import me.proton.core.auth.domain.entity.UnprivatizationInfo
import me.proton.core.crypto.common.pgp.Based64Encoded
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId

interface AuthDeviceRepository {

    /**
     * Create a new device remotely, returning Device Token.
     */
    suspend fun createDevice(
        userId: UserId,
        deviceName: String,
        activationToken: String
    ): CreatedDevice

    /**
     * Associate [deviceId] remotely, returning Encrypted Secret.
     */
    suspend fun associateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        deviceToken: String
    ): String

    /**
     * Activate [deviceId] remotely, by uploading [encryptedSecret].
     */
    suspend fun activateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        encryptedSecret: Based64Encoded
    )

    fun observeByUserId(
        userId: UserId,
        refresh: Boolean
    ): Flow<List<AuthDevice>>

    suspend fun getByUserId(
        userId: UserId,
        refresh: Boolean = false
    ): List<AuthDevice>

    suspend fun getByAddressId(
        userId: UserId,
        addressId: AddressId,
        refresh: Boolean = false
    ): List<AuthDevice>

    suspend fun deleteByUserId(
        userId: UserId
    )

    suspend fun deleteByDeviceId(
        userId: UserId,
        deviceId: AuthDeviceId
    )

    suspend fun rejectAuthDevice(
        userId: UserId,
        deviceId: AuthDeviceId
    )

    suspend fun getUnprivatizationInfo(
        userId: UserId
    ): UnprivatizationInfo
}

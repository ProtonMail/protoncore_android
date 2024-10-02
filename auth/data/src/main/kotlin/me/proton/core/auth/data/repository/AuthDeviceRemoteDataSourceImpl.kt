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

package me.proton.core.auth.data.repository

import me.proton.core.auth.data.api.AuthDeviceApi
import me.proton.core.auth.data.api.request.ActivateDeviceRequest
import me.proton.core.auth.data.api.request.AssociateDeviceRequest
import me.proton.core.auth.data.api.request.CreateDeviceRequest
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.CreatedDevice
import me.proton.core.auth.domain.entity.UnprivatizationInfo
import me.proton.core.auth.domain.repository.AuthDeviceRemoteDataSource
import me.proton.core.crypto.common.aead.AeadEncryptedString
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import javax.inject.Inject

class AuthDeviceRemoteDataSourceImpl @Inject constructor(
    private val provider: ApiProvider,
    private val context: CryptoContext
) : AuthDeviceRemoteDataSource {

    override suspend fun createDevice(
        userId: UserId,
        name: String,
        activationToken: String?
    ): CreatedDevice = provider.get<AuthDeviceApi>(userId).invoke {
        val request = CreateDeviceRequest(name, activationToken)
        createDevice(request).toCreatedDevice(context)
    }.valueOrThrow

    override suspend fun associateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        deviceToken: String
    ): String = provider.get<AuthDeviceApi>(userId).invoke {
        associateDevice(
            deviceId = deviceId.id,
            request = AssociateDeviceRequest(deviceToken)
        ).device.encryptedSecret
    }.valueOrThrow

    override suspend fun activateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        encryptedSecret: AeadEncryptedString
    ): Unit = provider.get<AuthDeviceApi>(userId).invoke {
        activateDevice(
            deviceId = deviceId.id,
            request = ActivateDeviceRequest(encryptedSecret)
        )
        Unit
    }.valueOrThrow

    override suspend fun deleteDevice(
        deviceId: AuthDeviceId,
        userId: UserId
    ): Unit = provider.get<AuthDeviceApi>(userId).invoke {
        deleteDevice(deviceId.id)
        Unit
    }.valueOrThrow

    override suspend fun getAuthDevices(
        userId: UserId
    ): List<AuthDevice> = provider.get<AuthDeviceApi>(userId).invoke {
        getDevices().devices.map { it.toAuthDevice(userId) }
    }.valueOrThrow

    override suspend fun rejectAuthDevice(
        userId: UserId,
        deviceId: AuthDeviceId
    ): Unit = provider.get<AuthDeviceApi>(userId).invoke {
        rejectAuthDevice(deviceId.id)
        Unit
    }.valueOrThrow

    override suspend fun requestAdminHelp(
        userId: UserId,
        deviceId: AuthDeviceId
    ): Unit = provider.get<AuthDeviceApi>(userId).invoke {
        pingAdminForHelp(deviceId.id)
        Unit
    }.valueOrThrow

    override suspend fun getUnprivatizationInfo(
        userId: UserId
    ): UnprivatizationInfo = provider.get<AuthDeviceApi>(userId).invoke {
        getUnprivatizationInfo().toUnprivatizationInfo()
    }.valueOrThrow
}

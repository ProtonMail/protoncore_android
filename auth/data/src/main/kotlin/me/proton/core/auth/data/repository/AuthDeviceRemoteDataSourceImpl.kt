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

import me.proton.core.auth.data.api.AuthenticationApi
import me.proton.core.auth.data.api.request.InitDeviceRequest
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.InitDeviceStatus
import me.proton.core.auth.domain.repository.AuthDeviceRemoteDataSource
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.auth.data.api.request.AssociateDeviceRequest
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.DeviceTokenString
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

class AuthDeviceRemoteDataSourceImpl @Inject constructor(
    private val provider: ApiProvider,
) : AuthDeviceRemoteDataSource {
    override suspend fun associateDeviceWithSession(
        sessionId: SessionId,
        deviceId: AuthDeviceId,
        deviceToken: DeviceTokenString
    ): String = provider.get<AuthenticationApi>(sessionId).invoke {
        associateDevice(
            deviceId = deviceId.id,
            request = AssociateDeviceRequest(deviceToken)
        ).device.encryptedSecret
    }.valueOrThrow

    override suspend fun deleteDevice(
        deviceId: AuthDeviceId,
        userId: UserId
    ) {
        provider.get<AuthenticationApi>(userId).invoke {
            deleteDevice(deviceId.id)
        }.valueOrThrow
    }

    override suspend fun initDevice(
        sessionUserId: SessionUserId,
        name: String,
        activationToken: String
    ): InitDeviceStatus =
        provider.get<AuthenticationApi>(sessionUserId).invoke {
            val request = InitDeviceRequest(name, activationToken)
            initDevice(request).toInitDeviceStatus()
        }.valueOrThrow

    override suspend fun getAuthDevices(sessionUserId: SessionUserId): List<AuthDevice> =
        provider.get<AuthenticationApi>(sessionUserId).invoke {
            getAvailableDevices().devices.map { it.toAuthDevice(sessionUserId) }
        }.valueOrThrow
}

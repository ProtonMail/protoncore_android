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

import kotlinx.coroutines.flow.Flow
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.InitDeviceStatus
import me.proton.core.auth.domain.repository.AuthDeviceLocalDataSource
import me.proton.core.auth.domain.repository.AuthDeviceRemoteDataSource
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import javax.inject.Inject

class AuthDeviceRepositoryImpl @Inject constructor(
    private val localDataSource: AuthDeviceLocalDataSource,
    private val remoteDataSource: AuthDeviceRemoteDataSource
) : AuthDeviceRepository {

    override fun observeByUserId(userId: UserId): Flow<List<AuthDevice>> {
        return localDataSource.observeByUserId(userId)
    }

    override fun observeByAddressId(addressId: AddressId): Flow<List<AuthDevice>> {
        return localDataSource.observeByAddressId(addressId)
    }

    override suspend fun getByUserId(userId: UserId): List<AuthDevice> {
        return localDataSource.getByUserId(userId)
    }

    override suspend fun getByAddressId(addressId: AddressId): List<AuthDevice> {
        return localDataSource.getByAddressId(addressId)
    }

    override suspend fun initDevice(
        sessionUserId: SessionUserId,
        name: String,
        activationToken: String
    ): InitDeviceStatus {
        return remoteDataSource.initDevice(sessionUserId, name, activationToken)
    }
}

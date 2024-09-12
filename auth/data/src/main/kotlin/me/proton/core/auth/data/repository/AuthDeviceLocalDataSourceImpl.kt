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
import kotlinx.coroutines.flow.map
import me.proton.core.auth.data.dao.AuthDeviceDao
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.auth.data.entity.toAuthDevice
import me.proton.core.auth.data.entity.toAuthDeviceEntity
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.repository.AuthDeviceLocalDataSource
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import javax.inject.Inject

class AuthDeviceLocalDataSourceImpl @Inject constructor(
    db: AuthDatabase
) : AuthDeviceLocalDataSource {

    private val dao: AuthDeviceDao = db.authDeviceDao()

    override fun observeByUserId(userId: UserId): Flow<List<AuthDevice>> {
        return dao.observeByUserId(userId).map { list -> list.map { it.toAuthDevice() } }
    }

    override suspend fun getByUserId(userId: UserId): List<AuthDevice> {
        return dao.getByUserId(userId).map { it.toAuthDevice() }
    }

    override suspend fun getByAddressId(addressId: AddressId): List<AuthDevice> {
        return dao.getByAddressId(addressId).map { it.toAuthDevice() }
    }

    override suspend fun upsert(authDevices: List<AuthDevice>): Unit =
        dao.insertOrUpdate(*authDevices.map { it.toAuthDeviceEntity() }.toTypedArray())

    override suspend fun deleteAll(vararg userIds: UserId) {
        dao.deleteAll(*userIds)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun deleteByDeviceId(vararg deviceIds: AuthDeviceId) {
        dao.deleteByDeviceId(*deviceIds)
    }
}

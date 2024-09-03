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
import me.proton.core.auth.data.dao.DeviceSecretDao
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.auth.data.entity.toDeviceSecret
import me.proton.core.auth.data.entity.toDeviceSecretEntity
import me.proton.core.auth.domain.entity.DeviceSecret
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class DeviceSecretRepositoryImpl @Inject constructor(
    db: AuthDatabase
) : DeviceSecretRepository {

    private val dao: DeviceSecretDao = db.deviceSecretDao()

    override fun observeByUserId(userId: UserId): Flow<DeviceSecret?> {
        return dao.observeByUserId(userId).map { it?.toDeviceSecret() }
    }

    override suspend fun getByUserId(userId: UserId): DeviceSecret? {
        return dao.getByUserId(userId)?.toDeviceSecret()
    }

    override suspend fun upsert(deviceSecret: DeviceSecret) {
        dao.insertOrUpdate(deviceSecret.toDeviceSecretEntity())
    }

    override suspend fun deleteAll(userId: UserId) {
        dao.deleteAll(userId)
    }
}

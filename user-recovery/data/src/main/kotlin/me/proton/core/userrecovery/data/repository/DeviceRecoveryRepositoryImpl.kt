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

package me.proton.core.userrecovery.data.repository

import me.proton.core.domain.entity.UserId
import me.proton.core.userrecovery.data.dao.DeviceRecoveryDao
import me.proton.core.userrecovery.data.db.DeviceRecoveryDatabase
import me.proton.core.userrecovery.data.entity.toRecoveryFile
import me.proton.core.userrecovery.data.entity.toRecoveryFileEntity
import me.proton.core.userrecovery.domain.entity.RecoveryFile
import me.proton.core.userrecovery.domain.repository.DeviceRecoveryRepository
import javax.inject.Inject

class DeviceRecoveryRepositoryImpl @Inject constructor(
    database: DeviceRecoveryDatabase
) : DeviceRecoveryRepository {
    private val dao: DeviceRecoveryDao = database.deviceRecoveryDao()

    override suspend fun deleteAll(userId: UserId) {
        dao.deleteAll(userId)
    }

    override suspend fun getRecoveryFiles(userId: UserId): List<RecoveryFile> =
        dao.getRecoveryFiles(userId).map { it.toRecoveryFile() }

    override suspend fun insertRecoveryFile(recoveryFile: RecoveryFile) {
        dao.insertOrUpdate(recoveryFile.toRecoveryFileEntity())
    }
}

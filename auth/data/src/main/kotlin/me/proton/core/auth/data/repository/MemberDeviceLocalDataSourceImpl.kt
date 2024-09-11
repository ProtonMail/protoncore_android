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

package me.proton.core.auth.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.auth.data.entity.toMemberDevice
import me.proton.core.auth.data.entity.toMemberDeviceEntity
import me.proton.core.auth.domain.entity.MemberDevice
import me.proton.core.auth.domain.entity.MemberDeviceId
import me.proton.core.auth.domain.repository.MemberDeviceLocalDataSource
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class MemberDeviceLocalDataSourceImpl @Inject constructor(
    db: AuthDatabase
) : MemberDeviceLocalDataSource {
    private val dao = db.memberDeviceDao()

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun deleteAll(userId: UserId) {
        dao.deleteAll(userId)
    }

    override suspend fun deleteByMemberId(userId: UserId, memberId: UserId) {
        dao.deleteAll(userId, memberId)
    }

    override suspend fun deleteByDeviceId(userId: UserId, deviceId: MemberDeviceId) {
        dao.deleteAll(userId, deviceId)
    }

    override fun observeByUserId(userId: UserId): Flow<List<MemberDevice>> =
        dao.observeByUserId(userId).map { list -> list.map { it.toMemberDevice() } }

    override suspend fun upsert(devices: List<MemberDevice>) {
        dao.insertOrUpdate(*devices.map { it.toMemberDeviceEntity() }.toTypedArray())
    }
}

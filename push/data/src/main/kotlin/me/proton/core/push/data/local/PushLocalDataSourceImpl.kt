/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.push.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.push.data.local.db.toPush
import me.proton.core.push.data.local.db.toPushEntity
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.local.PushLocalDataSource
import javax.inject.Inject

public class PushLocalDataSourceImpl @Inject constructor(
    private val pushDatabase: PushDatabase
) : PushLocalDataSource {

    private val dao = pushDatabase.pushDao()

    override fun observeAllPushes(userId: UserId, type: PushObjectType): Flow<List<Push>> {
        return dao.observeAllPushes(userId, type.value).map { pushEntities ->
            pushEntities.map { pushEntity -> pushEntity.toPush() }
        }
    }

    override suspend fun getAllPushes(userId: UserId, type: PushObjectType): List<Push> {
        return dao.getAllPushes(userId, type.value).map { it.toPush() }
    }

    override suspend fun getPush(userId: UserId, pushId: PushId): Push? {
        return dao.getPush(userId, pushId)?.toPush()
    }

    override suspend fun deleteAllPushes() {
        dao.deleteAllPushes()
    }

    override suspend fun deletePushesByUser(vararg userIds: UserId) {
        dao.deletePushes(*userIds)
    }

    override suspend fun deletePushesById(userId: UserId, vararg pushIds: PushId) {
        dao.deletePushes(userId, *pushIds)
    }

    override suspend fun deletePushesByType(userId: UserId, type: PushObjectType) {
        dao.deletePushes(userId, type.value)
    }

    override suspend fun mergePushes(userId: UserId, vararg pushes: Push): List<Push> {
        val userPushes = pushes.filter { it.userId == userId }
        pushDatabase.inTransaction {
            deletePushesByUser(userId)
            upsertPushes(*userPushes.toTypedArray())
        }
        return userPushes
    }

    override suspend fun upsertPushes(vararg pushes: Push) {
        dao.insertOrUpdate(*pushes.map { it.toPushEntity() }.toTypedArray())
    }
}

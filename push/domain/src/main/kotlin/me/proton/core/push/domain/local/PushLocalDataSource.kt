/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.push.domain.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType

public interface PushLocalDataSource {

    public fun observeAllPushes(userId: UserId, type: PushObjectType): Flow<List<Push>>

    public suspend fun getAllPushes(userId: UserId, type: PushObjectType): List<Push>
    public suspend fun getPush(userId: UserId, pushId: PushId): Push?

    public suspend fun deleteAllPushes()
    public suspend fun deletePushesByUser(vararg userIds: UserId)
    public suspend fun deletePushesById(userId: UserId, vararg pushIds: PushId)
    public suspend fun deletePushesByType(userId: UserId, type: PushObjectType)

    /** Merges/replaces Pushes for a given [user][userId].
     * Deletes all pushes for the given [user][userId] and insert [pushes] that match the [userId].
     * [Push] objects that have different [Push.userId] will be skipped.
     * @return A list of inserted [Push] objects.
     */
    public suspend fun mergePushes(userId: UserId, vararg pushes: Push): List<Push>
    public suspend fun upsertPushes(vararg pushes: Push)
}

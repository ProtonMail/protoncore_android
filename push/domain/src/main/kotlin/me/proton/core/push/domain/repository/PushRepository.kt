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

package me.proton.core.push.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType

public interface PushRepository {
    /**
     * Delete a push by [userId] and [pushId], first locally, then remotely in the background.
     */
    public suspend fun deletePush(userId: UserId, pushId: PushId)

    /**
     * Returns all [Push] objects for a given [user][userId] and [push type][type].
     */
    public suspend fun getAllPushes(userId: UserId, type: PushObjectType, refresh: Boolean = false): List<Push>

    /**
     * Observe all pushes for a given [user][userId] and [push type][type].
     */
    public fun observeAllPushes(userId: UserId, type: PushObjectType, refresh: Boolean = false): Flow<List<Push>>

    /**
     * Mark local data as stale for [user][userId] and [push type][type].
     *
     * Note: Repository will refresh data asap.
     */
    public fun markAsStale(userId: UserId, type: PushObjectType)
}

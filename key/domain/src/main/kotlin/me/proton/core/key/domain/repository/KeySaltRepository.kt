/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.key.domain.repository

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PrivateKeySalt

interface KeySaltRepository {
    /**
     * Get [PrivateKeySalt] list, using [sessionUserId].
     *
     * @return value from cache/disk if [refresh] is false, otherwise from fetcher if [refresh] is true.
     */
    suspend fun getKeySalts(sessionUserId: SessionUserId, refresh: Boolean = true): List<PrivateKeySalt>

    /**
     * Clear all persisted [PrivateKeySalt], by [userId].
     */
    suspend fun clear(userId: UserId)

    /**
     * Clear all persisted [PrivateKeySalt].
     */
    suspend fun clearAll()
}

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

package me.proton.core.eventmanager.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.data.entity.EventMetadataEntity
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.State

@Dao
abstract class EventMetadataDao : BaseDao<EventMetadataEntity>() {

    @Query("SELECT * FROM EventMetadataEntity WHERE config = :config AND userId = :userId ORDER BY createdAt")
    abstract fun observe(userId: UserId, config: EventManagerConfig): Flow<List<EventMetadataEntity>>

    @Query("SELECT * FROM EventMetadataEntity WHERE config = :config AND userId = :userId AND eventId = :eventId")
    abstract fun observe(userId: UserId, config: EventManagerConfig, eventId: String): Flow<EventMetadataEntity?>

    @Query("SELECT * FROM EventMetadataEntity WHERE config = :config AND userId = :userId ORDER BY createdAt")
    abstract suspend fun get(userId: UserId, config: EventManagerConfig): List<EventMetadataEntity>

    @Query("SELECT * FROM EventMetadataEntity WHERE config = :config AND userId = :userId AND eventId = :eventId")
    abstract suspend fun get(userId: UserId, config: EventManagerConfig, eventId: String): EventMetadataEntity?

    @Query("UPDATE EventMetadataEntity SET state = :state, updatedAt = :updatedAt WHERE config = :config AND userId = :userId AND eventId = :eventId")
    abstract suspend fun updateState(userId: UserId, config: EventManagerConfig, eventId: String, state: State, updatedAt: Long)

    @Query("UPDATE EventMetadataEntity SET state = :state, updatedAt = :updatedAt WHERE config = :config AND userId = :userId")
    abstract suspend fun updateState(userId: UserId, config: EventManagerConfig, state: State, updatedAt: Long)

    @Query("DELETE FROM EventMetadataEntity WHERE config = :config AND userId = :userId AND eventId = :eventId")
    abstract suspend fun delete(userId: UserId, config: EventManagerConfig, eventId: String)

    @Query("DELETE FROM EventMetadataEntity WHERE config = :config AND userId = :userId")
    abstract suspend fun deleteAll(userId: UserId, config: EventManagerConfig)
}

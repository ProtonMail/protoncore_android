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

package me.proton.core.observability.data.db

import androidx.room.Dao
import androidx.room.Query
import me.proton.core.data.room.db.BaseDao
import me.proton.core.observability.data.entity.ObservabilityEventEntity

@Dao
public abstract class ObservabilityDao : BaseDao<ObservabilityEventEntity>() {

    @Query("SELECT * FROM ObservabilityEventEntity")
    internal abstract fun getAll(): List<ObservabilityEventEntity>

    @Query("SELECT * FROM ObservabilityEventEntity LIMIT :limit")
    internal abstract fun getAll(limit: Int): List<ObservabilityEventEntity>

    @Query("SELECT COUNT(*) FROM ObservabilityEventEntity")
    internal abstract fun getCount(): Long

    @Query("DELETE FROM ObservabilityEventEntity")
    internal abstract suspend fun deleteAll()

    @Query("DELETE FROM ObservabilityEventEntity WHERE id IN (:ids)")
    internal abstract suspend fun deleteAll(ids: List<Long>)

}
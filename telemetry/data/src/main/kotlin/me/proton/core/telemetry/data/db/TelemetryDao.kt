/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.telemetry.data.db

import androidx.room.Dao
import androidx.room.Query
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.data.entity.TelemetryEventEntity

@Dao
public abstract class TelemetryDao : BaseDao<TelemetryEventEntity>() {

    @Query("SELECT * FROM TelemetryEventEntity WHERE userId IS NULL ORDER BY timestamp DESC LIMIT :limit")
    internal abstract fun getAllUnAuth(limit: Int): List<TelemetryEventEntity>

    @Query("SELECT * FROM TelemetryEventEntity WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    internal abstract fun getAll(userId: UserId, limit: Int): List<TelemetryEventEntity>

    @Query("DELETE FROM TelemetryEventEntity WHERE userId IS NULL")
    internal abstract suspend fun deleteAllUnAuth()

    @Query("DELETE FROM TelemetryEventEntity WHERE userId = :userId")
    internal abstract suspend fun deleteAll(userId: UserId)

}

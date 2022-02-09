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

package me.proton.core.featureflags.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflags.data.entity.FeatureFlagEntity

@Dao
abstract class FeatureFlagDao : BaseDao<FeatureFlagEntity>() {

    @Query("SELECT * FROM FeatureFlagEntity WHERE code = :feature AND userId = :userId")
    abstract suspend fun observe(userId: UserId, feature: String): Flow<FeatureFlagEntity?>

    @Query("SELECT * FROM FeatureFlagEntity WHERE code = :feature AND userId = :userId")
    abstract suspend fun get(userId: UserId, feature: String): FeatureFlagEntity?

    @Query("DELETE FROM FeatureFlagEntity WHERE userId = :userId")
    abstract suspend fun deleteAll(userId: UserId)
}

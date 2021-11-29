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

package me.proton.core.account.data.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId

@Dao
abstract class AccountMetadataDao : BaseDao<AccountMetadataEntity>() {

    @Query("SELECT * FROM AccountMetadataEntity WHERE product = :product AND primaryAtUtc = (SELECT MAX(primaryAtUtc) FROM AccountMetadataEntity) LIMIT 1")
    abstract fun observeLatestPrimary(product: Product): Flow<AccountMetadataEntity?>

    @Query("SELECT * FROM AccountMetadataEntity WHERE product = :product ORDER BY primaryAtUtc DESC")
    abstract suspend fun getAllDescending(product: Product): List<AccountMetadataEntity>

    @Query("SELECT * FROM AccountMetadataEntity WHERE product = :product AND userId = :userId")
    abstract suspend fun getByUserId(product: Product, userId: UserId): AccountMetadataEntity?

    @Query("UPDATE AccountMetadataEntity SET migrations = :migrations WHERE product = :product AND userId = :userId")
    abstract suspend fun updateMigrations(product: Product, userId: UserId, migrations: List<String>?)

    @Query("DELETE FROM AccountMetadataEntity WHERE product = :product AND userId = :userId")
    abstract suspend fun delete(product: Product, userId: UserId)
}

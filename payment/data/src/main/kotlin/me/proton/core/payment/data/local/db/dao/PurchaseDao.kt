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

package me.proton.core.payment.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.payment.data.local.entity.PurchaseEntity
import me.proton.core.payment.domain.entity.PurchaseState

@Dao
public abstract class PurchaseDao : BaseDao<PurchaseEntity>() {
    @Query("SELECT * FROM PurchaseEntity WHERE planName = :planName")
    public abstract fun observe(planName: String): Flow<PurchaseEntity?>

    @Query("SELECT * FROM PurchaseEntity")
    public abstract fun observe(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM PurchaseEntity WHERE planName = :planName")
    public abstract suspend fun get(planName: String): PurchaseEntity?

    @Query("SELECT * FROM PurchaseEntity")
    public abstract suspend fun getAll(): List<PurchaseEntity>

    @Query("UPDATE PurchaseEntity SET purchaseState = :state WHERE planName = :planName")
    public abstract suspend fun updatePurchaseState(planName: String, state: PurchaseState)

    @Query("DELETE FROM PurchaseEntity WHERE planName = :planName")
    public abstract suspend fun delete(planName: String)
}

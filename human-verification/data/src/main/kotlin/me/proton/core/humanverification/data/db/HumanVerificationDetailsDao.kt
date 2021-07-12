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

package me.proton.core.humanverification.data.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.data.room.db.BaseDao
import me.proton.core.humanverification.data.entity.HumanVerificationEntity
import me.proton.core.network.domain.humanverification.HumanVerificationState

@Dao
abstract class HumanVerificationDetailsDao : BaseDao<HumanVerificationEntity>() {

    @Query("SELECT * FROM HumanVerificationEntity")
    abstract fun getAll(): Flow<List<HumanVerificationEntity>>

    @Query("SELECT * FROM HumanVerificationEntity WHERE clientId = :clientId")
    abstract suspend fun getByClientId(clientId: String): HumanVerificationEntity?

    @Query("DELETE FROM HumanVerificationEntity")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM HumanVerificationEntity WHERE clientId = :clientId")
    abstract suspend fun deleteByClientId(clientId: String)

    @Query("UPDATE HumanVerificationEntity SET state = :humanVerificationState, humanHeaderTokenType = :tokenType, humanHeaderTokenCode = :tokenCode WHERE clientId = :clientId")
    abstract suspend fun updateStateAndToken(
        clientId: String,
        humanVerificationState: HumanVerificationState,
        tokenType: EncryptedString?,
        tokenCode: EncryptedString?
    )
}

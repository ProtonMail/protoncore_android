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

package me.proton.core.contact.data.local.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId

@Dao
abstract class ContactDao: BaseDao<ContactEntity>()

@Dao
abstract class ContactEmailDao: BaseDao<ContactEmailEntity>() {
    @Query("SELECT * FROM ContactEmailEntity WHERE userId = :userId")
    abstract fun getAllContactsEmails(userId: UserId): Flow<List<ContactEmailEntity>>

    @Query("DELETE FROM ContactEmailEntity WHERE userId = :userId")
    abstract suspend fun deleteAllContactsEmails(userId: UserId)

    @Query("DELETE FROM ContactEmailEntity")
    abstract suspend fun deleteAllContactsEmails()
}

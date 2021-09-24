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

package me.proton.core.contact.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import me.proton.core.contact.data.local.db.entity.ContactEntity
import me.proton.core.contact.data.local.db.entity.relation.ContactWithMailsAndCardsRelation
import me.proton.core.contact.data.local.db.entity.relation.ContactWithMailsRelation
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId

@Dao
abstract class ContactDao: BaseDao<ContactEntity>() {
    @Transaction
    @Query("SELECT * FROM ContactEntity WHERE contactId = :contactId")
    abstract fun observeContact(contactId: ContactId): Flow<ContactWithMailsAndCardsRelation>

    @Transaction
    @Query("SELECT * FROM ContactEntity WHERE userId = :userId")
    abstract fun observeAllContacts(userId: UserId): Flow<List<ContactWithMailsRelation>>

    @Query("DELETE FROM ContactEntity WHERE contactId = :contactId")
    abstract suspend fun deleteContact(contactId: ContactId)

    @Query("DELETE FROM ContactEntity")
    abstract suspend fun deleteAllContacts()

    @Query("DELETE FROM ContactEntity WHERE userId IN (:userIds)")
    abstract suspend fun deleteAllContacts(vararg userIds: UserId)
}

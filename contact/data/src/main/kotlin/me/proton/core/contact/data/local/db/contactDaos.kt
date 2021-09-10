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
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId

@Dao
abstract class ContactDao: BaseDao<ContactEntity>() {
    @Transaction
    @Query("SELECT * FROM ContactEntity WHERE contactId = :contactId")
    abstract fun getContact(contactId: ContactId): Flow<ContactCompoundEntity>

    @Query("DELETE FROM ContactEntity WHERE contactId = :contactId")
    abstract suspend fun deleteContact(contactId: ContactId)

    @Query("DELETE FROM ContactEntity")
    abstract suspend fun deleteAllContacts()

    @Query("DELETE FROM ContactEntity WHERE userId = :userId")
    abstract suspend fun deleteAllContacts(userId: UserId)
}

@Dao
abstract class ContactCardDao: BaseDao<ContactCardEntity>() {
    @Query("DELETE FROM ContactCardEntity WHERE contactId = :contactId")
    abstract suspend fun deleteAllContactCards(contactId: ContactId)
}

@Dao
abstract class ContactEmailDao: BaseDao<ContactEmailEntity>() {
    @Query("SELECT * FROM ContactEmailEntity WHERE userId = :userId ORDER BY `order`, name")
    abstract fun getAllContactsEmails(userId: UserId): Flow<List<ContactEmailEntity>>

    @Query("DELETE FROM ContactEmailEntity WHERE userId = :userId")
    abstract suspend fun deleteAllContactsEmails(userId: UserId)

    @Query("DELETE FROM ContactEmailEntity WHERE contactId = :contactId")
    abstract suspend fun deleteAllContactsEmails(contactId: ContactId)

    @Query("DELETE FROM ContactEmailEntity")
    abstract suspend fun deleteAllContactsEmails()
}

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
import me.proton.core.contact.data.local.db.entity.ContactEmailEntity
import me.proton.core.contact.data.local.db.entity.relation.ContactEmailWithLabelsRelation
import me.proton.core.contact.domain.entity.ContactEmailId
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId

@Dao
abstract class ContactEmailDao: BaseDao<ContactEmailEntity>() {
    @Transaction
    @Query("SELECT * FROM ContactEmailEntity WHERE userId = :userId ORDER BY `order`, name")
    abstract fun observeAllContactsEmails(userId: UserId): Flow<List<ContactEmailWithLabelsRelation>>

    @Transaction
    @Query("SELECT * FROM ContactEmailEntity WHERE contactId = :contactId ORDER BY `order`, name")
    abstract fun observeAllContactsEmails(contactId: ContactId): Flow<List<ContactEmailWithLabelsRelation>>

    @Query("DELETE FROM ContactEmailEntity WHERE contactId IN (:contactEmailIds)")
    protected abstract suspend fun deleteContactsEmailsSingleBatch(vararg contactEmailIds: ContactEmailId)

    @Transaction
    open suspend fun deleteContactsEmails(vararg contactEmailIds: ContactEmailId) {
        contactEmailIds.toList().chunked(SQLITE_MAX_VARIABLE_NUMBER).forEach {
            deleteContactsEmailsSingleBatch(*it.toTypedArray())
        }
    }

    @Query("DELETE FROM ContactEmailEntity WHERE userId = :userId")
    abstract suspend fun deleteAllContactsEmails(userId: UserId)

    @Query("DELETE FROM ContactEmailEntity WHERE contactId IN (:contactIds)")
    protected abstract suspend fun deleteAllContactsEmailsSingleBatch(vararg contactIds: ContactId)

    @Transaction
    open suspend fun deleteAllContactsEmails(vararg contactIds: ContactId) {
        contactIds.toList().chunked(SQLITE_MAX_VARIABLE_NUMBER).forEach {
            deleteAllContactsEmailsSingleBatch(*it.toTypedArray())
        }
    }

    @Query("DELETE FROM ContactEmailEntity")
    abstract suspend fun deleteAllContactsEmails()
}

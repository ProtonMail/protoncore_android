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

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.domain.entity.UserId

interface ContactDatabase {
    fun contactDao(): ContactDao
    fun contactEmailDao(): ContactEmailDao

    fun getAllContactsEmails(userId: UserId): Flow<List<ContactEmail>> {
        return contactEmailDao().getAllContactsEmails(userId).map { entities ->
            entities.map {
                it.toContactEmail()
            }
        }
    }

    suspend fun insertOrUpdateContactsEmails(userId: UserId, contactsEmails: List<ContactEmail>) {
        val entities = contactsEmails.map { it.toContactEmailEntity(userId) }
        contactEmailDao().insertOrUpdate(*entities.toTypedArray())
    }

    companion object {
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                TODO("provide migration when feature done")
            }
        }
    }
}

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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.domain.entity.UserId

interface ContactDatabase: Database {
    fun contactDao(): ContactDao
    fun contactCardDao(): ContactCardDao
    fun contactEmailDao(): ContactEmailDao
    fun contactEmailLabelDao(): ContactEmailLabelCrossRefDao

    fun getContact(contactId: ContactId): Flow<Contact> {
        return contactDao().getContact(contactId).map { it.toContact() }
    }

    suspend fun deleteContact(contactId: ContactId) {
        inTransaction {
            contactEmailDao().deleteAllContactsEmails(contactId = contactId)
            contactDao().deleteContact(contactId = contactId)
        }
    }

    suspend fun deleteAllContacts(userId: UserId) {
        inTransaction {
            contactDao().deleteAllContacts(userId)
            contactEmailDao().deleteAllContactsEmails(userId)
        }
    }

    suspend fun deleteAllContacts() {
        inTransaction {
            contactDao().deleteAllContacts()
            contactEmailDao().deleteAllContactsEmails()
        }
    }

    suspend fun insertOrUpdate(userId: UserId, contact: Contact) {
        inTransaction {
            contactDao().insertOrUpdate(contact.toContactEntity(userId))

            contactCardDao().deleteAllContactCards(contact.id)
            val contactCardsEntities = contact.cards.map { it.toContactCardEntity(contact.id) }
            contactCardDao().insertOrUpdate(*contactCardsEntities.toTypedArray())

            contactEmailDao().deleteAllContactsEmails(contactId = contact.id)
            insertOrUpdateContactsEmails(userId, contact.contactEmails)
        }
    }

    fun getAllContactsEmails(userId: UserId): Flow<List<ContactEmail>> {
        return contactEmailDao().getAllContactsEmails(userId).map { entities ->
            entities.map {
                it.toContactEmail()
            }
        }.distinctUntilChanged()
    }

    suspend fun insertOrUpdateContactsEmails(userId: UserId, contactsEmails: List<ContactEmail>) {
        inTransaction {
            contactEmailLabelDao().deleteAllLabels(contactsEmails.map { it.id })

            val mailEntities = contactsEmails.map { it.toContactEmailEntity(userId) }
            contactEmailDao().insertOrUpdate(*mailEntities.toTypedArray())

            val mailLabelEntities = contactsEmails.flatMap { it.toContactEmailLabelCrossRefs() }
            contactEmailLabelDao().insertOrUpdate(*mailLabelEntities.toTypedArray())
        }
    }

    companion object {
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                TODO("provide migration when feature done")
            }
        }
    }
}

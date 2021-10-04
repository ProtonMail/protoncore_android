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

package me.proton.core.contact.tests

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.proton.core.contact.domain.entity.ContactEmailId
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TransactionTests: ContactDatabaseTests() {

    @Test
    fun `delete contact delete contact and emails`() = runBlocking {
        givenUser0InDb()
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactEmailDao().insertOrUpdate(User0.Contact0.ContactEmail0.contactEmailEntity)
        db.contactDao().deleteContacts(User0.Contact0.contactId)
        assert(db.contactDao().observeContact(User0.Contact0.contactId).firstOrNull() == null)
        assert(db.contactEmailDao().observeAllContactsEmails(User0.Contact0.contactId).first().isEmpty())
    }

    @Test
    fun `delete all contacts from user also delete all contacts and emails from user`() = runBlocking {
        givenUser0InDb()
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactEmailDao().insertOrUpdate(User0.Contact0.ContactEmail0.contactEmailEntity)
        db.contactDao().deleteAllContacts(User0.userId)
        assert(db.contactDao().observeContact(User0.Contact0.contactId).firstOrNull() == null)
        assert(db.contactEmailDao().observeAllContactsEmails(User0.Contact0.contactId).first().isEmpty())
    }

    @Test
    fun `delete all contacts delete all contacts and emails`() = runBlocking {
        givenUser0InDb()
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactEmailDao().insertOrUpdate(User0.Contact0.ContactEmail0.contactEmailEntity)
        db.contactDao().deleteAllContacts()
        assert(db.contactDao().observeContact(User0.Contact0.contactId).firstOrNull() == null)
        assert(db.contactEmailDao().observeAllContactsEmails(User0.Contact0.contactId).first().isEmpty())
    }

    @Test
    fun `merge contacts apply correct diff`() = runBlocking {
        givenUser0InDb()
        val baseEmails = listOf(User0.Contact0.createContactEmail(ContactEmailId("a"), emptyList()))
        val updatedEmails = listOf(User0.Contact0.createContactEmail(ContactEmailId("b"), emptyList()))
        val baseContact = User0.Contact0.contact.copy(contactEmails = baseEmails)
        val updatedContact = User0.Contact0.contact.copy(contactEmails = updatedEmails)
        localDataSource.mergeContacts(baseContact)
        assert(localDataSource.observeContact(User0.Contact0.contactId).first()?.contact == baseContact)
        localDataSource.mergeContacts(updatedContact)
        assert(localDataSource.observeContact(User0.Contact0.contactId).first()?.contact == updatedContact)
    }

    @Test
    fun `merge contacts with cards apply correct diff`() = runBlocking {
        givenUser0InDb()
        val baseCards = listOf(contactCard("card-a"))
        val updatedCards = listOf(contactCard("card-b"))
        val baseEmails = listOf(User0.Contact0.createContactEmail(ContactEmailId("a"), emptyList()))
        val updatedEmails = listOf(User0.Contact0.createContactEmail(ContactEmailId("b"), emptyList()))
        val baseContact = User0.Contact0.contactWithCards.copy(
            contact = User0.Contact0.contactWithCards.contact.copy(contactEmails = baseEmails),
            contactCards = baseCards,
        )
        val updatedContact = User0.Contact0.contactWithCards.copy(
            contact = User0.Contact0.contactWithCards.contact.copy(contactEmails = updatedEmails),
            contactCards = updatedCards,
        )
        localDataSource.upsertContactWithCards(baseContact)
        assert(localDataSource.observeContact(User0.Contact0.contactId).first() == baseContact)
        localDataSource.upsertContactWithCards(updatedContact)
        assert(localDataSource.observeContact(User0.Contact0.contactId).first() == updatedContact)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun `upsert contacts throws if user not present`() = runBlocking {
        localDataSource.upsertContacts(User0.Contact0.contact)
    }

    @Test
    fun `upsert contacts doesn't throws if user is present`() = runBlocking {
        givenUser0InDb()
        localDataSource.upsertContacts(User0.Contact0.contact)
        assert(localDataSource.observeContact(User0.Contact0.contactId).first()?.contact == User0.Contact0.contact)
    }
}
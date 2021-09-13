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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.proton.core.contact.domain.entity.ContactEmailId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContactDatabaseTests {

    private lateinit var db: TestDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TestDatabase::class.java).build()

        // Room does not support runBlockingTest (especially for transactions) so have to use runBlocking.
        // Assuming we have an user
        runBlocking {
            db.accountDao().insertOrUpdate(User0.accountEntity)
            db.userDao().insertOrUpdate(User0.userEntity)
        }
    }

    @Test
    fun `contact is deleted on foreign key deletion`() = runBlocking {
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        assert(db.contactDao().getContact(User0.Contact0.contactId).firstOrNull() != null)
        db.userDao().delete(User0.userId)
        assert(db.contactDao().getContact(User0.Contact0.contactId).firstOrNull() == null)
    }

    @Test
    fun `contact card deleted on foreign key deletion`() = runBlocking {
        val hasContactCard = suspend {
            db.contactDao().getContact(User0.Contact0.contactId).firstOrNull()?.cards?.any {
                it.data == User0.Contact0.ContactCard0.contactCardEntity.data
            } ?: false
        }
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactCardDao().insertOrUpdate(User0.Contact0.ContactCard0.contactCardEntity)
        assert(hasContactCard())
        db.contactDao().deleteContact(User0.Contact0.contactId)
        assert(!hasContactCard())
    }

    @Test
    fun `contact email is deleted on foreign key deletion`() = runBlocking {
        val hasContactEmail = suspend {
            db.contactEmailDao().getAllContactsEmails(User0.userId).first().any {
                it.contactEmail.contactEmailId == User0.Contact0.ContactEmail0.contactEmailId
            }
        }
        db.contactEmailDao().insertOrUpdate(User0.Contact0.ContactEmail0.contactEmailEntity)
        assert(hasContactEmail())
        db.userDao().delete(User0.userId)
        assert(!hasContactEmail())
    }

    @Test
    fun `contact email label cross ref deleted on foreign key deletion`() = runBlocking {
        val testContactEmail = User0.Contact0.ContactEmail0
        val hasLabels = suspend {
            val dbLabels = db.contactEmailLabelDao().getAllLabels(testContactEmail.contactEmailId).first()
            dbLabels == testContactEmail.contactEmail.labelIds
        }
        db.contactEmailDao().insertOrUpdate(testContactEmail.contactEmailEntity)
        db.contactEmailLabelDao().insertOrUpdate(*testContactEmail.emailLabelEntities)
        assert(hasLabels())
        db.contactEmailDao().deleteAllContactsEmails(User0.Contact0.contactId)
        assert(!hasLabels())
    }

    // Transactions testing
    @Test
    fun `delete contact delete contact and emails`() = runBlocking {
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactEmailDao().insertOrUpdate(User0.Contact0.ContactEmail0.contactEmailEntity)
        db.deleteContact(User0.Contact0.contactId)
        assert(db.contactDao().getContact(User0.Contact0.contactId).firstOrNull() == null)
        assert(db.contactEmailDao().getAllContactsEmails(User0.Contact0.contactId).first().isEmpty())
    }

    @Test
    fun `delete all contacts from user also delete all contacts and emails from user`() = runBlocking {
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactEmailDao().insertOrUpdate(User0.Contact0.ContactEmail0.contactEmailEntity)
        db.deleteAllContacts(User0.userId)
        assert(db.contactDao().getContact(User0.Contact0.contactId).firstOrNull() == null)
        assert(db.contactEmailDao().getAllContactsEmails(User0.Contact0.contactId).first().isEmpty())
    }

    @Test
    fun `delete all contacts delete all contacts and emails`() = runBlocking {
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactEmailDao().insertOrUpdate(User0.Contact0.ContactEmail0.contactEmailEntity)
        db.deleteAllContacts()
        assert(db.contactDao().getContact(User0.Contact0.contactId).firstOrNull() == null)
        assert(db.contactEmailDao().getAllContactsEmails(User0.Contact0.contactId).first().isEmpty())
    }

    @Test
    fun `insert or update contacts emails apply correct diff`() = runBlocking {
        db.contactEmailDao().insertOrUpdate(User0.Contact0.ContactEmail0.contactEmailEntity)
        db.contactEmailLabelDao().insertOrUpdate(*User0.Contact0.ContactEmail0.emailLabelEntities)

        val updatedLabels = User0.Contact0.ContactEmail0.contactEmail.labelIds.map { it.reversed() }
        val updatedContactEmail = User0.Contact0.ContactEmail0.contactEmail.copy(
            labelIds = updatedLabels
        )
        db.insertOrUpdateContactsEmails(User0.userId, listOf(updatedContactEmail))

        assert(db.contactEmailLabelDao().getAllLabels(User0.Contact0.ContactEmail0.contactEmailId).first() == updatedLabels)
    }

    @Test
    fun `insert or update contact apply correct diff`() = runBlocking {
        val baseCards = listOf(contactCard("card-a"))
        val updatedCards = listOf(contactCard("card-b"))
        val baseEmails = listOf(User0.Contact0.contactId.contactEmail(ContactEmailId("a"), emptyList()))
        val updatedEmails = listOf(User0.Contact0.contactId.contactEmail(ContactEmailId("b"), emptyList()))
        val baseContact = User0.Contact0.contact.copy(
            cards = baseCards,
            contactEmails = baseEmails
        )
        val updatedContact = User0.Contact0.contact.copy(
            cards = updatedCards,
            contactEmails = updatedEmails
        )

        db.insertOrUpdate(User0.userId, baseContact)
        assert(db.getContact(User0.Contact0.contactId).first() == baseContact)
        db.insertOrUpdate(User0.userId, updatedContact)
        assert(db.getContact(User0.Contact0.contactId).first() == updatedContact)
    }

    @After
    fun closeDb() {
        db.close()
    }
}

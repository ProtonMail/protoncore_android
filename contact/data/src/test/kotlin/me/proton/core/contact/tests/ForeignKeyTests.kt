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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ForeignKeyTests : ContactDatabaseTests() {

    @Test
    fun `contact is deleted on foreign key deletion`() = runBlocking {
        givenUser0InDb()
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        assert(db.contactDao().observeContact(User0.Contact0.contactId).firstOrNull() != null)
        db.userDao().delete(User0.userId)
        assert(db.contactDao().observeContact(User0.Contact0.contactId).firstOrNull() == null)
    }

    @Test
    fun `contact card deleted on foreign key deletion`() = runBlocking {
        givenUser0InDb()
        val hasContactCard = suspend {
            db.contactDao().observeContact(User0.Contact0.contactId).firstOrNull()?.cards?.any {
                it.data == User0.Contact0.ContactCard0.contactCardEntity.data
            } ?: false
        }
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactCardDao().insertOrUpdate(User0.Contact0.ContactCard0.contactCardEntity)
        assert(hasContactCard())
        db.contactDao().deleteContacts(User0.Contact0.contactId)
        assert(!hasContactCard())
    }

    @Test
    fun `contact email is deleted on foreign key deletion`() = runBlocking {
        givenUser0InDb()
        val hasContactEmail = suspend {
            db.contactEmailDao().observeAllContactsEmails(User0.userId).first().any {
                it.contactEmail.contactEmailId == User0.Contact0.ContactEmail0.contactEmailId
            }
        }
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactEmailDao().insertOrUpdate(User0.Contact0.ContactEmail0.contactEmailEntity)
        assert(hasContactEmail())
        db.userDao().delete(User0.userId)
        assert(!hasContactEmail())
    }

    @Test
    fun `contact email label cross ref deleted on foreign key deletion`() = runBlocking {
        givenUser0InDb()
        val testContactEmail = User0.Contact0.ContactEmail0
        val hasLabels = suspend {
            val dbLabels = db.contactEmailLabelDao().observeAllLabels(testContactEmail.contactEmailId).first()
            dbLabels == testContactEmail.contactEmail.labelIds
        }
        db.contactDao().insertOrUpdate(User0.Contact0.contactEntity)
        db.contactEmailDao().insertOrUpdate(testContactEmail.contactEmailEntity)
        db.contactEmailLabelDao().insertOrUpdate(*testContactEmail.emailLabelEntities)
        assert(hasLabels())
        db.contactEmailDao().deleteAllContactsEmails(User0.Contact0.contactId)
        assert(!hasLabels())
    }
}

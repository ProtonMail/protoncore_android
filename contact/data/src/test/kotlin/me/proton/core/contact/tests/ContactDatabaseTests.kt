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
            db.accountDao().insertOrUpdate(account0)
            db.userDao().insertOrUpdate(user0)
        }
    }

    @Test
    fun `contact is deleted on foreign key deletion`() = runBlocking {
        db.contactDao().insertOrUpdate(contact1)
        assert(db.contactDao().getContact(contact1_id).firstOrNull() != null)
        db.userDao().delete(user0_id)
        assert(db.contactDao().getContact(contact1_id).firstOrNull() == null)
    }

    @Test
    fun `contact card deleted on foreign key deletion`() = runBlocking {
        val hasContactCard = suspend {
            db.contactDao().getContact(contact1_id).firstOrNull()?.cards?.any {
                it.data == contactCard1_1.data
            } ?: false
        }
        db.contactDao().insertOrUpdate(contact1)
        db.contactCardDao().insertOrUpdate(contactCard1_1)
        assert(hasContactCard())
        db.contactDao().deleteContact(contactId = contact1_id)
        assert(!hasContactCard())
    }

    @Test
    fun `contact email is deleted on foreign key deletion`() = runBlocking {
        val hasContactEmail = suspend {
            db.contactEmailDao().getAllContactsEmails(user0_id).first().any {
                it.contactEmail.contactEmailId == contactEmail1_1_id
            }
        }
        db.insertOrUpdateContactsEmails(user0_id, listOf(contactEmail1_1))
        assert(hasContactEmail())
        db.userDao().delete(user0_id)
        assert(!hasContactEmail())
    }

    @Test
    fun `contact email label cross ref deleted on foreign key deletion`() = runBlocking {
        val hasLabels = suspend {
            db.contactEmailLabelDao().getAllLabels(contactEmail1_1_id).first() == contactEmail1_1.labelIds
        }
        db.insertOrUpdateContactsEmails(user0_id, listOf(contactEmail1_1))
        assert(hasLabels())
        db.contactEmailDao().deleteAllContactsEmails(contact1_id)
        assert(!hasLabels())
    }

    @After
    fun closeDb() {
        db.close()
    }
}

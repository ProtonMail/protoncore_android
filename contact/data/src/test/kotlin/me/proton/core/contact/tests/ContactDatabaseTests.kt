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
import kotlinx.coroutines.runBlocking
import me.proton.core.contact.data.local.db.ContactLocalDataSourceImpl
import me.proton.core.contact.domain.repository.ContactLocalDataSource
import org.junit.After
import org.junit.Before

abstract class ContactDatabaseTests {

    protected lateinit var db: TestDatabase
    protected lateinit var localDataSource: ContactLocalDataSource

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TestDatabase::class.java).build()
        localDataSource = ContactLocalDataSourceImpl(db)
    }

    fun givenUser0InDb() {
        // Room does not support runBlockingTest (especially for transactions) so have to use runBlocking.
        // Assuming we have an user
        runBlocking {
            db.accountDao().insertOrUpdate(User0.accountEntity)
            db.userDao().insertOrUpdate(User0.userEntity)
        }
    }

    @After
    fun closeDb() {
        db.close()
    }
}

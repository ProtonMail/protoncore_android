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
import kotlinx.coroutines.runBlocking
import me.proton.core.label.data.local.toLabel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TransactionTests : LabelDatabaseTests() {

    @Test
    fun `delete all users delete all labels`() = runBlocking {
        givenUser0InDb()
        db.labelDao().insertOrUpdate(User0.label0Entity)
        db.userDao().delete(User0.userEntity)
        assert(db.userDao().getByUserId(User0.userId) == null)
        assert(db.labelDao().getAll(User0.userId, User0.label0Type.value).isEmpty())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun `upsert label throws if user not present`() = runBlocking {
        localDataSource.upsertLabel(listOf(User0.label0Entity.toLabel()))
    }

    @Test
    fun `upsert label doesn't throws if user is present`() = runBlocking {
        givenUser0InDb()
        localDataSource.upsertLabel(listOf(User0.label0Entity.toLabel()))
        assert(db.labelDao().getAll(User0.userId, User0.label0Type.value).isNotEmpty())
    }
}

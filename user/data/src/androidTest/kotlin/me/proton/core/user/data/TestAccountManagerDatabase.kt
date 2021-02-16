/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.user.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.accountmanager.data.db.AccountManagerDatabase

object TestAccountManagerDatabase {

    /**
     * Build a multi-threaded Room Database, without touching default dispatchers.
     *
     * [RoomDatabase.inTransaction] do not support a single threaded model (unlikely to be fixed in future).
     *
     * [TestCoroutineDispatcher] is single threaded, and [runBlockingTest] don't support more than one thread.
     *
     * We are currently forced to use runBlocking + withTimeout (to avoid waiting indefinitely).
     *
     * @see <a href="https://medium.com/@eyalg/testing-androidx-room-kotlin-coroutines-2d1faa3e674f">Explanation</a>
     */
    fun buildMultiThreaded(): AccountManagerDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, AccountManagerDatabase::class.java)
            .build()
    }
}

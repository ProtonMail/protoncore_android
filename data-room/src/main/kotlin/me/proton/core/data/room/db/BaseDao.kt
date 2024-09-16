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

package me.proton.core.data.room.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

abstract class BaseDao<in T> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnore(vararg entities: T)

    @Transaction
    open suspend fun insertOrUpdate(vararg entities: T) {
        update(*entities)
        insertOrIgnore(*entities)
    }

    @Update
    abstract suspend fun update(vararg entities: T): Int

    @Delete
    abstract suspend fun delete(vararg entities: T)

    /**
     * When using `DELETE FROM table WHERE column IN (item1.., itemN)`,
     * there is a maximum number of items that SQLite can handle at once.
     */
    @Transaction
    open suspend fun <V> deleteChunked(entities: List<V>, delete: suspend (List<V>) -> Unit) {
        entities.chunked(SQLITE_MAX_VARIABLE_NUMBER).forEach {
            delete(it)
        }
    }

    companion object {
        /** Maximum Number Of Host Parameters In A Single SQL Statement
         * https://www.sqlite.org/limits.html
         **/
        @JvmStatic
        protected val SQLITE_MAX_VARIABLE_NUMBER = 999
    }
}

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

package me.proton.core.key.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration

interface KeySaltDatabase : Database {
    fun keySaltDao(): KeySaltDao

    companion object {
        /**
         * - Create Table KeySaltEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create Table KeySaltEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `KeySaltEntity` (`userId` TEXT NOT NULL, `keyId` TEXT NOT NULL, `keySalt` TEXT, PRIMARY KEY(`userId`, `keyId`))")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_KeySaltEntity_userId` ON `KeySaltEntity` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_KeySaltEntity_keyId` ON `KeySaltEntity` (`keyId`)")
            }
        }
    }
}

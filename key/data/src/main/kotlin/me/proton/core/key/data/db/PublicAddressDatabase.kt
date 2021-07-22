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

interface PublicAddressDatabase : Database {
    fun publicAddressDao(): PublicAddressDao
    fun publicAddressKeyDao(): PublicAddressKeyDao
    fun publicAddressWithKeysDao(): PublicAddressWithKeysDao

    companion object {
        /**
         * - Create Table PublicAddressEntity.
         * - Create Table PublicAddressKeyEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create Table PublicAddressEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `PublicAddressEntity` (`email` TEXT NOT NULL, `recipientType` INTEGER NOT NULL, `mimeType` TEXT, PRIMARY KEY(`email`))")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_PublicAddressEntity_email` ON `PublicAddressEntity` (`email`)")
                // Create Table PublicAddressKeyEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `PublicAddressKeyEntity` (`email` TEXT NOT NULL, `flags` INTEGER NOT NULL, `publicKey` TEXT NOT NULL, `isPrimary` INTEGER NOT NULL, PRIMARY KEY(`email`, `publicKey`), FOREIGN KEY(`email`) REFERENCES `PublicAddressEntity`(`email`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_PublicAddressKeyEntity_email` ON `PublicAddressKeyEntity` (`email`)")
            }
        }
    }
}

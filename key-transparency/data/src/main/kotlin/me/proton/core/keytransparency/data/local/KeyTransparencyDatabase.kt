/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.keytransparency.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration

public interface KeyTransparencyDatabase : Database {

    public fun addressChangeDao(): AddressChangeDao

    public fun selfAuditResultDao(): SelfAuditResultDao

    public companion object {
        public val MIGRATION_0: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS `AddressChangeEntity` (
                        `userId` TEXT NOT NULL,
                        `changeId` TEXT NOT NULL,
                        `counterEncrypted` TEXT NOT NULL,
                        `emailEncrypted` TEXT NOT NULL,
                        `epochIdEncrypted` TEXT NOT NULL,
                        `creationTimestampEncrypted` TEXT NOT NULL,
                        `publicKeysEncrypted` TEXT NOT NULL,
                        `isObsolete` TEXT NOT NULL,
                        PRIMARY KEY(`userId`, `changeId`),
                        FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                        )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS `SelfAuditResultEntity` (
                        `userId` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        PRIMARY KEY(`userId`),
                        FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                    """.trimIndent()
                )
            }
        }
    }
}

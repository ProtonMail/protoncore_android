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

package me.proton.core.user.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.extension.addTableColumn
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.user.data.db.dao.UserDao
import me.proton.core.user.data.db.dao.UserWithKeysDao

interface UserDatabase : Database, UserKeyDatabase {
    fun userDao(): UserDao
    fun userWithKeysDao(): UserWithKeysDao

    companion object {
        /**
         * - Create Table UserEntity.
         * - Create Table UserKeyEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create Table UserEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `UserEntity` (`userId` TEXT NOT NULL, `email` TEXT, `name` TEXT, `displayName` TEXT, `currency` TEXT NOT NULL, `credit` INTEGER NOT NULL, `usedSpace` INTEGER NOT NULL, `maxSpace` INTEGER NOT NULL, `maxUpload` INTEGER NOT NULL, `role` INTEGER, `private` INTEGER NOT NULL, `subscribed` INTEGER NOT NULL, `services` INTEGER NOT NULL, `delinquent` INTEGER, `passphrase` BLOB, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `AccountEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_UserEntity_userId` ON `UserEntity` (`userId`)")
                // Create Table UserKeyEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `UserKeyEntity` (`userId` TEXT NOT NULL, `keyId` TEXT NOT NULL, `version` INTEGER NOT NULL, `privateKey` TEXT NOT NULL, `isPrimary` INTEGER NOT NULL, `fingerprint` TEXT, `activation` TEXT, PRIMARY KEY(`keyId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_UserKeyEntity_userId` ON `UserKeyEntity` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_UserKeyEntity_keyId` ON `UserKeyEntity` (`keyId`)")
            }
        }

        /**
         * - Added UserKeyEntity.active.
         * - Added UserKeyEntity.isUnlockable.
         */
        val MIGRATION_1 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "UserKeyEntity",
                    column = "active",
                    type = "INTEGER",
                    defaultValue = null
                )
                database.addTableColumn(
                    table = "UserKeyEntity",
                    column = "isUnlockable",
                    type = "INTEGER NOT NULL",
                    defaultValue = "0"
                )
            }
        }

        /**
         * - Added UserEntity.recovery.
         */
        val MIGRATION_2 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    addTableColumn("UserEntity", column = "recovery_state", type = "INTEGER")
                    addTableColumn("UserEntity", column = "recovery_startTime", type = "INTEGER")
                    addTableColumn("UserEntity", column = "recovery_endTime", type = "INTEGER")
                    addTableColumn("UserEntity", column = "recovery_sessionId", type = "TEXT")
                    addTableColumn("UserEntity", column = "recovery_reason", type = "INTEGER")
                }
            }
        }

        /**
         * - Added UserEntity.createTimeMs
         */
        val MIGRATION_3 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    "UserEntity",
                    column = "createdAtUtc",
                    type = "INTEGER NOT NULL",
                    defaultValue = "0"
                )
            }
        }

        /**
         * - Added UserEntity columns: maxBaseSpace, maxDriveSpace, usedBaseSpace, usedDriveSpace.
         */
        val MIGRATION_4 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                arrayOf(
                    "maxBaseSpace",
                    "maxDriveSpace",
                    "usedBaseSpace",
                    "usedDriveSpace"
                ).forEach { column ->
                    database.addTableColumn(
                        table = "UserEntity",
                        column = column,
                        type = "INTEGER",
                        defaultValue = null
                    )
                }
            }
        }
    }
}

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

package me.proton.core.account.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.db.Database
import me.proton.core.data.db.extension.dropTable
import me.proton.core.data.db.extension.dropTableColumn
import me.proton.core.data.db.migration.DatabaseMigration

interface AccountDatabase : Database {
    fun accountDao(): AccountDao
    fun sessionDao(): SessionDao
    fun accountMetadataDao(): AccountMetadataDao
    fun sessionDetailsDao(): SessionDetailsDao

    companion object {
        /**
         * - Create Table AccountEntity.
         * - Create Table AccountMetadataEntity.
         * - Create Table SessionEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create Table AccountEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `AccountEntity` (`userId` TEXT NOT NULL, `username` TEXT NOT NULL, `email` TEXT, `state` TEXT NOT NULL, `sessionId` TEXT, `sessionState` TEXT, PRIMARY KEY(`userId`), FOREIGN KEY(`sessionId`) REFERENCES `SessionEntity`(`sessionId`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AccountEntity_sessionId` ON `AccountEntity` (`sessionId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AccountEntity_userId` ON `AccountEntity` (`userId`)")
                // Create Table AccountMetadataEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `AccountMetadataEntity` (`userId` TEXT NOT NULL, `product` TEXT NOT NULL, `primaryAtUtc` INTEGER NOT NULL, PRIMARY KEY(`userId`, `product`), FOREIGN KEY(`userId`) REFERENCES `AccountEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AccountMetadataEntity_userId` ON `AccountMetadataEntity` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AccountMetadataEntity_product` ON `AccountMetadataEntity` (`product`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AccountMetadataEntity_primaryAtUtc` ON `AccountMetadataEntity` (`primaryAtUtc`)")
                // Create Table SessionEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `SessionEntity` (`userId` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `accessToken` TEXT NOT NULL, `refreshToken` TEXT NOT NULL, `humanHeaderTokenType` TEXT, `humanHeaderTokenCode` TEXT, `scopes` TEXT NOT NULL, `product` TEXT NOT NULL, PRIMARY KEY(`sessionId`), FOREIGN KEY(`userId`) REFERENCES `AccountEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_SessionEntity_sessionId` ON `SessionEntity` (`sessionId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_SessionEntity_userId` ON `SessionEntity` (`userId`)")
            }
        }

        /**
         * - Added Table HumanVerificationDetailsEntity.
         */
        val MIGRATION_1 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Added Table HumanVerificationDetailsEntity
                database.execSQL("CREATE TABLE IF NOT EXISTS `HumanVerificationDetailsEntity` (`sessionId` TEXT NOT NULL, `verificationMethods` TEXT NOT NULL, `captchaVerificationToken` TEXT, PRIMARY KEY(`sessionId`), FOREIGN KEY(`sessionId`) REFERENCES `SessionEntity`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_HumanVerificationDetailsEntity_sessionId` ON `HumanVerificationDetailsEntity` (`sessionId`)")
            }
        }

        /**
         * - Added Table SessionDetailsEntity.
         */
        val MIGRATION_2 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Added Table SessionDetailsEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `SessionDetailsEntity` (`sessionId` TEXT NOT NULL, `initialEventId` TEXT NOT NULL, `requiredAccountType` TEXT NOT NULL, `secondFactorEnabled` INTEGER NOT NULL, `twoPassModeEnabled` INTEGER NOT NULL, `password` TEXT, PRIMARY KEY(`sessionId`), FOREIGN KEY(`sessionId`) REFERENCES `SessionEntity`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_SessionDetailsEntity_sessionId` ON `SessionDetailsEntity` (`sessionId`)")
            }
        }

        /**
         * - Drop columns SessionEntity humanHeaderTokenCode & humanHeaderTokenType.
         * - Drop Table HumanVerificationDetailsEntity.
         */
        val MIGRATION_3 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop columns humanHeaderTokenCode & humanHeaderTokenType.
                database.dropTableColumn(
                    table = "SessionEntity",
                    createTable = {
                        execSQL("CREATE TABLE IF NOT EXISTS `SessionEntity` (`userId` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `accessToken` TEXT NOT NULL, `refreshToken` TEXT NOT NULL, `scopes` TEXT NOT NULL, `product` TEXT NOT NULL, PRIMARY KEY(`sessionId`), FOREIGN KEY(`userId`) REFERENCES `AccountEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    },
                    createIndices = {
                        execSQL("CREATE INDEX IF NOT EXISTS `index_SessionEntity_sessionId` ON `SessionEntity` (`sessionId`)")
                        execSQL("CREATE INDEX IF NOT EXISTS `index_SessionEntity_userId` ON `SessionEntity` (`userId`)")
                    },
                    columns = listOf("humanHeaderTokenCode", "humanHeaderTokenType")
                )
                // Drop Table HumanVerificationDetailsEntity.
                database.dropTable(table = "HumanVerificationDetailsEntity")
            }
        }
    }
}

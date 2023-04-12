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
import me.proton.core.user.data.db.dao.AddressDao
import me.proton.core.user.data.db.dao.AddressWithKeysDao

interface AddressDatabase : Database, AddressKeyDatabase {
    fun addressDao(): AddressDao
    fun addressWithKeysDao(): AddressWithKeysDao

    companion object {
        /**
         * -  Create Table AddressEntity.
         * -  Create Table AddressKeyEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create Table AddressEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `AddressEntity` (`userId` TEXT NOT NULL, `addressId` TEXT NOT NULL, `email` TEXT NOT NULL, `displayName` TEXT, `domainId` TEXT, `canSend` INTEGER NOT NULL, `canReceive` INTEGER NOT NULL, `enabled` INTEGER NOT NULL, `type` INTEGER, `order` INTEGER NOT NULL, PRIMARY KEY(`addressId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AddressEntity_addressId` ON `AddressEntity` (`addressId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AddressEntity_userId` ON `AddressEntity` (`userId`)")

                // Create Table AddressKeyEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `AddressKeyEntity` (`addressId` TEXT NOT NULL, `keyId` TEXT NOT NULL, `version` INTEGER NOT NULL, `privateKey` TEXT NOT NULL, `isPrimary` INTEGER NOT NULL, `flags` INTEGER NOT NULL, `token` TEXT, `signature` TEXT, `fingerprint` TEXT, `fingerprints` TEXT, `activation` TEXT, `active` INTEGER NOT NULL, PRIMARY KEY(`keyId`), FOREIGN KEY(`addressId`) REFERENCES `AddressEntity`(`addressId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AddressKeyEntity_addressId` ON `AddressKeyEntity` (`addressId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AddressKeyEntity_keyId` ON `AddressKeyEntity` (`keyId`)")
            }
        }

        /**
         * - Added AddressEntity.signature.
         */
        val MIGRATION_1 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(table = "AddressEntity", column = "signature", type = "TEXT")
            }
        }

        /**
         * - Added AddressEntity.signedKeyList.
         */
        val MIGRATION_2 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "AddressEntity",
                    column = "signedKeyList_data",
                    type = "TEXT"
                )
                database.addTableColumn(
                    table = "AddressEntity",
                    column = "signedKeyList_signature",
                    type = "TEXT"
                )
            }
        }

        /**
         * - Added AddressKeyEntity.isUnlockable.
         * - Added AddressKeyEntity.passphrase.
         */
        val MIGRATION_3 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "AddressKeyEntity",
                    column = "isUnlockable",
                    type = "INTEGER NOT NULL",
                    defaultValue = "0"
                )
                database.addTableColumn(
                    table = "AddressKeyEntity",
                    column = "passphrase",
                    type = "BLOB",
                    defaultValue = null
                )
            }
        }

        /**
         * - Added KeyTransparency properties to PublicAddressEntity.signedKeyList.
         */
        val MIGRATION_4 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "AddressEntity",
                    column = "signedKeyList_minEpochId",
                    type = "INTEGER"
                )
                database.addTableColumn(
                    table = "AddressEntity",
                    column = "signedKeyList_maxEpochId",
                    type = "INTEGER"
                )
                database.addTableColumn(
                    table = "AddressEntity",
                    column = "signedKeyList_expectedMinEpochId",
                    type = "INTEGER"
                )
            }
        }
    }
}

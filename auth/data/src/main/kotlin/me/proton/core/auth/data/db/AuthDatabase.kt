/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.auth.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.auth.data.dao.AuthDeviceDao
import me.proton.core.auth.data.dao.DeviceSecretDao
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.extension.addTableColumn
import me.proton.core.data.room.db.extension.dropTableColumn
import me.proton.core.data.room.db.extension.recreateTable
import me.proton.core.data.room.db.migration.DatabaseMigration

interface AuthDatabase : Database {
    fun deviceSecretDao(): DeviceSecretDao
    fun authDeviceDao(): AuthDeviceDao

    companion object {
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `DeviceSecretEntity` (`userId` TEXT NOT NULL, `secret` TEXT NOT NULL, `token` TEXT NOT NULL, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `AccountEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_DeviceSecretEntity_userId` ON `DeviceSecretEntity` (`userId`)")
            }
        }

        val MIGRATION_1 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `AuthDeviceEntity` (`userId` TEXT NOT NULL, `deviceId` TEXT NOT NULL, `addressId` TEXT NOT NULL, `state` INTEGER NOT NULL, `name` TEXT NOT NULL, `localizedClientName` TEXT NOT NULL, `createdAtUtcSeconds` INTEGER NOT NULL, `activatedAtUtcSeconds` INTEGER, `rejectedAtUtcSeconds` INTEGER, `activationToken` TEXT, `lastActivityAtUtcSeconds` INTEGER NOT NULL, PRIMARY KEY(`userId`, `deviceId`), FOREIGN KEY(`userId`) REFERENCES `AccountEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AuthDeviceEntity_userId` ON `AuthDeviceEntity` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_AuthDeviceEntity_addressId` ON `AuthDeviceEntity` (`addressId`)")
            }
        }

        val MIGRATION_2 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.recreateTable(
                    table = "AuthDeviceEntity",
                    createTable = {
                        database.execSQL("CREATE TABLE IF NOT EXISTS `AuthDeviceEntity` (`userId` TEXT NOT NULL, `deviceId` TEXT NOT NULL, `addressId` TEXT, `state` INTEGER NOT NULL, `name` TEXT NOT NULL, `localizedClientName` TEXT NOT NULL, `createdAtUtcSeconds` INTEGER NOT NULL, `activatedAtUtcSeconds` INTEGER, `rejectedAtUtcSeconds` INTEGER, `activationToken` TEXT, `lastActivityAtUtcSeconds` INTEGER NOT NULL, PRIMARY KEY(`userId`, `deviceId`), FOREIGN KEY(`userId`) REFERENCES `AccountEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    },
                    createIndices = {
                        execSQL("CREATE INDEX IF NOT EXISTS `index_AuthDeviceEntity_userId` ON `AuthDeviceEntity` (`userId`)")
                        execSQL("CREATE INDEX IF NOT EXISTS `index_AuthDeviceEntity_addressId` ON `AuthDeviceEntity` (`addressId`)")
                    }
                )
            }
        }

        val MIGRATION_3 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.recreateTable(
                    table = "DeviceSecretEntity",
                    createTable = {
                        database.execSQL("CREATE TABLE IF NOT EXISTS `DeviceSecretEntity` (`userId` TEXT NOT NULL, `deviceId` TEXT NOT NULL, `secret` TEXT NOT NULL, `token` TEXT NOT NULL, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `AccountEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    },
                    createIndices = {
                        execSQL("CREATE INDEX IF NOT EXISTS `index_DeviceSecretEntity_userId` ON `DeviceSecretEntity` (`userId`)")
                    }
                )
            }
        }
    }
}

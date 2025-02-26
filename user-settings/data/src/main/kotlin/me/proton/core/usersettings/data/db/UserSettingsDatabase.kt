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

package me.proton.core.usersettings.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.extension.addTableColumn
import me.proton.core.data.room.db.extension.dropTableColumn
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.usersettings.data.db.dao.UserSettingsDao

interface UserSettingsDatabase : Database {
    fun userSettingsDao(): UserSettingsDao

    companion object {
        /**
         * - Added Table UserSettingsEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Added Table UserSettingsEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `UserSettingsEntity` (`userId` TEXT NOT NULL, `news` INTEGER, `locale` TEXT, `logAuth` INTEGER, `invoiceText` TEXT, `density` INTEGER, `theme` TEXT, `themeType` INTEGER, `weekStart` INTEGER, `dateFormat` INTEGER, `timeFormat` INTEGER, `welcome` INTEGER, `earlyAccess` INTEGER, `email_value` TEXT, `email_status` INTEGER, `email_notify` INTEGER, `email_reset` INTEGER, `phone_value` TEXT, `phone_status` INTEGER, `phone_notify` INTEGER, `phone_reset` INTEGER, `password_mode` INTEGER, `password_expirationTime` INTEGER, `twoFA_enabled` INTEGER, `twoFA_allowed` INTEGER, `twoFA_expirationTime` INTEGER, `twoFA_u2fKeys` TEXT, `flags_welcomed` INTEGER, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            }
        }

        /**
         * - Removed UserSettingsEntity twoFA_u2fKeys deprecated property.
         */
        val MIGRATION_1 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.dropTableColumn(
                    table = "UserSettingsEntity",
                    createTable = {
                        execSQL("CREATE TABLE IF NOT EXISTS `UserSettingsEntity` (`userId` TEXT NOT NULL, `news` INTEGER, `locale` TEXT, `logAuth` INTEGER, `invoiceText` TEXT, `density` INTEGER, `theme` TEXT, `themeType` INTEGER, `weekStart` INTEGER, `dateFormat` INTEGER, `timeFormat` INTEGER, `welcome` INTEGER, `earlyAccess` INTEGER, `email_value` TEXT, `email_status` INTEGER, `email_notify` INTEGER, `email_reset` INTEGER, `phone_value` TEXT, `phone_status` INTEGER, `phone_notify` INTEGER, `phone_reset` INTEGER, `password_mode` INTEGER, `password_expirationTime` INTEGER, `twoFA_enabled` INTEGER, `twoFA_allowed` INTEGER, `twoFA_expirationTime` INTEGER, `flags_welcomed` INTEGER, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    },
                    createIndices = {},
                    column = "twoFA_u2fKeys"
                )
            }
        }

        /**
         * - Removed unused properties: "invoiceText", "theme", "themeType", "welcome" and "flags_welcomed".
         */
        val MIGRATION_2 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.dropTableColumn(
                    table = "UserSettingsEntity",
                    createTable = {
                        execSQL("CREATE TABLE IF NOT EXISTS `UserSettingsEntity` (`userId` TEXT NOT NULL, `news` INTEGER, `locale` TEXT, `logAuth` INTEGER, `density` INTEGER, `weekStart` INTEGER, `dateFormat` INTEGER, `timeFormat` INTEGER, `earlyAccess` INTEGER, `email_value` TEXT, `email_status` INTEGER, `email_notify` INTEGER, `email_reset` INTEGER, `phone_value` TEXT, `phone_status` INTEGER, `phone_notify` INTEGER, `phone_reset` INTEGER, `password_mode` INTEGER, `password_expirationTime` INTEGER, `twoFA_enabled` INTEGER, `twoFA_allowed` INTEGER, `twoFA_expirationTime` INTEGER, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    },
                    createIndices = {},
                    columns = listOf(
                        "twoFA_u2fKeys", // Fix: if MIGRATION_1 is done inside same transaction.
                        "invoiceText",
                        "theme",
                        "themeType",
                        "welcome",
                        "flags_welcomed"
                    )
                )
            }
        }


        val MIGRATION_3 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "UserSettingsEntity",
                    column = "telemetry",
                    type = "INTEGER"
                )
            }
        }

        val MIGRATION_4 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "UserSettingsEntity",
                    column = "crashReports",
                    type = "INTEGER"
                )
            }
        }

        val MIGRATION_5 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "UserSettingsEntity",
                    column = "deviceRecovery",
                    type = "INTEGER"
                )
            }
        }

        val MIGRATION_6 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "UserSettingsEntity",
                    column = "sessionAccountRecovery",
                    type = "INTEGER"
                )
            }
        }

        val MIGRATION_7 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "UserSettingsEntity",
                    column = "twoFA_registeredKeys",
                    type = "TEXT"
                )
            }
        }

        val MIGRATION_8 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "UserSettingsEntity",
                    column = "easyDeviceMigrationOptOut",
                    type = "INTEGER"
                )
            }
        }
    }
}

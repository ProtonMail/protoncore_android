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

package me.proton.core.contact.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.contact.data.local.db.dao.ContactCardDao
import me.proton.core.contact.data.local.db.dao.ContactDao
import me.proton.core.contact.data.local.db.dao.ContactEmailDao
import me.proton.core.contact.data.local.db.dao.ContactEmailLabelDao
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.extension.addTableColumn
import me.proton.core.data.room.db.migration.DatabaseMigration

interface ContactDatabase: Database {
    fun contactDao(): ContactDao
    fun contactCardDao(): ContactCardDao
    fun contactEmailDao(): ContactEmailDao
    fun contactEmailLabelDao(): ContactEmailLabelDao

    companion object {
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create ContactEntity table
                database.execSQL("CREATE TABLE IF NOT EXISTS `ContactEntity` (`userId` TEXT NOT NULL, `contactId` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`contactId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ContactEntity_userId` ON `ContactEntity` (`userId`)")

                // Create ContactCardEntity table
                database.execSQL("CREATE TABLE IF NOT EXISTS `ContactCardEntity` (`contactId` TEXT NOT NULL, `type` INTEGER NOT NULL, `data` TEXT NOT NULL, `signature` TEXT, `cardId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, FOREIGN KEY(`contactId`) REFERENCES `ContactEntity`(`contactId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ContactCardEntity_contactId` ON `ContactCardEntity` (`contactId`)")

                // Create ContactEmailEntity table
                database.execSQL("CREATE TABLE IF NOT EXISTS `ContactEmailEntity` (`userId` TEXT NOT NULL, `contactEmailId` TEXT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `defaults` INTEGER NOT NULL, `order` INTEGER NOT NULL, `contactId` TEXT NOT NULL, `canonicalEmail` TEXT, PRIMARY KEY(`contactEmailId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`contactId`) REFERENCES `ContactEntity`(`contactId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ContactEmailEntity_userId` ON `ContactEmailEntity` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ContactEmailEntity_contactId` ON `ContactEmailEntity` (`contactId`)")

                // Create ContactEmailLabelEntity table
                database.execSQL("CREATE TABLE IF NOT EXISTS `ContactEmailLabelEntity` (`contactEmailId` TEXT NOT NULL, `labelId` TEXT NOT NULL, PRIMARY KEY(`contactEmailId`, `labelId`), FOREIGN KEY(`contactEmailId`) REFERENCES `ContactEmailEntity`(`contactEmailId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            }
        }

        val MIGRATION_1 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "ContactEmailEntity",
                    column = "isProton",
                    type = "INTEGER"
                )
            }
        }
    }
}

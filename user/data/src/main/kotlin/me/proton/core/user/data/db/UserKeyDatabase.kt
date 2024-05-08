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
import me.proton.core.data.room.db.extension.dropTableColumn
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.user.data.db.dao.UserKeyDao

interface UserKeyDatabase : Database {
    fun userKeyDao(): UserKeyDao

    companion object {
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "UserKeyEntity",
                    column = "recoverySecret",
                    type = "TEXT"
                )

                database.addTableColumn(
                    table = "UserKeyEntity",
                    column = "recoverySecretSignature",
                    type = "TEXT"
                )
            }
        }

        val MIGRATION_1 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.dropTableColumn(
                    table = "UserKeyEntity",
                    createTable = {
                        execSQL("CREATE TABLE IF NOT EXISTS `UserKeyEntity` (`userId` TEXT NOT NULL, `keyId` TEXT NOT NULL, `version` INTEGER NOT NULL, `privateKey` TEXT NOT NULL, `isPrimary` INTEGER NOT NULL, `isUnlockable` INTEGER NOT NULL, `fingerprint` TEXT, `activation` TEXT, `active` INTEGER, `recoverySecretSignature` TEXT, PRIMARY KEY(`keyId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    },
                    createIndices = {
                        execSQL("CREATE INDEX IF NOT EXISTS `index_UserKeyEntity_userId` ON `UserKeyEntity` (`userId`)")
                        execSQL("CREATE INDEX IF NOT EXISTS `index_UserKeyEntity_keyId` ON `UserKeyEntity` (`keyId`)")
                    },
                    column = "recoverySecret"
                )
                database.addTableColumn(
                    table = "UserKeyEntity",
                    column = "recoverySecretHash",
                    type = "TEXT",
                    defaultValue = null
                )
            }
        }
    }
}

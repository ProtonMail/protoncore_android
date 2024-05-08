/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.userrecovery.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.extension.addTableColumn
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.userrecovery.data.dao.DeviceRecoveryDao

interface DeviceRecoveryDatabase : Database {
    fun deviceRecoveryDao(): DeviceRecoveryDao

    companion object {
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `RecoveryFileEntity` (`userId` TEXT NOT NULL, `createdAtUtcMillis` INTEGER NOT NULL, `recoveryFile` TEXT NOT NULL, `recoverySecretHash` TEXT NOT NULL, PRIMARY KEY(`recoverySecretHash`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_RecoveryFileEntity_userId` ON `RecoveryFileEntity` (`userId`)")
            }
        }

        val MIGRATION_1 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "RecoveryFileEntity",
                    column = "keyCount",
                    type = "INTEGER"
                )
            }
        }
    }
}

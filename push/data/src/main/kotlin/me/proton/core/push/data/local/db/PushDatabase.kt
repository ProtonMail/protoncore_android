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

package me.proton.core.push.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration

public interface PushDatabase : Database {
    public fun pushDao(): PushDao

    public companion object {
        public val MIGRATION_0: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create PushEntity table
                @Suppress("MaxLineLength")
                database.execSQL("CREATE TABLE IF NOT EXISTS `PushEntity` (`userId` TEXT NOT NULL, `pushId` TEXT NOT NULL, `objectId` TEXT NOT NULL, `type` TEXT NOT NULL, PRIMARY KEY(`userId`, `pushId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_PushEntity_userId` ON `PushEntity` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_PushEntity_type` ON `PushEntity` (`type`)")
            }
        }
    }
}

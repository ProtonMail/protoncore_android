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

package me.proton.core.notification.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration

public interface NotificationDatabase  : Database {
    public fun notificationDao(): NotificationDao

    public companion object {
        public val MIGRATION_0: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create NotificationEntity table
                @Suppress("MaxLineLength")
                database.execSQL("CREATE TABLE IF NOT EXISTS `NotificationEntity` (`notificationId` TEXT NOT NULL, `userId` TEXT NOT NULL, `time` INTEGER NOT NULL, `type` TEXT NOT NULL, `title` TEXT, `subtitle` TEXT, `body` TEXT, PRIMARY KEY(`userId`, `notificationId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                @Suppress("MaxLineLength")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_NotificationEntity_userId` ON `NotificationEntity` (`userId`)")
                @Suppress("MaxLineLength")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_NotificationEntity_notificationId` ON `NotificationEntity` (`notificationId`)")
            }
        }
    }
}
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

package me.proton.core.eventmanager.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.extension.addTableColumn
import me.proton.core.data.room.db.extension.dropTableColumn
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.eventmanager.data.db.dao.EventMetadataDao

interface EventMetadataDatabase : Database {
    fun eventMetadataDao(): EventMetadataDao

    companion object {
        /**
         * - Added Table EventMetadataEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `EventMetadataEntity` (`userId` TEXT NOT NULL, `config` TEXT NOT NULL, `eventId` TEXT, `nextEventId` TEXT, `refresh` TEXT, `more` INTEGER, `response` TEXT, `retry` INTEGER NOT NULL, `state` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER, PRIMARY KEY(`userId`, `config`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_EventMetadataEntity_userId` ON `EventMetadataEntity` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_EventMetadataEntity_config` ON `EventMetadataEntity` (`config`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_EventMetadataEntity_createdAt` ON `EventMetadataEntity` (`createdAt`)")
            }
        }

        /**
         * - Remove EventMetadataEntity response.
         */
        val MIGRATION_1 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.dropTableColumn(
                    table = "EventMetadataEntity",
                    createTable = {
                        database.execSQL("CREATE TABLE IF NOT EXISTS `EventMetadataEntity` (`userId` TEXT NOT NULL, `config` TEXT NOT NULL, `eventId` TEXT, `nextEventId` TEXT, `refresh` TEXT, `more` INTEGER, `retry` INTEGER NOT NULL, `state` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER, PRIMARY KEY(`userId`, `config`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    },
                    createIndices = {
                        database.execSQL("CREATE INDEX IF NOT EXISTS `index_EventMetadataEntity_userId` ON `EventMetadataEntity` (`userId`)")
                        database.execSQL("CREATE INDEX IF NOT EXISTS `index_EventMetadataEntity_config` ON `EventMetadataEntity` (`config`)")
                        database.execSQL("CREATE INDEX IF NOT EXISTS `index_EventMetadataEntity_createdAt` ON `EventMetadataEntity` (`createdAt`)")
                    },
                    column = "response"
                )
                // Change state to Enqueued -> Force fetch again.
                database.execSQL("UPDATE `EventMetadataEntity` SET state = 'Enqueued' WHERE state != 'Cancelled' ")
            }
        }

        /**
         * - Changed EventManager EventsResponse File location.
         */
        val MIGRATION_2 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Change state to Enqueued -> Force fetch again.
                database.execSQL("UPDATE `EventMetadataEntity` SET state = 'Enqueued' WHERE state != 'Cancelled' ")
            }
        }

        /**
         * - Added EventMetadataEntity fetchedAt nullable column
         */
        val MIGRATION_3 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "EventMetadataEntity",
                    column = "fetchedAt",
                    type = "INTEGER",
                )
            }
        }
    }
}

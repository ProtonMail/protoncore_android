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

package me.proton.core.challenge.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.extension.addTableColumn
import me.proton.core.data.room.db.extension.dropTableColumn
import me.proton.core.data.room.db.migration.DatabaseMigration

public interface ChallengeDatabase : Database {
    public fun challengeFramesDao(): ChallengeFramesDao

    public companion object {
        /**
         * - Added Table FrameEntity.
         */
        public val MIGRATION_0: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Added Table ChallengeFrameEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `ChallengeFrameEntity` (`challengeFrame` TEXT NOT NULL, `flow` TEXT NOT NULL, `focusTime` INTEGER NOT NULL, `clicks` INTEGER NOT NULL, `copy` TEXT NOT NULL, `paste` TEXT NOT NULL, `keys` TEXT NOT NULL, PRIMARY KEY(`challengeFrame`))")
            }
        }

        public val MIGRATION_1: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Change focusTime column type from INTEGER to TEXT.
                database.dropTableColumn(
                    table = "ChallengeFrameEntity",
                    createTable = {
                        execSQL("CREATE TABLE IF NOT EXISTS `ChallengeFrameEntity` (`challengeFrame` TEXT NOT NULL, `focusTime` TEXT NOT NULL, `flow` TEXT NOT NULL, `clicks` INTEGER NOT NULL, `copy` TEXT NOT NULL, `paste` TEXT NOT NULL, `keys` TEXT NOT NULL, PRIMARY KEY(`challengeFrame`))")
                    },
                    createIndices = { },
                    column = "focusTime"
                )
            }
        }
    }
}

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
import me.proton.core.data.room.db.migration.DatabaseMigration

interface ChallengeDatabase : Database {
    fun challengeFramesDao(): ChallengeFramesDao

    companion object {
        /**
         * - Added Table FrameEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Added Table FrameEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `ChallengeFrameEntity` (`clientId` TEXT NOT NULL, `challengeId` TEXT NOT NULL, `challengeType` TEXT NOT NULL, `clientIdType` TEXT NOT NULL, `focusTime` INTEGER NOT NULL, `clicks` INTEGER NOT NULL, `copy` TEXT NOT NULL, `paste` TEXT NOT NULL, `keys` TEXT NOT NULL, PRIMARY KEY(`clientId`, `challengeId`, `challengeType`))")
            }
        }
    }
}

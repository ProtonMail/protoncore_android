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

package me.proton.core.accountmanager.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * @author Dino Kadrikj.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
        """
            CREATE TABLE IF NOT EXISTS `HumanVerificationDetailsEntity` (
            `sessionId` TEXT NOT NULL, 
            `verificationMethods` TEXT NOT NULL, 
            `captchaVerificationToken` TEXT NOT NULL, 
            `completed` INTEGER NOT NULL, 
            PRIMARY KEY(`sessionId`), 
            FOREIGN KEY(`sessionId`) REFERENCES `SessionEntity` (`sessionId`))
        """.trimIndent()
        )
        database.execSQL(
            """
                CREATE INDEX IF NOT EXISTS `index_HumanVerificationDetailsEntity_sessionId` ON `HumanVerificationDetailsEntity` (`sessionId`)
            """.trimIndent()
        )
    }
}

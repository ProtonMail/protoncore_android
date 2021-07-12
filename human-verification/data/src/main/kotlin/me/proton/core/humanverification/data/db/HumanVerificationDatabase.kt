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

package me.proton.core.humanverification.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.db.Database
import me.proton.core.data.db.migration.DatabaseMigration

interface HumanVerificationDatabase : Database {
    fun humanVerificationDetailsDao(): HumanVerificationDetailsDao

    companion object {
        /**
         * - Added Table HumanVerificationEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Added Table HumanVerificationEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `HumanVerificationEntity` (`clientId` TEXT NOT NULL, `clientIdType` TEXT NOT NULL, `verificationMethods` TEXT NOT NULL, `captchaVerificationToken` TEXT, `state` TEXT NOT NULL, `humanHeaderTokenType` TEXT, `humanHeaderTokenCode` TEXT, PRIMARY KEY(`clientId`))")
            }
        }
    }
}

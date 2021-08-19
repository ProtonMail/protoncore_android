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

package me.proton.core.usersettings.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.usersettings.data.db.dao.OrganizationDao
import me.proton.core.usersettings.data.db.dao.OrganizationKeysDao
import me.proton.core.usersettings.data.db.dao.UserSettingsDao

interface OrganizationDatabase : Database {
    fun organizationDao(): OrganizationDao
    fun organizationKeysDao(): OrganizationKeysDao

    companion object {
        /**
         * - Added Table OrganizationEntity.
         * - Added Table OrganizationKeysEntity.
         */
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Added Table OrganizationEntity.
                // Added Table OrganizationKeysEntity.
                database.execSQL("CREATE TABLE IF NOT EXISTS `OrganizationEntity` (`userId` TEXT NOT NULL, `name` TEXT, `displayName` TEXT, `planName` TEXT, `vpnPlanName` TEXT, `twoFactorGracePeriod` INTEGER, `theme` TEXT, `email` TEXT, `maxDomains` INTEGER, `maxAddresses` INTEGER, `maxSpace` INTEGER, `maxMembers` INTEGER, `maxVPN` INTEGER, `features` INTEGER, `flags` INTEGER, `usedDomains` INTEGER, `usedAddresses` INTEGER, `usedSpace` INTEGER, `assignedSpace` INTEGER, `usedMembers` INTEGER, `usedVPN` INTEGER, `hasKeys` INTEGER, `toMigrate` INTEGER, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE TABLE IF NOT EXISTS `OrganizationKeysEntity` (`userId` TEXT NOT NULL, `publicKey` TEXT NOT NULL, `privateKey` TEXT NOT NULL, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            }
        }
    }
}

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

package me.proton.core.featureflag.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.extension.recreateTable
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.featureflag.data.db.dao.FeatureFlagDao

public interface FeatureFlagDatabase : Database {
    public fun featureFlagDao(): FeatureFlagDao

    public companion object {
        /**
         * Add Table FeatureFlagEntity.
         */
        public val MIGRATION_0: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Added Table FeatureFlagEntity.
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `FeatureFlagEntity` " +
                        "(`userId` TEXT NOT NULL, " +
                        "`featureId` TEXT NOT NULL, " +
                        "`isGlobal` INTEGER NOT NULL, " +
                        "`defaultValue` INTEGER NOT NULL, " +
                        "`value` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`userId`, `featureId`), " +
                        "FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_FeatureFlagEntity_userId` ON `FeatureFlagEntity` (`userId`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS " +
                        "`index_FeatureFlagEntity_featureId` ON `FeatureFlagEntity` (`featureId`)"
                )
            }
        }

        public val MIGRATION_1: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.recreateTable(
                    table = "FeatureFlagEntity",
                    createTable = {
                        database.execSQL("CREATE TABLE IF NOT EXISTS `FeatureFlagEntity` (`userId` TEXT, `featureId` TEXT NOT NULL, `isGlobal` INTEGER NOT NULL, `defaultValue` INTEGER NOT NULL, `value` INTEGER NOT NULL, PRIMARY KEY(`featureId`))")
                    },
                    createIndices = {
                        database.execSQL("CREATE INDEX IF NOT EXISTS `index_FeatureFlagEntity_userId` ON `FeatureFlagEntity` (`userId`)")
                        database.execSQL("CREATE INDEX IF NOT EXISTS `index_FeatureFlagEntity_featureId` ON `FeatureFlagEntity` (`featureId`)")
                    },
                    columns = listOf("userId", "featureId", "isGlobal", "defaultValue", "value")
                )
            }
        }
    }
}

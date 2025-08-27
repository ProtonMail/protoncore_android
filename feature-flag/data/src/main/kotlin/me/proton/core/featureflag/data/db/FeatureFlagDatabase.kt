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
import me.proton.core.data.room.db.extension.addTableColumn
import me.proton.core.data.room.db.extension.dropTable
import me.proton.core.data.room.db.extension.recreateTable
import me.proton.core.data.room.db.migration.DatabaseMigration

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

        /**
         * Recreate FeatureFlagEntity table, discarding previous data.
         *
         * Note: UserId is now not null ("global" -> when userId is null).
         */
        public val MIGRATION_2: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.dropTable(table = "FeatureFlagEntity")
                database.execSQL("CREATE TABLE IF NOT EXISTS `FeatureFlagEntity` (`userId` TEXT NOT NULL, `featureId` TEXT NOT NULL, `isGlobal` INTEGER NOT NULL, `defaultValue` INTEGER NOT NULL, `value` INTEGER NOT NULL, PRIMARY KEY(`userId`, `featureId`))")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_FeatureFlagEntity_userId` ON `FeatureFlagEntity` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_FeatureFlagEntity_featureId` ON `FeatureFlagEntity` (`featureId`)")
            }
        }

        /**
         * Recreate FeatureFlagEntity table, discarding previous data.
         *
         * Added Scope property. Removed isGlobal.
         */
        public val MIGRATION_3: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.dropTable(table = "FeatureFlagEntity")
                database.execSQL("CREATE TABLE IF NOT EXISTS `FeatureFlagEntity` (`userId` TEXT NOT NULL, `featureId` TEXT NOT NULL, `scope` TEXT NOT NULL, `defaultValue` INTEGER NOT NULL, `value` INTEGER NOT NULL, PRIMARY KEY(`userId`, `featureId`))")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_FeatureFlagEntity_userId` ON `FeatureFlagEntity` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_FeatureFlagEntity_featureId` ON `FeatureFlagEntity` (`featureId`)")
            }
        }

        /**
         * Added variantName, payloadType and payloadValue columns, null by default.
         */
        public val MIGRATION_4: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.addTableColumn(
                    table = "FeatureFlagEntity",
                    column = "variantName",
                    type = "TEXT",
                    defaultValue = null,
                )
                database.addTableColumn(
                    table = "FeatureFlagEntity",
                    column = "payloadType",
                    type = "TEXT",
                    defaultValue = null,
                )
                database.addTableColumn(
                    table = "FeatureFlagEntity",
                    column = "payloadValue",
                    type = "TEXT",
                    defaultValue = null,
                )
            }
        }
    }
}

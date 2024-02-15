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

package me.proton.core.payment.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.payment.data.local.db.dao.GooglePurchaseDao
import me.proton.core.payment.data.local.db.dao.PurchaseDao

public interface PaymentDatabase : Database {
    public fun purchaseDao(): PurchaseDao
    public fun googlePurchaseDao(): GooglePurchaseDao

    public companion object {
        public val MIGRATION_0: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `GooglePurchaseEntity` (`googlePurchaseToken` TEXT NOT NULL, `paymentToken` TEXT NOT NULL, PRIMARY KEY(`googlePurchaseToken`))")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_GooglePurchaseEntity_paymentToken` ON `GooglePurchaseEntity` (`paymentToken`)")
            }
        }

        public val MIGRATION_1: DatabaseMigration = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `PurchaseEntity` (`sessionId` TEXT NOT NULL, `planName` TEXT NOT NULL, `planCycle` INTEGER NOT NULL, `purchaseState` TEXT NOT NULL, `purchaseFailure` TEXT, `paymentProvider` TEXT NOT NULL, `paymentOrderId` TEXT, `paymentToken` TEXT, `paymentCurrency` TEXT NOT NULL, `paymentAmount` INTEGER NOT NULL, PRIMARY KEY(`planName`), FOREIGN KEY(`sessionId`) REFERENCES `SessionEntity`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_PurchaseEntity_planName` ON `PurchaseEntity` (`planName`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_PurchaseEntity_sessionId` ON `PurchaseEntity` (`sessionId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_PurchaseEntity_purchaseState` ON `PurchaseEntity` (`purchaseState`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_PurchaseEntity_paymentProvider` ON `PurchaseEntity` (`paymentProvider`)")
            }
        }
    }
}

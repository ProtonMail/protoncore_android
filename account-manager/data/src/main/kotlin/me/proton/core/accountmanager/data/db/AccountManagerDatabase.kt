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

package me.proton.core.accountmanager.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.db.AccountConverters
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.account.data.entity.HumanVerificationDetailsEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.accountmanager.data.db.migration.MIGRATION_1_2
import me.proton.core.data.db.CommonConverters

@Database(
    entities = [
        // account-data
        AccountEntity::class,
        AccountMetadataEntity::class,
        SessionEntity::class,
        HumanVerificationDetailsEntity::class
    ],
    version = 2
)
@TypeConverters(
    CommonConverters::class,
    AccountConverters::class
)
abstract class AccountManagerDatabase :
    RoomDatabase(),
    AccountDatabase {

    companion object {
        fun buildDatabase(context: Context): AccountManagerDatabase {
            return Room
                .databaseBuilder(context, AccountManagerDatabase::class.java, "db-account-manager")
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = withTransaction(block)
}

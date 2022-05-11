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

package me.proton.core.push.data.testing

import androidx.room.Database
import androidx.room.TypeConverters
import kotlinx.coroutines.runBlocking
import me.proton.core.account.data.db.AccountConverters
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.account.data.entity.SessionDetailsEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.crypto.android.keystore.CryptoConverters
import me.proton.core.data.room.db.BaseDatabase
import me.proton.core.data.room.db.CommonConverters
import me.proton.core.push.data.local.db.PushConverters
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.push.data.local.db.PushEntity
import me.proton.core.user.data.db.UserConverters
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.user.data.entity.UserKeyEntity

@Database(
    entities = [
        AccountMetadataEntity::class, AccountEntity::class, PushEntity::class,
        SessionDetailsEntity::class, SessionEntity::class, UserEntity::class, UserKeyEntity::class
    ],
    exportSchema = false,
    version = 1
)
@TypeConverters(
    AccountConverters::class,
    CommonConverters::class,
    CryptoConverters::class,
    PushConverters::class,
    UserConverters::class
)
internal abstract class TestDatabase : BaseDatabase(), AccountDatabase, PushDatabase, UserDatabase

@Suppress("BlockingMethodInNonBlockingContext")
internal fun TestDatabase.prepare(): TestDatabase {
    clearAllTables()
    runBlocking {
        accountDao().insertOrUpdate(testAccountEntity(testUserId))
        userDao().insertOrUpdate(testUserEntity(testUserId))
    }
    return this
}

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

package me.proton.core.user.data.repository

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.extension.toEntity
import me.proton.core.user.data.extension.toEntityList
import me.proton.core.user.data.extension.toUser
import me.proton.core.user.data.extension.toUserKeyList
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserRepository

class UserRepositoryImpl(
    private val db: UserDatabase,
    private val provider: ApiProvider
) : UserRepository, PassphraseRepository {

    private val userDao = db.userDao()
    private val userKeyDao = db.userKeyDao()
    private val userWithKeysDao = db.userWithKeysDao()

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { userId: UserId ->
            provider.get<UserApi>(userId).invoke {
                getUsers().user.toUser(getPassphrase(userId))
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { userId -> getUserLocal(userId) },
            writer = { _, input -> insertOrUpdate(input) },
            delete = { userId -> delete(userId) },
            deleteAll = { deleteAll() }
        )
    ).build()

    private fun getUserLocal(userId: UserId): Flow<User?> = userWithKeysDao.findByUserId(userId)
        .map { user ->
            val userKeyList = user?.keys?.toUserKeyList(getPassphrase(userId)).orEmpty()
            user?.entity?.toUser(userKeyList)
        }

    private suspend fun insertOrUpdate(user: User) =
        db.inTransaction {
            // Get current passphrase -> don't overwrite passphrase.
            val passphrase = userDao.getPassphrase(user.userId)
            userDao.insertOrUpdate(user.toEntity(passphrase))
            userKeyDao.insertOrUpdate(*user.keys.toEntityList().toTypedArray())
        }

    private suspend fun delete(userId: UserId) =
        userDao.delete(userId)

    private suspend fun deleteAll() =
        userDao.deleteAll()

    override fun getUserFlow(sessionUserId: SessionUserId, refresh: Boolean): Flow<DataResult<User>> =
        store.stream(StoreRequest.cached(sessionUserId, refresh = refresh)).map { it.toDataResult() }

    override suspend fun getUser(sessionUserId: SessionUserId, refresh: Boolean): User =
        if (refresh) store.fresh(sessionUserId) else store.get(sessionUserId)

    // region PassphraseRepository

    override suspend fun setPassphrase(userId: UserId, passphrase: EncryptedByteArray) =
        db.inTransaction {
            requireNotNull(userDao.getByUserId(userId)) { "Cannot set passphrase, User doesn't exist in DB." }
            userDao.setPassphrase(userId, passphrase)
        }

    override suspend fun getPassphrase(userId: UserId): EncryptedByteArray? =
        userDao.getPassphrase(userId)

    override suspend fun clearPassphrase(userId: UserId) =
        userDao.setPassphrase(userId, null)

    //endregion
}

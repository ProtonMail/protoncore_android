/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.user.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.updateIsActive
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.extension.toEntity
import me.proton.core.user.data.extension.toEntityList
import me.proton.core.user.data.extension.toUser
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.repository.UserLocalDataSource
import javax.inject.Inject

class UserLocalDataSourceImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val db: UserDatabase,
) : UserLocalDataSource {
    private val userDao = db.userDao()
    private val userKeyDao = db.userKeyDao()
    private val userWithKeysDao = db.userWithKeysDao()

    override suspend fun getPassphrase(
        userId: UserId
    ): EncryptedByteArray? {
        return userDao.getPassphrase(userId)
    }

    override suspend fun setPassphrase(
        userId: UserId,
        passphrase: EncryptedByteArray?,
        onSuccess: suspend () -> Unit
    ) {
        return db.inTransaction {
            if (passphrase != getPassphrase(userId)) {
                userDao.setPassphrase(userId, passphrase)
                upsert(requireNotNull(getUser(userId)))
                onSuccess()
            }
        }
    }

    override suspend fun isCredentialLess(userId: UserId): Boolean =
        userDao.getByUserId(userId)?.type == Type.CredentialLess.value

    override suspend fun getCredentialLessUser(userId: UserId): User? =
        userDao.getByUserId(userId)?.takeIf { it.type == Type.CredentialLess.value }?.toUser(keys = emptyList())

    override suspend fun getUser(userId: UserId): User? = userWithKeysDao.getByUserId(userId)?.toUser()

    override fun observe(userId: UserId): Flow<User?> =
        userWithKeysDao.observeByUserId(userId).map { user -> user?.toUser() }

    override suspend fun upsert(user: User, onSuccess: (suspend () -> Unit)?) {
        db.inTransaction {
            // Get current passphrase -> don't overwrite passphrase.
            val passphrase = userDao.getPassphrase(user.userId)
            // Update isActive and passphrase.
            val userKeys = user.keys.updateIsActive(passphrase)
            // Insert in Database.
            userDao.insertOrUpdate(user.toEntity(passphrase))
            userKeyDao.deleteAllByUserId(user.userId)
            userKeyDao.insertOrUpdate(*userKeys.toEntityList().toTypedArray())
            onSuccess?.invoke()
        }
    }

    override suspend fun updateUserUsedSpace(userId: UserId, usedSpace: Long) {
        userDao.setUsedSpace(userId, usedSpace)
    }

    override suspend fun updateUserUsedBaseSpace(userId: UserId, usedBaseSpace: Long) {
        userDao.setUsedBaseSpace(userId, usedBaseSpace)
    }

    override suspend fun updateUserUsedDriveSpace(userId: UserId, usedDriveSpace: Long) {
        userDao.setUsedDriveSpace(userId, usedDriveSpace)
    }

    private fun List<UserKey>.updateIsActive(passphrase: EncryptedByteArray?): List<UserKey> =
        map { key -> key.copy(privateKey = key.privateKey.updateIsActive(cryptoContext, passphrase)) }
}

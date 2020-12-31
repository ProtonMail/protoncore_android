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

package me.proton.core.user.data

import kotlinx.coroutines.flow.Flow
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.simple.PlainByteArray
import me.proton.core.crypto.common.simple.EncryptedByteArray
import me.proton.core.crypto.common.simple.encrypt
import me.proton.core.crypto.common.simple.use
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.canUnlock
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.extension.primary
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.user.domain.UnlockResult
import me.proton.core.user.domain.UnlockResult.Error
import me.proton.core.user.domain.UnlockResult.Success
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository

class UserManagerImpl(
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val passphraseRepository: PassphraseRepository,
    private val keySaltRepository: KeySaltRepository,
    private val cryptoContext: CryptoContext
) : UserManager {

    override fun getUser(sessionUserId: SessionUserId, refresh: Boolean): Flow<DataResult<User?>> =
        userRepository.getUser(sessionUserId, refresh = refresh)

    override fun getAddresses(sessionUserId: SessionUserId, refresh: Boolean): Flow<DataResult<List<UserAddress>>> =
        userAddressRepository.getAddresses(sessionUserId, refresh = refresh)

    override suspend fun unlockWithPassword(
        userId: UserId,
        password: PlainByteArray,
        refresh: Boolean
    ): UnlockResult {
        val user = userRepository.getUserBlocking(userId, refresh = refresh)
        val userPrimaryKey = user.keys.primary() ?: return Error.NoPrimaryKey

        val salts = keySaltRepository.getKeySalts(userId)
        val primaryKeySalt = salts.find { it.keyId == userPrimaryKey.keyId }?.takeIf { !it.keySalt.isNullOrEmpty() }
        if (primaryKeySalt?.keySalt == null) {
            return Error.NoKeySaltsForPrimaryKey
        }

        val encryptedPassphrase = cryptoContext.pgpCrypto.getPassphrase(password.array, primaryKeySalt.keySalt!!).use {
            it.encrypt(cryptoContext.simpleCrypto)
        }

        return unlockWithPassphrase(userId, encryptedPassphrase, refresh = false)
    }

    override suspend fun unlockWithPassphrase(
        userId: UserId,
        passphrase: EncryptedByteArray,
        refresh: Boolean
    ): UnlockResult {
        val user = userRepository.getUserBlocking(userId, refresh = refresh)
        val userPrimaryKey = user.keys.primary() ?: return Error.NoPrimaryKey

        val updatedPrimaryKey = PrivateKey(
            userPrimaryKey.privateKey.key,
            userPrimaryKey.privateKey.isPrimary,
            passphrase
        )

        if (!updatedPrimaryKey.canUnlock(cryptoContext)) {
            return Error.PrimaryKeyInvalidPassphrase
        }

        userRepository.createOrUpdateUserLocal(user)
        passphraseRepository.setPassphrase(userId, passphrase)
        return Success
    }

    override suspend fun lock(userId: UserId) {
        passphraseRepository.clearPassphrase(userId)
    }

    override suspend fun changePassword(userId: UserId, oldPassword: String, newPassword: String) {
        TODO("Not yet implemented")
        // oldPassword: Check if valid.
        // oldPassphrase: Get from DB.
        // passphrase = generatePassphrase(password, keySalt).
        // isOldValid = passphraseCanUnlockKey(privateKey, passphrase).
        // newPassphrase: generate.
        keySaltRepository.clear(userId)
    }
}

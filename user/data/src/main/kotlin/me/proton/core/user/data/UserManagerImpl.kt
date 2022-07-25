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
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.canUnlock
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.extension.primary
import me.proton.core.key.domain.extension.updatePrivateKeyPassphrase
import me.proton.core.key.domain.extension.updatePrivateKeyPassphraseOrNull
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.UserManager.UnlockResult
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.hasMigratedKey
import me.proton.core.user.domain.extension.hasNonMigratedKey
import me.proton.core.user.domain.extension.isOrganizationAdmin
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.user.domain.signKeyList

class UserManagerImpl(
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val passphraseRepository: PassphraseRepository,
    private val keySaltRepository: KeySaltRepository,
    private val privateKeyRepository: PrivateKeyRepository,
    private val userAddressKeySecretProvider: UserAddressKeySecretProvider,
    private val cryptoContext: CryptoContext
) : UserManager {

    private val keyStore = cryptoContext.keyStoreCrypto
    private val pgp = cryptoContext.pgpCrypto

    override suspend fun addUser(user: User, addresses: List<UserAddress>) {
        userRepository.addUser(user)
        userAddressRepository.addAddresses(addresses)
    }

    override fun observeUser(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<User?> = userRepository.observeUser(sessionUserId, refresh = refresh)

    override fun getUserFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<User?>> = userRepository.getUserFlow(sessionUserId, refresh = refresh)

    override suspend fun getUser(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): User = userRepository.getUser(sessionUserId, refresh = refresh)

    override fun observeAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<List<UserAddress>> = userAddressRepository.observeAddresses(sessionUserId, refresh = refresh)

    override fun getAddressesFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<List<UserAddress>>> = userAddressRepository.getAddressesFlow(sessionUserId, refresh = refresh)

    override suspend fun getAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): List<UserAddress> = userAddressRepository.getAddresses(sessionUserId, refresh = refresh)

    override suspend fun unlockWithPassword(
        userId: UserId,
        password: PlainByteArray
    ): UnlockResult {
        val user = userRepository.getUser(userId)
        val userPrimaryKey = user.keys.primary()
            ?: return UnlockResult.Error.NoPrimaryKey

        val salts = keySaltRepository.getKeySalts(userId)
        val primaryKeySalt = salts.find { it.keyId == userPrimaryKey.keyId }?.keySalt?.takeIf { it.isNotEmpty() }
            ?: return UnlockResult.Error.NoKeySaltsForPrimaryKey

        val passphrase = pgp.getPassphrase(password.array, primaryKeySalt).use {
            it.encrypt(keyStore)
        }
        return unlockWithPassphrase(userId, passphrase)
    }

    override suspend fun unlockWithPassphrase(
        userId: UserId,
        passphrase: EncryptedByteArray
    ): UnlockResult {
        val user = userRepository.getUser(userId)
        val userPrimaryKey = user.keys.primary()
            ?: return UnlockResult.Error.NoPrimaryKey

        val key = userPrimaryKey.privateKey.copy(passphrase = passphrase)
        if (!key.canUnlock(cryptoContext)) {
            return UnlockResult.Error.PrimaryKeyInvalidPassphrase
        }

        passphraseRepository.setPassphrase(userId, passphrase)
        return UnlockResult.Success
    }

    override suspend fun lock(userId: UserId) {
        passphraseRepository.clearPassphrase(userId)
    }

    override suspend fun changePassword(
        userId: UserId,
        newPassword: EncryptedString,
        secondFactorCode: String,
        proofs: SrpProofs,
        srpSession: String,
        auth: Auth?,
        orgPrivateKey: Armored?
    ): Boolean {
        newPassword.decrypt(keyStore).toByteArray().use { decryptedNewPassword ->
            val keySalt = pgp.generateNewKeySalt()
            pgp.getPassphrase(decryptedNewPassword.array, keySalt).use { newPassphrase ->
                val addresses = userAddressRepository.getAddresses(userId, refresh = true)
                val user = userRepository.getUser(userId, refresh = true)
                val isUserMigrated = addresses.hasMigratedKey()

                // For migrated accounts, update only the user keys.
                val updatedUserKeys = user.keys.takeIf { isUserMigrated }
                    ?.mapNotNull { it.updatePrivateKeyPassphraseOrNull(cryptoContext, newPassphrase.array) }

                // For non-migrated accounts, update the user keys and addresses keys.
                val updatedKeys = user.keys.takeUnless { isUserMigrated }?.plus(addresses.flatMap { it.keys })
                    ?.mapNotNull { it.updatePrivateKeyPassphraseOrNull(cryptoContext, newPassphrase.array) }

                // Update organization key if provided.
                val updatedOrgPrivateKey = orgPrivateKey?.takeIf { user.isOrganizationAdmin() }?.let { key ->
                    requireNotNull(passphraseRepository.getPassphrase(userId)).decrypt(keyStore).use {
                        key.updatePrivateKeyPassphrase(cryptoContext, it.array, newPassphrase.array)
                    }
                }

                // Update all needed keys providing any authentication proof.
                val result = privateKeyRepository.updatePrivateKeys(
                    sessionUserId = userId,
                    keySalt = keySalt,
                    srpProofs = proofs,
                    srpSession = srpSession,
                    secondFactorCode = secondFactorCode,
                    auth = auth,
                    keys = updatedKeys,
                    userKeys = updatedUserKeys,
                    organizationKey = updatedOrgPrivateKey
                )

                // Lock, refresh and unlock.
                lock(userId)

                // Refresh User and Addresses.
                userAddressRepository.getAddresses(userId, refresh = true)
                userRepository.getUser(userId, refresh = true)

                // Unlock.
                unlockWithPassphrase(userId, newPassphrase.encrypt(keyStore))
                return result
            }
        }
    }

    override suspend fun setupPrimaryKeys(
        sessionUserId: SessionUserId,
        username: String,
        domain: String,
        auth: Auth,
        password: ByteArray
    ): User {
        val primaryKeySalt = pgp.generateNewKeySalt()
        pgp.getPassphrase(password, primaryKeySalt).use { passphrase ->
            // Generate a new PrivateKey for User.
            val privateKey = pgp.generateNewPrivateKey(
                username = username,
                domain = domain,
                passphrase = passphrase.array
            )
            val encryptedPassphrase = passphrase.encrypt(keyStore)
            val userPrivateKey = PrivateKey(
                key = privateKey,
                isPrimary = true,
                isActive = true,
                passphrase = encryptedPassphrase
            )

            // Find all missing UserAddress Keys.
            val userAddresses = userAddressRepository.getAddresses(sessionUserId, refresh = true)
            val userAddressesWithoutKeys = userAddresses.filter { it.keys.isEmpty() }

            // If User have at least one non-migrated UserAddressKey (old key format), let's use the legacy format.
            val generateOldAddressKeyFormat = userAddresses.hasNonMigratedKey()

            // Generate new address keys.
            val newAddressKeys = userAddressesWithoutKeys.map { address ->
                userAddressKeySecretProvider.generateUserAddressKey(
                    generateOldFormat = generateOldAddressKeyFormat,
                    userAddress = address,
                    userPrivateKey = userPrivateKey,
                    isPrimary = true
                ).let { key ->
                    val userAddressWithKeys = address.copy(keys = address.keys.plus(key))
                    PrivateAddressKey(
                        addressId = address.addressId.id,
                        privateKey = key.privateKey,
                        token = key.token,
                        signature = key.signature,
                        signedKeyList = userAddressWithKeys.signKeyList(cryptoContext)
                    )
                }
            }

            // Setup initial primary UserKey and UserAddressKey, remotely.
            privateKeyRepository.setupInitialKeys(
                sessionUserId = sessionUserId,
                primaryKey = privateKey,
                primaryKeySalt = primaryKeySalt,
                addressKeys = newAddressKeys,
                auth = auth
            )

            // We know we can unlock the key with this passphrase as we just generated from it.
            passphraseRepository.setPassphrase(sessionUserId, encryptedPassphrase)

            // Refresh User and Addresses.
            userAddressRepository.getAddresses(sessionUserId, refresh = true)
            return checkNotNull(userRepository.getUser(sessionUserId, refresh = true))
        }
    }
}

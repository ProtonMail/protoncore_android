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

import com.google.crypto.tink.subtle.Base64
import kotlinx.coroutines.flow.Flow
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.decryptWith
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.canUnlock
import me.proton.core.key.domain.entity.key.Key
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey
import me.proton.core.key.domain.extension.primary
import me.proton.core.key.domain.extension.updateOrganizationPrivateKey
import me.proton.core.key.domain.extension.updatePrivateKey
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.key.domain.signedKeyList
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.UserManager.UnlockResult
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.hasMigratedKey
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository

class UserManagerImpl(
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val passphraseRepository: PassphraseRepository,
    private val keySaltRepository: KeySaltRepository,
    private val privateKeyRepository: PrivateKeyRepository,
    private val userAddressKeySecretProvider: UserAddressKeySecretProvider,
    private val cryptoContext: CryptoContext,
    private val keyStoreCrypto: KeyStoreCrypto
) : UserManager {

    override suspend fun addUser(user: User, addresses: List<UserAddress>) {
        userRepository.addUser(user)
        userAddressRepository.addAddresses(addresses)
    }

    override fun getUserFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<User?>> = userRepository.getUserFlow(sessionUserId, refresh = refresh)

    override suspend fun getUser(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): User = userRepository.getUser(sessionUserId, refresh = refresh)

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
        password: PlainByteArray,
        refresh: Boolean
    ): UnlockResult {
        val user = userRepository.getUser(userId)
        val userPrimaryKey = user.keys.primary()
            ?: return UnlockResult.Error.NoPrimaryKey

        val salts = keySaltRepository.getKeySalts(userId)
        val primaryKeySalt = salts.find { it.keyId == userPrimaryKey.keyId }?.keySalt?.takeIf { it.isNotEmpty() }
            ?: return UnlockResult.Error.NoKeySaltsForPrimaryKey

        val passphrase = cryptoContext.pgpCrypto.getPassphrase(password.array, primaryKeySalt).use {
            it.encryptWith(cryptoContext.keyStoreCrypto)
        }
        return unlockWithPassphrase(userId, passphrase, refresh = false)
    }

    override suspend fun unlockWithPassphrase(
        userId: UserId,
        passphrase: EncryptedByteArray,
        refresh: Boolean
    ): UnlockResult {
        val user = userRepository.getUser(userId, refresh = refresh)
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
        loginPassword: String,
        newPassword: String,
        secondFactorCode: String,
        clientProofs: SrpProofs,
        srpSession: String,
        auth: Auth?,
        organizationPrivateKey: String?
    ): Boolean {
        val user = userRepository.getUser(userId)
        val keySalt = cryptoContext.pgpCrypto.generateNewKeySalt()

        loginPassword.decryptWith(keyStoreCrypto).toByteArray().use { decryptedLoginPassword ->
            newPassword.decryptWith(keyStoreCrypto).toByteArray().use { decryptedNewPassphrase ->

                cryptoContext.pgpCrypto.getPassphrase(decryptedNewPassphrase.array, keySalt).use { newPassphrase ->
                    val addresses = userAddressRepository.getAddresses(userId)

                    var keys: MutableList<Key>? = null
                    var userKeys: MutableList<Key>? = null

                    if (addresses.hasMigratedKey()) {
                        // for migrated accounts, we update only the user keys
                        userKeys = mutableListOf()
                        for (userKey in user.keys) {
                            val updatedKey = userKey.update(
                                newPassphrase = newPassphrase.array
                            )
                            updatedKey?.let { userKeys.add(it) }
                        }
                    } else {
                        // for non-migrated accounts all keys (user + address) go into keys field
                        keys = mutableListOf()
                        for (userKey in user.keys) {
                            val updatedKey = userKey.update(newPassphrase.array)
                            updatedKey?.let { keys.add(it) }
                        }
                        for (address in addresses) {
                            for (addressKey in address.keys) {
                                val updatedAddressKey = addressKey.update(newPassphrase.array)
                                updatedAddressKey?.let { keys.add(it) }
                            }
                        }
                    }

                    val orgPrivateKey = if (organizationPrivateKey != null && organizationPrivateKey.isNotEmpty()) {
                        val currentPassphrase =
                            requireNotNull(passphraseRepository.getPassphrase(userId)?.decryptWith(keyStoreCrypto))
                        organizationPrivateKey.updateOrganizationPrivateKey(
                            cryptoContext = cryptoContext,
                            currentPassphrase = currentPassphrase.array,
                            newPassphrase = newPassphrase.array
                        ) ?: ""
                    } else ""

                    val result = privateKeyRepository.updatePrivateKeys(
                        sessionUserId = userId,
                        keySalt = keySalt,
                        clientEphemeral = Base64.encode(clientProofs.clientEphemeral),
                        clientProof = Base64.encode(clientProofs.clientProof),
                        srpSession = srpSession,
                        secondFactorCode = secondFactorCode,
                        auth = auth,
                        keys = keys,
                        userKeys = userKeys,
                        organizationKey = orgPrivateKey
                    )

                    userAddressRepository.getAddresses(userId, refresh = true)
                    userRepository.getUser(userId, refresh = true)
                    passphraseRepository.setPassphrase(userId, newPassphrase.encryptWith(keyStoreCrypto))
                    return result
                }
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
        val primaryKeySalt = cryptoContext.pgpCrypto.generateNewKeySalt()
        cryptoContext.pgpCrypto.getPassphrase(password, primaryKeySalt).use { passphrase ->
            // Generate a new PrivateKey for User.
            val privateKey = cryptoContext.pgpCrypto.generateNewPrivateKey(
                username = username,
                domain = domain,
                passphrase = passphrase.array
            )
            val encryptedPassphrase = passphrase.encryptWith(cryptoContext.keyStoreCrypto)
            val userPrivateKey = PrivateKey(
                key = privateKey,
                isPrimary = true,
                passphrase = encryptedPassphrase
            )

            // Find all missing UserAddress Keys.
            val userAddresses = userAddressRepository.getAddresses(sessionUserId, refresh = true)
            val userAddressesWithoutKeys = userAddresses.filter { it.keys.isEmpty() }

            // If User have at least one migrated UserAddressKey (new key format), let's continue like this.
            val generateOldAddressKeyFormat = !userAddresses.hasMigratedKey()

            // Generate new PrivateAddressKeys.
            val privateAddressKeys = userAddressesWithoutKeys.map { address ->
                userAddressKeySecretProvider.generateUserAddressKey(
                    generateOldFormat = generateOldAddressKeyFormat,
                    userAddress = address,
                    userPrivateKey = userPrivateKey,
                    isPrimary = true
                ).let { key ->
                    PrivateAddressKey(
                        addressId = address.addressId.id,
                        privateKey = key.privateKey,
                        token = key.token,
                        signature = key.signature,
                        signedKeyList = key.privateKey.signedKeyList(cryptoContext)
                    )
                }
            }

            // Setup initial primary UserKey and UserAddressKey, remotely.
            privateKeyRepository.setupInitialKeys(
                sessionUserId = sessionUserId,
                primaryKey = privateKey,
                primaryKeySalt = primaryKeySalt,
                addressKeys = privateAddressKeys,
                auth = auth
            )

            // We know we can unlock the key with this passphrase as we just generated from it.
            passphraseRepository.setPassphrase(sessionUserId, encryptedPassphrase)

            // Refresh User and Addresses.
            userAddressRepository.getAddresses(sessionUserId, refresh = true)
            return checkNotNull(userRepository.getUser(sessionUserId, refresh = true))
        }
    }

    private fun KeyHolderPrivateKey.update(newPassphrase: ByteArray) =
        updatePrivateKey(
            keyStoreCrypto = keyStoreCrypto,
            cryptoContext = cryptoContext,
            newPassphrase = newPassphrase
        )
}

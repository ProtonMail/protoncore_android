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
import me.proton.core.accountrecovery.domain.repository.AccountRecoveryRepository
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.canUnlock
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.extension.keyHolder
import me.proton.core.key.domain.extension.primary
import me.proton.core.key.domain.extension.updatePrivateKeyPassphraseOrNull
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.key.domain.useKeysAs
import me.proton.core.user.data.usecase.GenerateSignedKeyList
import me.proton.core.user.domain.SignedKeyListChangeListener
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.UserManager.UnlockResult
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.extension.generateNewKeyFormat
import me.proton.core.user.domain.extension.hasMigratedKey
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManagerImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val passphraseRepository: PassphraseRepository,
    private val keySaltRepository: KeySaltRepository,
    private val privateKeyRepository: PrivateKeyRepository,
    private val accountRecoveryRepository: AccountRecoveryRepository,
    private val userAddressKeySecretProvider: UserAddressKeySecretProvider,
    private val cryptoContext: CryptoContext,
    private val generateSignedKeyList: GenerateSignedKeyList,
    private val signedKeyListChangeListener: Optional<SignedKeyListChangeListener>
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

    override suspend fun getUser(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): User = userRepository.getUser(sessionUserId, refresh = refresh)

    override fun observeAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<List<UserAddress>> = userAddressRepository.observeAddresses(sessionUserId, refresh = refresh)

    override suspend fun getAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): List<UserAddress> = userAddressRepository.getAddresses(sessionUserId, refresh = refresh)

    override suspend fun unlockWithPassword(
        userId: UserId,
        password: PlainByteArray,
        refreshKeySalts: Boolean
    ): UnlockResult {
        val user = userRepository.getUser(userId)
        val userPrimaryKey = user.keys.primary()
            ?: return UnlockResult.Error.NoPrimaryKey

        val salts = keySaltRepository.getKeySalts(userId, refreshKeySalts)
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
        secondFactorProof: SecondFactorProof?,
        proofs: SrpProofs,
        srpSession: String,
        auth: Auth?
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

                // Update all needed keys providing any authentication proof.
                val result = privateKeyRepository.updatePrivateKeys(
                    sessionUserId = userId,
                    keySalt = keySalt,
                    srpProofs = proofs,
                    srpSession = srpSession,
                    secondFactorProof = secondFactorProof,
                    auth = auth,
                    keys = updatedKeys,
                    userKeys = updatedUserKeys
                )

                lock(userId)
                userAddressRepository.getAddresses(userId, refresh = true)
                userRepository.getUser(userId, refresh = true)
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

            // Generate new address keys.
            val newKeys = userAddressesWithoutKeys.map { address ->
                userAddressKeySecretProvider.generateUserAddressKey(
                    generateNewKeyFormat = userAddresses.generateNewKeyFormat(),
                    userAddress = address,
                    userPrivateKey = userPrivateKey,
                    isPrimary = true
                )
            }

            val userAddressesWithNewKeys = userAddressesWithoutKeys.zip(newKeys).map { (address, newKey) ->
                address.copy(keys = listOf(newKey))
            }

            // Generate new SKL
            val newSKLs = userAddressesWithNewKeys.map { generateSignedKeyList(it) }

            // Check key transparency state
            userAddressesWithNewKeys.map { userAddressWithNewKeys ->
                if (signedKeyListChangeListener.isPresent) {
                    // Key transparency needs to be checked before generating SKLs
                    signedKeyListChangeListener.get().onSKLChangeRequested(sessionUserId, userAddressWithNewKeys)
                }
            }

            // Make the key objects to submit to the API
            val newAddressKeys = userAddressesWithNewKeys.mapIndexed { index, address ->
                val skl = newSKLs[index]
                val key = newKeys[index]
                PrivateAddressKey(
                    addressId = address.addressId.id,
                    privateKey = key.privateKey,
                    token = key.token,
                    signature = key.signature,
                    signedKeyList = skl
                )
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
            val user = checkNotNull(userRepository.getUser(sessionUserId, refresh = true))
            val updatedAddresses = userAddressRepository.getAddresses(sessionUserId, refresh = true)

            if (signedKeyListChangeListener.isPresent) {
                // Key transparency needs to be checked later for new keys
                userAddressesWithNewKeys.forEachIndexed { index, address ->
                    val updatedUserAddress = checkNotNull(updatedAddresses.find { it.addressId == address.addressId })
                    val skl = newSKLs[index]
                    signedKeyListChangeListener.get().onSKLChangeAccepted(sessionUserId, updatedUserAddress, skl)
                }
            }

            return user
        }
    }

    override suspend fun reactivateKey(
        userKey: UserKey
    ): User {
        val userId = userKey.userId

        // Refresh addresses first.
        val addresses = userAddressRepository.getAddresses(userId, refresh = true)

        // Find disabled address keys that can be decrypted with the user key we are reactivating.
        val decryptedAddressKeys = userKey.keyHolder().useKeysAs(cryptoContext) { context ->
            addresses
                .flatMap { it.keys }
                .filter { !it.active }
                .filter { it.token != null && it.signature != null }
                .filter { key ->
                    val decryptedPassphrase = userAddressKeySecretProvider.getPassphrase(
                        userId = userId,
                        userContext = context,
                        key = key.copy(active = true)
                    )
                    decryptedPassphrase != null
                }
        }

        // Get fingerprint for the key(s) we want to reactivate.
        val decryptedAddressKeysFingerprints =
            decryptedAddressKeys.map { pgp.getFingerprint(it.privateKey.key) }

        // Generate the signed key list (desired active keys).
        val signedKeyLists = decryptedAddressKeys.groupBy { it.addressId }.map {
            val userAddress = userAddressRepository.getAddress(userId, it.key)
            it.key.id to generateSignedKeyList(requireNotNull(userAddress)) { key ->
                // all current active + reactivating
                key.active || key in decryptedAddressKeys
            }
        }.toMap()

        privateKeyRepository.reactivatePrivateKey(
            sessionUserId = userId,
            privateKeyId = userKey.keyId.id,
            privateKey = userKey.privateKey,
            addressKeysFingerprints = decryptedAddressKeysFingerprints,
            signedKeyLists = signedKeyLists
        )

        val user = userRepository.getUser(userId, refresh = true)
        userAddressRepository.getAddresses(userId, refresh = true)
        return user
    }

    override suspend fun resetPassword(
        sessionUserId: SessionUserId,
        newPassword: EncryptedString,
        auth: Auth?
    ): Boolean {
        newPassword.decrypt(keyStore).toByteArray().use { decryptedNewPassword ->
            val keySalt = pgp.generateNewKeySalt()

            pgp.getPassphrase(decryptedNewPassword.array, keySalt).use { newPassphrase ->
                val addresses = userAddressRepository.getAddresses(sessionUserId, refresh = true)
                val user = userRepository.getUser(sessionUserId, refresh = true)
                val isUserMigrated = addresses.hasMigratedKey()
                val updatedUserKeys = user.keys.takeIf { isUserMigrated }
                    ?.mapNotNull { it.updatePrivateKeyPassphraseOrNull(cryptoContext, newPassphrase.array) }

                val result = accountRecoveryRepository.resetPassword(
                    sessionUserId = sessionUserId,
                    auth = auth,
                    keySalt = keySalt,
                    userKeys = updatedUserKeys
                )

                passphraseRepository.setPassphrase(sessionUserId, newPassphrase.encrypt(keyStore))
                userRepository.getUser(sessionUserId, refresh = true)
                userAddressRepository.getAddresses(sessionUserId, refresh = true)
                return result
            }
        }
    }
}

/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.usersettings.domain.usecase

import com.google.crypto.tink.subtle.Base64
import me.proton.core.auth.domain.ClientSecret
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decryptWith
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.key.domain.entity.key.Key
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.hasMigratedKey
import me.proton.core.user.domain.extension.hasSubscription
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import javax.inject.Inject

class PerformUpdateMailboxPassword @Inject constructor(
    private val authRepository: AuthRepository,
    private val userAddressRepository: UserAddressRepository,
    private val organizationRepository: OrganizationRepository,
    private val passphraseRepository: PassphraseRepository,
    private val keyRepository: PrivateKeyRepository,
    private val srpCrypto: SrpCrypto,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val cryptoContext: CryptoContext,
    @ClientSecret private val clientSecret: String
) {
    suspend operator fun invoke(
        twoPasswordMode: Boolean,
        user: User,
        loginPassword: EncryptedString,
        newPassword: EncryptedString,
        secondFactorCode: String = ""
    ): Boolean {
        val username = requireNotNull(user.name ?: user.email)
        val userId = user.userId
        val paid = user.hasSubscription()

        val loginInfo = authRepository.getLoginInfo(
            username = username,
            clientSecret = clientSecret
        )
        val modulus = authRepository.randomModulus()

        val organizationKeys = if (paid) {
            organizationRepository.getOrganizationKeys(userId)
        } else null

        val keySalt = cryptoContext.pgpCrypto.generateNewKeySalt()

        loginPassword.decryptWith(keyStoreCrypto).toByteArray().use { decryptedLoginPassword ->
            newPassword.decryptWith(keyStoreCrypto).toByteArray().use { decryptedNewPassphrase ->
                val clientProofs: SrpProofs = srpCrypto.generateSrpProofs(
                    username = username,
                    password = decryptedLoginPassword.array,
                    version = loginInfo.version.toLong(),
                    salt = loginInfo.salt,
                    modulus = loginInfo.modulus,
                    serverEphemeral = loginInfo.serverEphemeral
                )

                val auth = if (!twoPasswordMode) srpCrypto.calculatePasswordVerifier(
                    username = username,
                    password = decryptedNewPassphrase.array,
                    modulusId = modulus.modulusId,
                    modulus = modulus.modulus
                ) else null

                var keys: MutableList<Key>? = null
                var userKeys: MutableList<Key>? = null

                cryptoContext.pgpCrypto.getPassphrase(decryptedNewPassphrase.array, keySalt).use { newPassphrase ->
                    val addresses = userAddressRepository.getAddresses(userId)

                    if (addresses.hasMigratedKey()) {
                        // for migrated accounts, we update only the user keys
                        userKeys = mutableListOf()
                        for (userKey in user.keys) {
                            val updatedKey = userKey.updatePrivateKey(newPassphrase.array)
                            updatedKey?.let { userKeys!!.add(it) }
                        }
                    } else {
                        // for non-migrated accounts all keys (user + address) go into keys field
                        keys = mutableListOf()
                        for (userKey in user.keys) {
                            val updatedKey = userKey.updatePrivateKey(newPassphrase.array)
                            updatedKey?.let { keys!!.add(it) }
                        }
                        for (address in addresses) {
                            for (addressKey in address.keys) {
                                val updatedAddressKey = addressKey.updatePrivateKey(newPassphrase.array)
                                updatedAddressKey?.let { keys!!.add(it) }
                            }
                        }
                    }

                    val orgPrivateKey = if (organizationKeys != null && organizationKeys.privateKey.isNotEmpty()) {
                        val currentPassphrase =
                            requireNotNull(passphraseRepository.getPassphrase(userId)?.decryptWith(keyStoreCrypto))
                        organizationKeys.privateKey.updateOrganizationPrivateKey(currentPassphrase.array, newPassphrase.array)
                            ?: ""
                    } else ""

                    return keyRepository.updateKeysForPasswordChange(
                        sessionUserId = userId,
                        keySalt = keySalt,
                        clientEphemeral = Base64.encode(clientProofs.clientEphemeral),
                        clientProof = Base64.encode(clientProofs.clientProof),
                        srpSession = loginInfo.srpSession,
                        secondFactorCode = secondFactorCode,
                        auth = auth,
                        keys = keys,
                        userKeys = userKeys,
                        organizationKey = orgPrivateKey
                    )
                }
            }
        }
    }

    private fun KeyHolderPrivateKey.updatePrivateKey(newPassphrase: ByteArray): Key? {
        val passphrase = privateKey.passphrase?.decryptWith(keyStoreCrypto)?.array ?: return null
        val armored = cryptoContext.pgpCrypto.updatePrivateKeyPassphrase(
            privateKey = privateKey.key,
            oldPassphrase = passphrase,
            newPassphrase = newPassphrase
        )
        return if (armored != null) Key(armored, keyId.id) else null
    }

    private fun String.updateOrganizationPrivateKey(currentPassphrase: ByteArray, newPassphrase: ByteArray): String? {
        return cryptoContext.pgpCrypto.updatePrivateKeyPassphrase(
            privateKey = this,
            oldPassphrase = currentPassphrase,
            newPassphrase = newPassphrase
        )
    }
}

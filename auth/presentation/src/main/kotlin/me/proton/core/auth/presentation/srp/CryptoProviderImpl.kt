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

package me.proton.core.auth.presentation.srp

import android.util.Base64
import com.proton.gopenpgp.crypto.ClearTextMessage
import com.proton.gopenpgp.crypto.Crypto
import me.proton.core.auth.domain.crypto.CryptoProvider
import me.proton.core.auth.domain.entity.Auth
import me.proton.core.auth.domain.entity.KeySecurity
import me.proton.core.auth.domain.entity.KeyType
import javax.inject.Inject
import com.proton.gopenpgp.crypto.KeyRing
import com.proton.gopenpgp.crypto.PlainMessage
import com.proton.gopenpgp.helper.Helper
import com.proton.gopenpgp.srp.Srp
import me.proton.core.auth.domain.crypto.PasswordVerifier
import me.proton.core.auth.domain.crypto.PrimeGenerator
import me.proton.core.auth.domain.entity.Primes
import me.proton.core.auth.domain.exception.EmptyCredentialsException
import me.proton.core.auth.domain.exception.InvalidPrimeException
import me.proton.core.auth.domain.exception.KeyGenerationException
import java.math.BigInteger
import java.security.SecureRandom

/**
 * @author Dino Kadrikj.
 */
class CryptoProviderImpl @Inject constructor() : CryptoProvider {

    companion object {
        const val CURRENT_AUTH_VERSION = 4
    }

    @Suppress("TooGenericExceptionCaught")
    // Proton GopenPGP lib throws this generic exception, so we have to live with this detekt warning
    // until the lib is updated
    override fun passphraseCanUnlockKey(armoredKey: String, passphrase: ByteArray): Boolean {
        return try {
            val unlockedKey = Crypto.newKeyFromArmored(armoredKey).unlock(passphrase)
            unlockedKey.clearPrivateParams()
            true
        } catch (ignored: Exception) {
            // means that the unlock check has failed. This is how gopenpgp works.
            false
        }
    }

    /**
     * Generates new private key.
     *
     * @param username the username for which the key will be generated
     * @param passphrase the mailbox entered/generated passphrase
     * @param keyType the type of key (see [KeyType])
     * @param keySecurity the key length (bits)
     *
     * @return the new private key as String.
     */
    override fun generateNewPrivateKey(
        username: String,
        domain: String,
        passphrase: ByteArray,
        keyType: KeyType,
        keySecurity: KeySecurity
    ): String {

        if (passphrase.isEmpty()) {
            throw EmptyCredentialsException("The passphrase for generating key can't be empty.")
        }

        if (keyType === KeyType.RSA) {
            // Generate some primes as the go library is quite slow. On android we can use SSL + multithreading
            // This reduces the generation time from 3 minutes to 1 second.
            val primes: Array<BigInteger?> = PrimeGenerator().generatePrimes(keySecurity.value / 2, 4)
                ?: throw InvalidPrimeException("Generating primes error. Null list of primes.")
            primes.forEach {
                if (it == null) {
                    throw InvalidPrimeException("Generating primes error. At least one of the primes is null.")
                }
            }
            val usernameAndDomain = "$username@$domain"
            return generateRSAKeyWithPrimes(
                usernameAndDomain,
                usernameAndDomain,
                passphrase,
                keySecurity.value,
                Primes(primes[0]!!, primes[1]!!, primes[2]!!, primes[3]!!)
            )
        }
        return Helper.generateKey(username, username, passphrase, keyType.toString(), keySecurity.value.toLong())
    }

    /**
     * Generates new signed key list for a key.
     *
     * @param passphrase the mailbox entered/generated passphrase
     *
     * @return a pair of key-list in JSON format and it's signature
     */
    override fun generateSignedKeyList(key: String, passphrase: ByteArray): Pair<String, String> {
        val keyFingerprint: String = Crypto.newKeyFromArmored(key).fingerprint
        val keyList = """
                        [
                            {
                                "Fingerprint": "$keyFingerprint",
                                "SHA256Fingerprints": ${String(Helper.getJsonSHA256Fingerprints(key))},
                                "Flags": 3,
                                "Primary": 1
                            }
                        ]
                        """.trimIndent()

        val signedKeyList = signTextDetached(keyList, key, passphrase)
        return Pair(keyList, signedKeyList)
    }

    override fun calculatePasswordVerifier(
        username: String,
        passphrase: ByteArray,
        modulusId: String,
        modulus: String
    ): Auth {
        val salt = ByteArray(10)
        SecureRandom().nextBytes(salt)
        val base64Modulus = Base64.decode(ClearTextMessage(modulus).data, Base64.DEFAULT)
        val hashedPassword: ByteArray = Srp.hashPassword(
            CURRENT_AUTH_VERSION.toLong(),
            String(passphrase),
            username,
            salt,
            base64Modulus
        )
        val passwordVerifier = PasswordVerifier(version = CURRENT_AUTH_VERSION, modulusId = modulusId, salt = salt)
        return passwordVerifier.generateAuth(KeySecurity.HIGH.value, base64Modulus, hashedPassword)
    }

    private fun signTextDetached(plainText: String, privateKey: String, passphrase: ByteArray): String {
        val privateKeyRing = buildPrivateKeyRingArmored(privateKey, passphrase)
        return privateKeyRing!!.signDetached(PlainMessage(plainText)).armored
    }

    /**
     * Builds KeyRing from single armored Private Key and unlocks it with provided passphrase.
     */
    private fun buildPrivateKeyRingArmored(key: String, passphrase: ByteArray): KeyRing? =
        Crypto.newKeyRing(Crypto.newKeyFromArmored(key).unlock(passphrase))

    /**
     * Crypto Helper doesn't have this method, so we have our own implementation
     */
    private fun generateRSAKeyWithPrimes(
        name: String, email: String, passphrase: ByteArray, bits: Int,
        primes: Primes
    ): String {
        val key = Crypto.generateRSAKeyWithPrimes(
            name,
            email,
            bits.toLong(),
            primes.prime1.toByteArray(),
            primes.prime2.toByteArray(),
            primes.prime3.toByteArray(),
            primes.prime4.toByteArray()
        )
        val lockedKey = key.lock(passphrase)
        if (lockedKey.isUnlocked) {
            throw KeyGenerationException("Generate address key error. Could not lock newly generated key.")
        }
        key.clearPrivateParams()
        return lockedKey.armor()
    }
}

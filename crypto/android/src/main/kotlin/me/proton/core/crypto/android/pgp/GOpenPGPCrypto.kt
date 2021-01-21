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

package me.proton.core.crypto.android.pgp

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.Radix64Encoder
import com.google.crypto.tink.subtle.Base64
import com.proton.gopenpgp.crypto.Crypto
import com.proton.gopenpgp.crypto.Key
import com.proton.gopenpgp.crypto.KeyRing
import com.proton.gopenpgp.crypto.PGPSignature
import com.proton.gopenpgp.crypto.PlainMessage
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.exception.CryptoException
import java.io.Closeable

/**
 * [PGPCrypto] implementation based on GOpenPGP Android library.
 */
@Suppress("TooManyFunctions")
class GOpenPGPCrypto : PGPCrypto {

    private class CloseableUnlockedKey(val value: Key) : Closeable {
        override fun close() {
            value.clearPrivateParams()
        }
    }

    private class CloseableUnlockedKeyRing(val value: KeyRing) : Closeable {
        override fun close() {
            value.clearPrivateParams()
        }
    }

    private fun <R> List<CloseableUnlockedKey>.use(block: (List<CloseableUnlockedKey>) -> R): R {
        try {
            return block(this)
        } finally {
            forEach { it.close() }
        }
    }

    private fun newKey(key: Unarmored) = CloseableUnlockedKey(Crypto.newKey(key))
    private fun newKeys(keys: List<Unarmored>) = keys.map { newKey(it) }

    private fun newKeyRing(key: CloseableUnlockedKey) =
        CloseableUnlockedKeyRing(Crypto.newKeyRing(key.value))

    private fun newKeyRing(keys: List<CloseableUnlockedKey>) =
        CloseableUnlockedKeyRing(Crypto.newKeyRing(null).apply { keys.forEach { addKey(it.value) } })

    private fun newKeyRing(key: Armored) =
        Crypto.newKeyRing(Crypto.newKeyFromArmored(key))

    private fun newKeyRing(keys: List<Armored>) =
        Crypto.newKeyRing(null).apply { keys.map { Crypto.newKeyFromArmored(it) }.forEach { addKey(it) } }

    override fun lock(
        unlockedKey: Unarmored,
        passphrase: ByteArray
    ): Armored {
        return newKey(unlockedKey).use { key ->
            key.value.lock(passphrase).armor()
        }
    }

    override fun unlock(
        privateKey: Armored,
        passphrase: ByteArray
    ): UnlockedKey = runCatching {
        val key = Crypto.newKeyFromArmored(privateKey)
        val unlockedKey = key.unlock(passphrase)
        return GOpenPGPUnlockedKey(unlockedKey)
    }.getOrElse { throw CryptoException("PrivateKey cannot be unlocked using passphrase.", it) }

    private inline fun <T> decrypt(
        message: EncryptedMessage,
        unlockedKey: Unarmored,
        block: (PlainMessage) -> T
    ): T {
        val pgpMessage = Crypto.newPGPMessageFromArmored(message)
        return newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                block(keyRing.value.decrypt(pgpMessage, null, 0))
            }
        }
    }

    override fun decryptText(
        message: EncryptedMessage,
        unlockedKey: Unarmored
    ): String = runCatching {
        decrypt(message, unlockedKey) { it.string }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptData(
        message: EncryptedMessage,
        unlockedKey: Unarmored
    ): ByteArray = runCatching {
        decrypt(message, unlockedKey) { it.binary }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    private fun sign(
        plainMessage: PlainMessage,
        unlockedKey: Unarmored
    ): Signature {
        return newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                keyRing.value.signDetached(plainMessage).armored
            }
        }
    }

    override fun signText(
        plainText: String,
        unlockedKey: Unarmored
    ): Signature = runCatching {
        sign(PlainMessage(plainText), unlockedKey)
    }.getOrElse { throw CryptoException("PlainText cannot be signed.", it) }

    override fun signData(
        data: ByteArray,
        unlockedKey: Unarmored
    ): Signature = runCatching {
        sign(PlainMessage(data), unlockedKey)
    }.getOrElse { throw CryptoException("Data cannot be signed.", it) }

    private fun encrypt(
        plainMessage: PlainMessage,
        publicKey: Armored
    ): EncryptedMessage {
        val publicKeyRing = newKeyRing(publicKey)
        return publicKeyRing.encrypt(plainMessage, null).armored
    }

    override fun encryptText(
        plainText: String,
        publicKey: Armored
    ): EncryptedMessage = runCatching {
        encrypt(PlainMessage(plainText), publicKey)
    }.getOrElse { throw CryptoException("PlainText cannot be encrypted.", it) }

    override fun encryptData(
        data: ByteArray,
        publicKey: Armored
    ): EncryptedMessage = runCatching {
        encrypt(PlainMessage(data), publicKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted.", it) }

    private fun verify(
        plainMessage: PlainMessage,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = runCatching {
        val pgpSignature = PGPSignature(signature)
        val publicKeyRing = newKeyRing(publicKey)
        publicKeyRing.verifyDetached(plainMessage, pgpSignature, validAtUtc)
    }.isSuccess

    override fun verifyText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = verify(PlainMessage(plainText), signature, publicKey, validAtUtc)

    override fun verifyData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = verify(PlainMessage(data), signature, publicKey, validAtUtc)

    private fun encryptAndSign(
        plainMessage: PlainMessage,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage {
        val publicKeyRing = newKeyRing(publicKey)
        return newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                publicKeyRing.encrypt(plainMessage, keyRing.value).armored
            }
        }
    }

    override fun encryptAndSignText(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage = runCatching {
        encryptAndSign(PlainMessage(plainText), publicKey, unlockedKey)
    }.getOrElse { throw CryptoException("PlainText cannot be encrypted or signed.", it) }

    override fun encryptAndSignData(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage = runCatching {
        encryptAndSign(PlainMessage(data), publicKey, unlockedKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted or signed.", it) }

    private inline fun <T> decryptAndVerify(
        msg: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        crossinline block: (PlainMessage) -> T
    ): T {
        val pgpMessage = Crypto.newPGPMessageFromArmored(msg)
        val publicKeyRing = newKeyRing(publicKeys)
        return newKeys(unlockedKeys).use { keys ->
            newKeyRing(keys).use { keyRing ->
                block(keyRing.value.decrypt(pgpMessage, publicKeyRing, Crypto.getUnixTime()))
            }
        }
    }

    override fun decryptAndVerifyText(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>
    ): String = runCatching {
        decryptAndVerify(message, publicKeys, unlockedKeys) { it.string }
    }.getOrElse { throw CryptoException("Message cannot be decrypted or verified.", it) }

    override fun decryptAndVerifyData(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>
    ): ByteArray = runCatching {
        decryptAndVerify(message, publicKeys, unlockedKeys) { it.binary }
    }.getOrElse { throw CryptoException("Message cannot be decrypted or verified.", it) }

    override fun getPublicKey(
        privateKey: Armored
    ): Armored = runCatching {
        Crypto.newKeyFromArmored(privateKey).armoredPublicKey
    }.getOrElse { throw CryptoException("Public key cannot be extracted from privateKey.", it) }

    override fun getFingerprint(
        key: Armored
    ): String = runCatching {
        Crypto.newKeyFromArmored(key).fingerprint
    }.getOrElse { throw CryptoException("Fingerprint cannot be extracted from key.", it) }

    override fun getPassphrase(
        password: ByteArray,
        encodedSalt: String
    ): ByteArray {
        val decodedKeySalt: ByteArray = Base64.decode(encodedSalt, Base64.DEFAULT)
        val rawHash = BCrypt.with(BCrypt.Version.VERSION_2Y).hashRaw(10, decodedKeySalt, password).rawHash
        return Radix64Encoder.Default().encode(rawHash)
    }
}

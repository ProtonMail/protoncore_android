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
import com.proton.gopenpgp.constants.Constants
import com.proton.gopenpgp.crypto.Crypto
import com.proton.gopenpgp.crypto.Key
import com.proton.gopenpgp.crypto.KeyRing
import com.proton.gopenpgp.crypto.PGPMessage
import com.proton.gopenpgp.crypto.PGPSignature
import com.proton.gopenpgp.crypto.PGPSplitMessage
import com.proton.gopenpgp.crypto.PlainMessage
import com.proton.gopenpgp.crypto.SessionKey
import com.proton.gopenpgp.helper.ExplicitVerifyMessage
import com.proton.gopenpgp.helper.Helper
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.DecryptedData
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.DecryptedText
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.EncryptedPacket
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.PacketType
import me.proton.core.crypto.common.pgp.PlainFile
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.crypto.common.pgp.helper.PrimeGenerator
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.math.BigInteger
import java.security.SecureRandom

/**
 * [PGPCrypto] implementation based on GOpenPGP Android library.
 */
@Suppress("TooManyFunctions")
class GOpenPGPCrypto : PGPCrypto {

    // region Private

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

    private fun newKey(key: Key) = CloseableUnlockedKey(key)
    private fun newKey(key: Unarmored) = newKey(Crypto.newKey(key))
    private fun newKeys(keys: List<Unarmored>) = keys.map { newKey(it) }

    private fun newKeyRing(key: CloseableUnlockedKey) =
        CloseableUnlockedKeyRing(Crypto.newKeyRing(key.value))

    private fun newKeyRing(keys: List<CloseableUnlockedKey>) =
        CloseableUnlockedKeyRing(Crypto.newKeyRing(null).apply { keys.forEach { addKey(it.value) } })

    private fun newKeyRing(key: Armored) =
        Crypto.newKeyRing(Crypto.newKeyFromArmored(key))

    private fun newKeyRing(keys: List<Armored>) =
        Crypto.newKeyRing(null).apply { keys.map { Crypto.newKeyFromArmored(it) }.forEach { addKey(it) } }

    private fun encrypt(
        plainMessage: PlainMessage,
        publicKey: Armored
    ): EncryptedMessage {
        val publicKeyRing = newKeyRing(publicKey)
        return publicKeyRing.encrypt(plainMessage, null).armored
    }

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

    private inline fun <T> decrypt(
        message: EncryptedMessage,
        unlockedKey: Unarmored,
        block: (PlainMessage) -> T
    ): T {
        val pgpMessage = Crypto.newPGPMessageFromArmored(message)
        return decrypt(pgpMessage, unlockedKey, block)
    }

    private inline fun <T> decrypt(
        file: EncryptedFile,
        unlockedKey: Unarmored,
        block: (PlainMessage) -> T
    ): T {
        val pgpSplitMessage = Crypto.newPGPSplitMessage(file.keyPacket, file.dataPacket)
        return decrypt(pgpSplitMessage, unlockedKey, block)
    }

    private inline fun <T> decrypt(
        pgpMessage: PGPMessage,
        unlockedKey: Unarmored,
        block: (PlainMessage) -> T
    ): T {
        return newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                block(keyRing.value.decrypt(pgpMessage, null, 0))
            }
        }
    }

    private inline fun <T> decrypt(
        pgpSplitMessage: PGPSplitMessage,
        unlockedKey: Unarmored,
        block: (PlainMessage) -> T
    ): T {
        return newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                block(keyRing.value.decryptAttachment(pgpSplitMessage))
            }
        }
    }

    private inline fun <T> decryptAndVerify(
        msg: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        validAtUtc: Long,
        crossinline block: (ExplicitVerifyMessage) -> T
    ): T {
        val pgpMessage = Crypto.newPGPMessageFromArmored(msg)
        val publicKeyRing = newKeyRing(publicKeys)
        return newKeys(unlockedKeys).use { keys ->
            newKeyRing(keys).use { keyRing ->
                block(Helper.decryptExplicitVerify(pgpMessage, keyRing.value, publicKeyRing, validAtUtc))
            }
        }
    }

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

    // endregion

    // region Lock/Unlock

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

    // endregion

    // region Encrypt

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

    override fun encryptFile(
        file: PlainFile,
        publicKey: Armored
    ): EncryptedFile = runCatching {
        newKeyRing(publicKey)
            .newLowMemoryAttachmentProcessor(file.inputStream.available().toLong(), file.fileName)
            .use(file.inputStream).let { EncryptedFile(it.keyPacket, it.dataPacket) }
    }.getOrElse { throw CryptoException("File cannot be encrypted.", it) }

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

    override fun encryptSessionKey(
        keyPacket: ByteArray,
        publicKey: Armored
    ): ByteArray = runCatching {
        val publicKeyRing = newKeyRing(publicKey)
        val sessionKey = SessionKey(keyPacket, Constants.AES256)
        return publicKeyRing.encryptSessionKey(sessionKey)
    }.getOrElse { throw CryptoException("KeyPacket cannot be encrypted.", it) }

    override fun encryptSessionKey(
        keyPacket: ByteArray,
        password: ByteArray
    ): ByteArray = runCatching {
        val sessionKey = SessionKey(keyPacket, Constants.AES256)
        return Crypto.encryptSessionKeyWithPassword(sessionKey, password)
    }.getOrElse { throw CryptoException("KeyPacket cannot be encrypted with password.", it) }

    // endregion

    // region Decrypt

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

    override fun decryptFile(
        file: EncryptedFile,
        unlockedKey: Unarmored
    ): DecryptedFile = runCatching {
        decrypt(file, unlockedKey) {
            val inputStream = ByteArrayInputStream(it.binary)
            DecryptedFile(it.filename, inputStream, VerificationStatus.NotSigned)
        }
    }.getOrElse { throw CryptoException("File cannot be decrypted.", it) }

    override fun decryptAndVerifyText(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        validAtUtc: Long,
    ): DecryptedText = runCatching {
        decryptAndVerify(message, publicKeys, unlockedKeys, validAtUtc) {
            DecryptedText(
                it.message.string,
                it.signatureVerificationError.toVerificationStatus()
            )
        }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptAndVerifyData(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        validAtUtc: Long,
    ): DecryptedData = runCatching {
        decryptAndVerify(message, publicKeys, unlockedKeys, validAtUtc) {
            DecryptedData(
                it.message.binary,
                it.signatureVerificationError.toVerificationStatus()
            )
        }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptSessionKey(
        keyPacket: ByteArray,
        unlockedKey: Unarmored
    ): ByteArray = runCatching {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                keyRing.value.decryptSessionKey(keyPacket).key
            }
        }
    }.getOrElse { throw CryptoException("KeyPacket cannot be decrypted.", it) }

    // endregion

    // region Sign

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

    override fun signFile(
        file: PlainFile,
        unlockedKey: Unarmored
    ): Signature = runCatching {
        sign(PlainMessage(file.inputStream.buffered().use { it.readBytes() }), unlockedKey)
    }.getOrElse { throw CryptoException("InputStream cannot be signed.", it) }

    // endregion

    // region Verify

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

    override fun verifyFile(
        file: DecryptedFile,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean {
        val plainMessage = PlainMessage(file.inputStream.buffered().use { it.readBytes() })
        return verify(plainMessage, signature, publicKey, validAtUtc)
    }

    // endregion

    // region Get

    override fun getArmored(
        data: Unarmored
    ): Armored = runCatching {
        Crypto.newPGPMessage(data).armored
    }.getOrElse { throw CryptoException("Armored cannot be extracted from Unarmored.", it) }

    override fun getUnarmored(
        data: Armored
    ): Unarmored = runCatching {
        Crypto.newPGPMessageFromArmored(data).binary
    }.getOrElse { throw CryptoException("Unarmored cannot be extracted from Armored.", it) }

    override fun getEncryptedPackets(
        message: EncryptedMessage
    ): List<EncryptedPacket> = runCatching {
        val pgpSplitMessage = PGPSplitMessage(message)
        return listOf(
            EncryptedPacket(pgpSplitMessage.keyPacket, PacketType.Key),
            EncryptedPacket(pgpSplitMessage.dataPacket, PacketType.Data)
        )
    }.getOrElse { throw CryptoException("EncryptedFile cannot be extracted from EncryptedMessage.", it) }

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

    override fun getJsonSHA256Fingerprints(
        key: Armored
    ): String = runCatching {
        Helper.getJsonSHA256Fingerprints(key).toString(Charsets.UTF_8)
    }.getOrElse { throw CryptoException("SHA256 Fingerprints cannot be extracted from key.", it) }

    override fun getPassphrase(
        password: ByteArray,
        encodedSalt: String
    ): ByteArray {
        val decodedKeySalt: ByteArray = Base64.decode(encodedSalt, Base64.DEFAULT)
        val rawHash = BCrypt.with(BCrypt.Version.VERSION_2Y).hashRaw(10, decodedKeySalt, password).rawHash
        return Radix64Encoder.Default().encode(rawHash)
    }

    override fun generateNewKeySalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val keySalt = Base64.encodeToString(salt, Base64.DEFAULT)
        // Truncate newline character.
        return keySalt.substring(0, keySalt.length - 1)
    }

    override fun generateNewToken(size: Long): ByteArray {
        fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
        val secret = Crypto.randomToken(size)
        val token = secret.toHexString().toByteArray(Charsets.UTF_8)
        require(token.size == secret.size * 2)
        return token
    }

    @Suppress("MagicNumber")
    override fun generateNewPrivateKey(
        username: String,
        domain: String,
        passphrase: ByteArray,
        keyType: PGPCrypto.KeyType,
        keySecurity: PGPCrypto.KeySecurity
    ): Armored = runCatching {
        check(passphrase.isNotEmpty()) { "The passphrase for generating key can't be empty." }

        // Set offset 24h in the past.
        Crypto.setKeyGenerationOffset(-86_400L)

        val email = "$username@$domain"

        when (keyType) {
            PGPCrypto.KeyType.RSA -> generateKeyFromRSA(username, email, passphrase, keySecurity)
            PGPCrypto.KeyType.X25519 -> generateKeyFromHelper(username, email, passphrase, keyType, keySecurity)
        }
    }.getOrElse { throw CryptoException("Key cannot be generated.", it) }

    @Suppress("MagicNumber")
    private fun generateKeyFromRSA(
        name: String,
        email: String,
        passphrase: ByteArray,
        keySecurity: PGPCrypto.KeySecurity
    ): Armored {
        // Generate some primes as the go library is quite slow.
        // On android we can use SSL + multithreading.
        // This reduces the generation time from 3 minutes to 1 second.
        val primes: Array<BigInteger?>? = PrimeGenerator().generatePrimes(keySecurity.value / 2, 4)

        checkNotNull(primes) { "Generating primes error. Null list of primes." }

        val arrays = primes
            .map { checkNotNull(it) { "At least one of the primes is null." } }
            .map { it.toByteArray() }

        return generateRSAKeyWithPrimes(
            name,
            email,
            passphrase,
            keySecurity.value,
            arrays[0], arrays[1], arrays[2], arrays[3]
        )
    }

    @Suppress("LongParameterList")
    private fun generateRSAKeyWithPrimes(
        name: String,
        email: String,
        passphrase: ByteArray,
        bits: Int,
        prime1: ByteArray,
        prime2: ByteArray,
        prime3: ByteArray,
        prime4: ByteArray
    ): Armored = runCatching {
        val generatedKey = Crypto.generateRSAKeyWithPrimes(name, email, bits.toLong(), prime1, prime2, prime3, prime4)
        newKey(generatedKey).use { key ->
            val lockedKey = key.value.lock(passphrase)
            check(lockedKey.isLocked) { "Could not lock newly generated key." }
            lockedKey.armor()
        }
    }.getOrElse { throw CryptoException("Key cannot be generated.", it) }

    private fun generateKeyFromHelper(
        name: String,
        email: String,
        passphrase: ByteArray,
        keyType: PGPCrypto.KeyType,
        keySecurity: PGPCrypto.KeySecurity
    ): Armored = Helper.generateKey(name, email, passphrase, keyType.toString(), keySecurity.value.toLong())

    // endregion
}

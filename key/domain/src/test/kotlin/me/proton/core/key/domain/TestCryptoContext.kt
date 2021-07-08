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

package me.proton.core.key.domain

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.DecryptedData
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.DecryptedText
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.EncryptedPacket
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.PacketType
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.VerificationStatus
import java.io.File

class TestCryptoContext : CryptoContext {

    // Default key for SimpleCrypto.
    private val defaultKey = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)

    // Use the defaultKey to encrypt/decrypt.
    override val keyStoreCrypto: KeyStoreCrypto = object : KeyStoreCrypto {
        override fun encrypt(value: String): EncryptedString =
            value.toByteArray().encrypt(defaultKey).fromByteArray()

        override fun decrypt(value: EncryptedString): String =
            value.toByteArray(Charsets.UTF_8).decrypt(defaultKey).fromByteArray()

        override fun encrypt(value: PlainByteArray): EncryptedByteArray =
            EncryptedByteArray(value.array.encrypt(defaultKey))

        override fun decrypt(value: EncryptedByteArray): PlainByteArray =
            PlainByteArray(value.array.decrypt(defaultKey))
    }

    // Map<PublicKey, UnlockedKey from PrivateKey> : Simulate encrypt(publicKey) -> decrypt(privateKey).
    val unlockedKeys = mutableMapOf<Armored, Unarmored>()

    // UnlockedKey = unlock(privateKey, passphrase) -> privateKey is encrypted using passphrase.
    // PrivateKey == PublicKey -> encrypt(publicKey, message) == encrypt(privateKey, message)
    override val pgpCrypto: PGPCrypto = object : PGPCrypto {

        private fun String.encryptMessage(key: Armored) = encryptMessage(unlockedKeys[key]!!)
        private fun String.encryptMessage(key: Unarmored) = toByteArray().encrypt(key).fromByteArray()

        private fun Armored.decryptMessage(key: Armored) = decryptMessage(unlockedKeys[key]!!)
        private fun Armored.decryptMessage(key: Unarmored) = toByteArray().decrypt(key).fromByteArray()
        private fun String.extractMessage() = substring(indexOf("[") + 1, indexOf("]"))

        // Encrypt key with passphrase.
        override fun lock(unlockedKey: Unarmored, passphrase: ByteArray): Armored =
            unlockedKey.encrypt(passphrase).fromByteArray()

        // Decrypt key with passphrase.
        override fun unlock(privateKey: Armored, passphrase: ByteArray): UnlockedKey =
            privateKey.toByteArray().decrypt(passphrase).let {
                object : UnlockedKey {
                    override val value = it
                    override fun close() = value.fill(0)
                }
            }

        // Concat text+key for testing purpose.
        override fun signText(plainText: String, unlockedKey: Unarmored): Signature =
            "sign([$plainText], with=${unlockedKey.fromByteArray()})"
                .encryptMessage(unlockedKey)

        // Concat data+key for testing purpose.
        override fun signData(data: ByteArray, unlockedKey: Unarmored): Signature =
            "sign([${data.fromByteArray()}], with=${unlockedKey.fromByteArray()})"
                .encryptMessage(unlockedKey)

        override fun signFile(file: File, unlockedKey: Unarmored): Signature =
            signData(file.readBytes(), unlockedKey)

        override fun verifyText(
            plainText: String,
            signature: Signature,
            publicKey: Armored,
            validAtUtc: Long
        ): Boolean {
            val decryptedSignature = signature.decryptMessage(publicKey)
            return plainText == decryptedSignature.extractMessage()
        }

        override fun verifyData(
            data: ByteArray,
            signature: Signature,
            publicKey: Armored,
            validAtUtc: Long
        ): Boolean {
            val decryptedSignature = signature.decryptMessage(publicKey)
            return data.fromByteArray() == decryptedSignature.extractMessage()
        }

        override fun verifyFile(
            file: DecryptedFile,
            signature: Armored,
            publicKey: Armored,
            validAtUtc: Long
        ): Boolean {
            val decryptedSignature = signature.decryptMessage(publicKey)
            val data = file.file.readBytes()
            return data.fromByteArray() == decryptedSignature.extractMessage()
        }

        override fun getArmored(data: Unarmored): Armored = data.fromByteArray()

        override fun getUnarmored(data: Armored): Unarmored = data.toByteArray()

        override fun getEncryptedPackets(message: EncryptedMessage): List<EncryptedPacket> = listOf(
            EncryptedPacket("keyPacket".toByteArray(), PacketType.Key),
            EncryptedPacket("dataPacket".toByteArray(), PacketType.Data),
        )

        override fun decryptText(message: EncryptedMessage, unlockedKey: Unarmored): String =
            message.decryptMessage(unlockedKey).let { decrypted ->
                check(decrypted.startsWith("TEXT"))
                decrypted.extractMessage()
            }

        override fun decryptData(message: EncryptedMessage, unlockedKey: Unarmored): ByteArray =
            message.decryptMessage(unlockedKey).let { decrypted ->
                check(decrypted.startsWith("BINARY"))
                decrypted.extractMessage().toByteArray()
            }

        override fun decryptFile(source: EncryptedFile, destination: File, unlockedKey: Unarmored): DecryptedFile {
            val data = source.file.readBytes()
            return DecryptedFile(
                file = destination.apply { appendBytes(decryptData(data.fromByteArray(), unlockedKey)) },
                status = VerificationStatus.NotSigned,
                filename = source.file.name,
                lastModifiedEpochSeconds = source.file.lastModified()
            )
        }

        override fun encryptText(plainText: String, publicKey: Armored): EncryptedMessage =
            "TEXT([$plainText]+$publicKey)"
                .encryptMessage(publicKey)

        override fun encryptData(data: ByteArray, publicKey: Armored): EncryptedMessage =
            "BINARY([${data.fromByteArray()}]+$publicKey)"
                .encryptMessage(publicKey)

        override fun encryptFile(source: File, destination: File, publicKey: Armored): EncryptedFile {
            val data = source.readBytes()
            return EncryptedFile(
                file = destination.apply { appendBytes(encryptData(data, publicKey).toByteArray()) },
                keyPacket = source.name.toByteArray(),
            )
        }

        override fun encryptAndSignText(
            plainText: String,
            publicKey: Armored,
            unlockedKey: Unarmored
        ): EncryptedMessage =
            "TEXT([$plainText]+$publicKey+${unlockedKey.fromByteArray()})"
                .encryptMessage(unlockedKey)

        override fun encryptAndSignData(data: ByteArray, publicKey: Armored, unlockedKey: Unarmored): EncryptedMessage =
            "BINARY([${data.fromByteArray()}]+$publicKey+${unlockedKey.fromByteArray()})"
                .encryptMessage(unlockedKey)

        override fun encryptSessionKey(keyPacket: ByteArray, publicKey: Armored): ByteArray = keyPacket

        override fun encryptSessionKey(keyPacket: ByteArray, password: ByteArray): ByteArray = keyPacket

        override fun decryptAndVerifyText(
            message: EncryptedMessage,
            publicKeys: List<Armored>,
            unlockedKeys: List<Unarmored>,
            validAtUtc: Long
        ): DecryptedText = DecryptedText(
            message.decryptMessage(unlockedKeys.first()).let {
                check(it.startsWith("TEXT"))
                it.extractMessage()
            },
            VerificationStatus.Success
        )

        override fun decryptAndVerifyData(
            message: EncryptedMessage,
            publicKeys: List<Armored>,
            unlockedKeys: List<Unarmored>,
            validAtUtc: Long
        ): DecryptedData = DecryptedData(
            message.decryptMessage(unlockedKeys.first()).let {
                check(it.startsWith("BINARY"))
                it.extractMessage().toByteArray()
            },
            VerificationStatus.Success
        )

        override fun decryptSessionKey(keyPacket: ByteArray, unlockedKey: Unarmored): ByteArray = keyPacket

        override fun getPublicKey(privateKey: Armored): Armored = privateKey

        override fun getFingerprint(key: Armored): String = "fingerprint($key)"

        override fun getJsonSHA256Fingerprints(key: Armored): String = "jsonSHA256Fingerprint($key)"

        override fun getPassphrase(password: ByteArray, encodedSalt: String): ByteArray = password.copyOf()

        override fun generateNewKeySalt(): String = "keySalt"

        override fun generateNewToken(size: Long): ByteArray = "token".toByteArray()

        override fun generateNewPrivateKey(
            username: String,
            domain: String,
            passphrase: ByteArray,
            keyType: PGPCrypto.KeyType,
            keySecurity: PGPCrypto.KeySecurity
        ): Armored = "privateKey"

        override fun updateTime(epochSeconds: Long) = Unit
    }
}

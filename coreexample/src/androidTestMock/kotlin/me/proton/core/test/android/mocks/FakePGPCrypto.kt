/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.test.android.mocks

import kotlinx.serialization.Serializable
import io.mockk.mockk
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.DataPacket
import me.proton.core.crypto.common.pgp.DecryptedData
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.DecryptedMimeMessage
import me.proton.core.crypto.common.pgp.DecryptedText
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.EncryptedPacket
import me.proton.core.crypto.common.pgp.EncryptedSignature
import me.proton.core.crypto.common.pgp.HashKey
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.SignatureContext
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.serialize
import java.io.File
import java.util.Base64

class FakePGPCrypto : PGPCrypto {
    private val decoder = Base64.getDecoder()
    private val encoder = Base64.getEncoder()

    override fun isPublicKey(key: Armored): Boolean {
        return mockk()
    }

    override fun isPrivateKey(key: Armored): Boolean {
        return mockk()
    }

    override fun isValidKey(key: Armored): Boolean {
        return true
    }

    override fun isForwardingKey(key: Armored): Boolean {
        return false
    }

    @Serializable
    data class ByteArrayList(val keys: List<ByteArray>)

    override fun serializeKeys(keys: List<Unarmored>): ByteArray =
        ByteArrayList(keys).serialize().encodeToByteArray()

    override fun deserializeKeys(keys: ByteArray): List<Unarmored> =
        keys.decodeToString().deserialize<ByteArrayList>().keys

    override fun lock(unlockedKey: Unarmored, passphrase: ByteArray): Armored {
        return decoder.decode(unlockedKey).decodeToString()
    }

    override fun unlock(privateKey: Armored, passphrase: ByteArray): UnlockedKey {
        return object : UnlockedKey {
            override val value: Unarmored get() = encoder.encode(privateKey.encodeToByteArray())
            override fun close() = Unit
        }
    }

    override fun encryptText(plainText: String, publicKey: Armored): EncryptedMessage {
        return plainText
    }

    override fun encryptTextWithPassword(text: String, password: ByteArray): EncryptedMessage {
        TODO("Not yet implemented: encryptTextWithPassword")
    }

    override fun encryptData(data: ByteArray, publicKey: Armored): EncryptedMessage {
        return encoder.encodeToString(data)
    }

    override fun encryptData(data: ByteArray, sessionKey: SessionKey): DataPacket {
        return data
    }

    override fun encryptDataWithPassword(data: ByteArray, password: ByteArray): EncryptedMessage {
        TODO("Not yet implemented: encryptDataWithPassword")
    }

    override fun encryptFile(source: File, destination: File, sessionKey: SessionKey): EncryptedFile {
        TODO("Not yet implemented: encryptFile")
    }

    override fun encryptAndSignText(plainText: String, publicKey: Armored, unlockedKey: Unarmored, signatureContext: SignatureContext?): EncryptedMessage {
        TODO("Not yet implemented: encryptAndSignText")
    }

    override fun encryptAndSignTextWithCompression(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): EncryptedMessage {
        TODO("Not yet implemented: encryptAndSignTextWithCompression")
    }

    override fun encryptAndSignData(data: ByteArray, publicKey: Armored, unlockedKey: Unarmored, signatureContext: SignatureContext?): EncryptedMessage {
        TODO("Not yet implemented: encryptAndSignData")
    }

    override fun encryptAndSignDataWithCompression(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): EncryptedMessage {
        TODO("Not yet implemented: encryptAndSignDataWithCompression")
    }

    override fun encryptAndSignData(data: ByteArray, sessionKey: SessionKey, unlockedKey: Unarmored, signatureContext: SignatureContext?): DataPacket {
        TODO("Not yet implemented: encryptAndSignData")
    }

    override fun encryptAndSignFile(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): EncryptedFile {
        TODO("Not yet implemented: encryptAndSignFile")
    }

    override fun encryptSessionKey(sessionKey: SessionKey, publicKey: Armored): KeyPacket {
        TODO("Not yet implemented: encryptSessionKey")
    }

    override fun encryptSessionKeyWithPassword(sessionKey: SessionKey, password: ByteArray): KeyPacket {
        TODO("Not yet implemented: encryptSessionKeyWithPassword")
    }

    override fun encryptMessageToAdditionalKey(
        message: EncryptedMessage,
        unlockedKey: Unarmored,
        publicKey: Armored,
    ): EncryptedMessage {
        TODO("Not yet implemented: encryptMessageToAdditionalKey")
    }

    override fun decryptText(message: EncryptedMessage, unlockedKey: Unarmored): String {
        TODO("Not yet implemented: decryptText")
    }

    override fun decryptTextWithPassword(message: EncryptedMessage, password: ByteArray): String {
        TODO("Not yet implemented: decryptTextWithPassword")
    }

    override fun decryptData(message: EncryptedMessage, unlockedKey: Unarmored): ByteArray {
        TODO("Not yet implemented: decryptData")
    }

    override fun decryptData(data: DataPacket, sessionKey: SessionKey): ByteArray {
        TODO("Not yet implemented: decryptData")
    }

    override fun decryptDataWithPassword(message: EncryptedMessage, password: ByteArray): ByteArray {
        TODO("Not yet implemented: decryptTextWithPassword")
    }

    override fun decryptFile(source: EncryptedFile, destination: File, sessionKey: SessionKey): DecryptedFile {
        TODO("Not yet implemented: decryptFile")
    }

    override fun decryptMimeMessage(message: EncryptedMessage, unlockedKeys: List<Unarmored>): DecryptedMimeMessage {
        TODO("Not yet implemented: decryptMimeMessage")
    }

    override fun decryptAndVerifyText(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): DecryptedText {
        TODO("Not yet implemented: decryptAndVerifyText")
    }

    override fun decryptAndVerifyData(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): DecryptedData {
        TODO("Not yet implemented: decryptAndVerifyData")
    }

    override fun decryptAndVerifyData(
        data: DataPacket,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): DecryptedData {
        TODO("Not yet implemented: decryptAndVerifyData")
    }

    override fun decryptAndVerifyFile(
        source: EncryptedFile,
        destination: File,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): DecryptedFile {
        TODO("Not yet implemented: decryptAndVerifyFile")
    }

    override fun decryptAndVerifyMimeMessage(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime
    ): DecryptedMimeMessage {
        TODO("Not yet implemented: decryptAndVerifyMimeMessage")
    }

    override fun decryptSessionKey(keyPacket: KeyPacket, unlockedKey: Unarmored): SessionKey {
        TODO("Not yet implemented: decryptSessionKey")
    }

    override fun decryptSessionKeyWithPassword(keyPacket: KeyPacket, password: ByteArray): SessionKey {
        TODO("Not yet implemented: decryptSessionKeyWithPassword")
    }

    override fun signText(
        plainText: String,
        unlockedKey: Unarmored,
        trimTrailingSpaces: Boolean,
        signatureContext: SignatureContext?
    ): Signature {
        return "${plainText.hashCode()}"
    }

    override fun signData(data: ByteArray, unlockedKey: Unarmored, signatureContext: SignatureContext?): Signature {
        TODO("Not yet implemented: signData")
    }

    override fun signFile(file: File, unlockedKey: Unarmored, signatureContext: SignatureContext?): Signature {
        TODO("Not yet implemented: signFile")
    }

    override fun signTextEncrypted(
        plainText: String,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        trimTrailingSpaces: Boolean,
        signatureContext: SignatureContext?
    ): EncryptedSignature {
        TODO("Not yet implemented: signTextEncrypted")
    }

    override fun signDataEncrypted(
        data: ByteArray,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        signatureContext: SignatureContext?
    ): EncryptedSignature {
        TODO("Not yet implemented: signDataEncrypted")
    }

    override fun signFileEncrypted(
        file: File,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        signatureContext: SignatureContext?
    ): EncryptedSignature {
        TODO("Not yet implemented: signFileEncrypted")
    }

    override fun verifyText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        trimTrailingSpaces: Boolean,
        verificationContext: VerificationContext?
    ): Boolean {
        return "${plainText.hashCode()}" == signature
    }

    override fun verifyData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean {
        TODO("Not yet implemented: verifyData")
    }

    override fun verifyFile(
        file: DecryptedFile,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean {
        TODO("Not yet implemented: verifyFile")
    }

    override fun getVerifiedTimestampOfText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        trimTrailingSpaces: Boolean,
        verificationContext: VerificationContext?
    ): Long? {
        TODO("Not yet implemented: getVerifiedTimestampOfText")
    }

    override fun getVerifiedTimestampOfData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Long? {
        TODO("Not yet implemented: getVerifiedTimestampOfData")
    }

    override fun verifyTextEncrypted(
        plainText: String,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
        trimTrailingSpaces: Boolean,
        verificationContext: VerificationContext?
    ): Boolean {
        TODO("Not yet implemented: verifyTextEncrypted")
    }

    override fun verifyDataEncrypted(
        data: ByteArray,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean {
        TODO("Not yet implemented: verifyDataEncrypted")
    }

    override fun verifyFileEncrypted(
        file: File,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean {
        TODO("Not yet implemented: verifyFileEncrypted")
    }

    override fun getArmored(data: Unarmored, header: PGPHeader): Armored {
        TODO("Not yet implemented: getArmored")
    }

    override fun getUnarmored(data: Armored): Unarmored {
        TODO("Not yet implemented: getUnarmored")
    }

    override fun getEncryptedPackets(message: EncryptedMessage): List<EncryptedPacket> {
        TODO("Not yet implemented: getEncryptedPackets")
    }

    override fun getPublicKey(privateKey: Armored): Armored {
        return privateKey
    }

    override fun getFingerprint(key: Armored): String {
        TODO("Not yet implemented: getFingerprint")
    }

    override fun isKeyExpired(key: Armored): Boolean {
        TODO("Not yet implemented: isKeyExpired")
    }

    override fun isKeyRevoked(key: Armored): Boolean {
        TODO("Not yet implemented: isKeyRevoked")
    }

    override fun getSHA256Fingerprint(key: Armored): String {
        TODO("Not yet implemented: getSHA256Fingerprint")
    }

    override fun getJsonSHA256Fingerprints(key: Armored): String {
        TODO("Not yet implemented: getJsonSHA256Fingerprints")
    }

    override fun getBase64Encoded(array: ByteArray): String {
        TODO("Not yet implemented: getBase64Encoded")
    }

    override fun getBase64EncodedNoWrap(array: ByteArray): String {
        TODO("Not yet implemented: getBase64EncodedNoWrap")
    }

    override fun getBase64Decoded(string: String): ByteArray {
        TODO("Not yet implemented: getBase64Decoded")
    }

    override fun getPassphrase(password: ByteArray, encodedSalt: String): ByteArray {
        return password
    }

    override fun generateNewSessionKey(): SessionKey {
        TODO("Not yet implemented: generateNewSessionKey")
    }

    override fun generateNewHashKey(): HashKey {
        TODO("Not yet implemented: generateNewHashKey")
    }

    override fun generateNewKeySalt(): String {
        TODO("Not yet implemented: generateNewKeySalt")
    }

    override fun generateNewToken(size: Int): ByteArray {
        TODO("Not yet implemented: generateNewToken")
    }

    override fun generateRandomBytes(size: Int): ByteArray {
        TODO("Not yet implemented: generateRandomBytes")
    }

    override fun generateNewPrivateKey(username: String, domain: String, passphrase: ByteArray): Armored {
        TODO("Not yet implemented: generateNewPrivateKey")
    }

    override fun updatePrivateKeyPassphrase(
        privateKey: Armored,
        passphrase: ByteArray,
        newPassphrase: ByteArray
    ): Armored {
        TODO("Not yet implemented: updatePrivateKeyPassphrase")
    }

    override suspend fun getCurrentTime(): Long {
        TODO("Not yet implemented: getCurrentTime")
    }

    override fun updateTime(epochSeconds: Long) = Unit
}

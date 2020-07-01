@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.crypto

import com.proton.pmcrypto.armor.Armor
import com.proton.pmcrypto.crypto.MIMECallbacks
import com.proton.pmcrypto.crypto.PmCrypto
import com.proton.pmcrypto.key.Key
import com.proton.pmcrypto.models.DecryptSignedVerify
import com.proton.pmcrypto.models.EncryptedSplit
import com.proton.pmcrypto.models.SessionSplit

class OpenPGP(private val goCrypto: PmCrypto) {

    val time: Long get() = goCrypto.time

    @Throws(Exception::class)
    fun decryptAttachment(
        keyPacket: ByteArray,
        dataPacket: ByteArray,
        privateKey: String,
        passphrase: String
    ): ByteArray {
        return goCrypto.decryptAttachment(keyPacket, dataPacket, privateKey, passphrase)
    }

    @Throws(Exception::class)
    fun decryptAttachmentBinKey(
        keyPacket: ByteArray,
        dataPacket: ByteArray,
        privateKeys: ByteArray,
        passphrase: String
    ): ByteArray {
        return goCrypto.decryptAttachmentBinKey(keyPacket, dataPacket, privateKeys, passphrase)
    }

    @Throws(Exception::class)
    fun decryptAttachmentWithPassword(
        keyPacket: ByteArray,
        dataPacket: ByteArray,
        password: String
    ): ByteArray {
        return goCrypto.decryptAttachmentWithPassword(keyPacket, dataPacket, password)
    }

    @Throws(Exception::class)
    fun decryptMessage(encryptedText: String, privateKey: String, passphrase: String): String {
        return goCrypto.decryptMessage(encryptedText, privateKey, passphrase)
    }

    @Throws(Exception::class)
    fun decryptMessageBinKey(
        encryptedText: String,
        privateKey: ByteArray,
        passphrase: String
    ): String {
        return goCrypto.decryptMessageBinKey(encryptedText, privateKey, passphrase)
    }

    @Throws(Exception::class)
    fun decryptMessageVerify(
        encryptedText: String,
        verifierKey: String,
        privateKey: String,
        passphrase: String,
        verifyTime: Long
    ): DecryptSignedVerify {
        return goCrypto.decryptMessageVerify(
            encryptedText,
            verifierKey,
            privateKey,
            passphrase,
            verifyTime
        )
    }

    @Throws(Exception::class)
    fun decryptMessageVerifyBinKey(
        encryptedText: String,
        verifierKey: ByteArray,
        privateKey: String,
        passphrase: String,
        verifyTime: Long
    ): DecryptSignedVerify {
        return goCrypto.decryptMessageVerifyBinKey(
            encryptedText,
            verifierKey,
            privateKey,
            passphrase,
            verifyTime
        )
    }

    @Throws(Exception::class)
    fun decryptMessageVerifyBinKeyPrivbinkeys(
        encryptedText: String,
        verifierKey: ByteArray,
        privateKeys: ByteArray,
        passphrase: String,
        verifyTime: Long
    ): DecryptSignedVerify {
        return goCrypto.decryptMessageVerifyBinKeyPrivBinKeys(
            encryptedText,
            verifierKey,
            privateKeys,
            passphrase,
            verifyTime
        )
    }

    @Throws(Exception::class)
    fun decryptMessageVerifyPrivbinkeys(
        encryptedText: String,
        verifierKey: String,
        privateKeys: ByteArray,
        passphrase: String,
        verifyTime: Long
    ): DecryptSignedVerify {
        return goCrypto.decryptMessageVerifyPrivBinKeys(
            encryptedText,
            verifierKey,
            privateKeys,
            passphrase,
            verifyTime
        )
    }

    @Throws(Exception::class)
    fun decryptMessageWithPassword(encrypted: String, password: String): String {
        return goCrypto.decryptMessageWithPassword(encrypted, password)
    }

    @Throws(Exception::class)
    fun encryptAttachment(
        plainData: ByteArray,
        fileName: String,
        publicKey: String
    ): EncryptedSplit {
        return goCrypto.encryptAttachment(plainData, fileName, publicKey)
    }

    @Throws(Exception::class)
    fun encryptAttachmentBinKey(
        plainData: ByteArray,
        fileName: String,
        publicKey: ByteArray
    ): EncryptedSplit {
        return goCrypto.encryptAttachment(plainData, fileName, Armor.armorKey(publicKey))
    }

    @Throws(Exception::class)
    fun encryptAttachmentWithPassword(plainData: ByteArray, password: String): String {
        return goCrypto.encryptAttachmentWithPassword(plainData, password)
    }

    @Throws(Exception::class)
    fun encryptMessage(
        plainText: String,
        publicKey: String,
        privateKey: String,
        passphrase: String,
        trim: Boolean
    ): String {
        return goCrypto.encryptMessage(plainText, publicKey, privateKey, passphrase, trim)
    }

    @Throws(Exception::class)
    fun encryptMessageBinKey(
        plainText: String,
        publicKey: ByteArray,
        privateKey: String,
        passphrase: String,
        trim: Boolean
    ): String {
        return goCrypto.encryptMessageBinKey(plainText, publicKey, privateKey, passphrase, trim)
    }

    @Throws(Exception::class)
    fun encryptMessageWithPassword(plainText: String, password: String): String {
        return goCrypto.encryptMessageWithPassword(plainText, password)
    }

    @Throws(Exception::class)
    fun generateKey(
        userName: String,
        domain: String,
        passphrase: String,
        keyType: KeyType,
        bits: Int
    ): String {
        // Generate some primes as the go library is quite slow. On android we can use SSL + multithreading
        // This reduces the generation time from 3 minutes to 1 second.
        if (keyType === KeyType.RSA) {
            val generator = PrimeGenerator()
            val primes = generator.generatePrimes(bits / 2, 4)
            return goCrypto.generateRSAKeyWithPrimes(
                userName, domain, passphrase, bits.toLong(),
                primes[0].toByteArray(), primes[1].toByteArray(),
                primes[2].toByteArray(), primes[3].toByteArray()
            )
        }
        return goCrypto.generateKey(userName, domain, passphrase, keyType.toString(), bits.toLong())
    }

    @Throws(Exception::class)
    fun isKeyExpired(publicKey: String): Boolean {
        return goCrypto.isKeyExpired(publicKey)
    }

    @Throws(Exception::class)
    fun isKeyExpiredBin(publicKey: ByteArray): Boolean {
        return goCrypto.isKeyExpiredBin(publicKey)
    }

    @Throws(Exception::class)
    fun signBinDetached(plainData: ByteArray, privateKey: String, passphrase: String): String {
        return goCrypto.signBinDetached(plainData, privateKey, passphrase)
    }

    @Throws(Exception::class)
    fun signBinDetachedBinKey(
        plainData: ByteArray,
        privateKey: ByteArray,
        passphrase: String
    ): String {
        return goCrypto.signBinDetachedBinKey(plainData, privateKey, passphrase)
    }

    @Throws(Exception::class)
    fun signTextDetached(
        plainText: String,
        privateKey: String,
        passphrase: String,
        trim: Boolean
    ): String {
        return goCrypto.signTextDetached(plainText, privateKey, passphrase, trim)
    }

    @Throws(Exception::class)
    fun signTextDetachedBinKey(
        plainText: String,
        privateKey: ByteArray,
        passphrase: String,
        trim: Boolean
    ): String {
        return goCrypto.signTextDetachedBinKey(plainText, privateKey, passphrase, trim)
    }

    @Throws(Exception::class)
    fun updatePrivateKeyPassphrase(
        privateKey: String,
        oldPassphrase: String,
        newPassphrase: String
    ): String {
        return goCrypto.updatePrivateKeyPassphrase(privateKey, oldPassphrase, newPassphrase)
    }

    fun updateTime(newTime: Long) {
        goCrypto.updateTime(newTime)
    }

    @Throws(Exception::class)
    fun verifyBinSignDetached(
        signature: String,
        plainData: ByteArray,
        publicKey: String,
        verifyTime: Long
    ): Boolean {
        return goCrypto.verifyBinSignDetached(signature, plainData, publicKey, verifyTime)
    }

    @Throws(Exception::class)
    fun verifyBinSignDetachedBinKey(
        signature: String,
        plainData: ByteArray,
        publicKey: ByteArray,
        verifyTime: Long
    ): Boolean {
        return goCrypto.verifyBinSignDetachedBinKey(signature, plainData, publicKey, verifyTime)
    }

    @Throws(Exception::class)
    fun verifyTextSignDetached(
        signature: String,
        plainText: String,
        publicKey: String,
        verifyTime: Long
    ): Boolean {
        return goCrypto.verifyTextSignDetached(signature, plainText, publicKey, verifyTime)
    }

    @Throws(Exception::class)
    fun verifyTextSignDetachedBinKey(
        signature: String,
        plainText: String,
        publicKey: ByteArray,
        verifyTime: Long
    ): Boolean {
        return goCrypto.verifyTextSignDetachedBinKey(signature, plainText, publicKey, verifyTime)
    }

    @Throws(Exception::class)
    fun getSessionFromKeyPacketBinkeys(
        keyPackage: ByteArray,
        privateKey: ByteArray,
        passphrase: String
    ): SessionSplit {
        return goCrypto.getSessionFromKeyPacketBinkeys(keyPackage, privateKey, passphrase)
    }

    @Throws(Exception::class)
    fun keyPacketWithPublicKeyBin(sessionSplit: SessionSplit, publicKey: ByteArray): ByteArray {
        return goCrypto.keyPacketWithPublicKeyBin(sessionSplit, publicKey)
    }

    @Throws(Exception::class)
    fun keyPacketWithPublicKey(sessionSplit: SessionSplit, publicKey: String): ByteArray {
        return goCrypto.keyPacketWithPublicKey(sessionSplit, publicKey)
    }

    @Throws(Exception::class)
    fun symmetricKeyPacketWithPassword(sessionSplit: SessionSplit, password: String): ByteArray {
        return goCrypto.symmetricKeyPacketWithPassword(sessionSplit, password)
    }

    @Throws(Exception::class)
    fun getFingerprint(key: String): String {
        return Key.getFingerprint(key)
    }

    @Throws(Exception::class)
    fun getFingerprint(key: ByteArray): String {
        return Key.getFingerprintBinKey(key)
    }

    @Throws(Exception::class)
    fun randomToken(): ByteArray {
        return goCrypto.randomToken()
    }

    @Throws(Exception::class)
    fun decryptMIMEMessage(
        encryptedText: String, verifierKey: ByteArray,
        privateKeys: ByteArray, passphrase: String,
        callbacks: MIMECallbacks, verifyTime: Long
    ) {
        goCrypto.decryptMIMEMessage(
            encryptedText,
            verifierKey,
            privateKeys,
            passphrase,
            callbacks,
            verifyTime
        )
    }

}

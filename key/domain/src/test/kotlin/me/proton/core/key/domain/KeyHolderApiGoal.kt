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
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.extension.keyHolder
import java.io.File

internal fun keyHolderApi(
    context: CryptoContext,
    keyHolder: KeyHolder
) {
    keyHolder.useKeys(context) {
        val message = "message"
        val data = message.toByteArray()

        // Encrypt + Sign Detached.
        val encrypted = encryptText(message)
        val signature = signText(message)

        val decrypted = decryptText(encrypted)
        verifyText(decrypted, signature)

        // Encrypt + Sign Embedded (more secure).
        val encryptedSigned = encryptAndSignText(message)
        decryptAndVerifyText(encryptedSigned)

        val encryptedSignedData = encryptAndSignData(data)
        decryptAndVerifyData(encryptedSignedData)

        val file = data.getFile("file")
        val keyPacket = generateNewKeyPacket()
        val encryptedFile = encryptFile(source = file, destination = File("file.encrypted"), keyPacket)
        decryptFile(source = encryptedFile, destination = File("file.decrypted"), keyPacket)
    }
}

internal fun keyHolderApiOrNull(
    context: CryptoContext,
    keyHolder: KeyHolder
) {
    keyHolder.useKeys(context) {
        val message = "message"
        val data = message.toByteArray()

        val encrypted = encryptText(message)
        val signature = signText(message)

        decryptTextOrNull(encrypted)?.let { decrypted ->
            verifyText(decrypted, signature)
        }

        // Encrypt + Sign Embedded (more secure).
        val encryptedSigned = encryptAndSignText(message)
        decryptAndVerifyTextOrNull(encryptedSigned)

        val encryptedSignedData = encryptAndSignData(data)
        decryptAndVerifyDataOrNull(encryptedSignedData)
    }
}

internal fun publicAddressApi(
    context: CryptoContext,
    keyHolder: KeyHolder,
    publicAddresses: List<PublicAddress>
) {
    keyHolder.useKeys(context) {
        val file = File("file.plain")

        val keyPacket = generateNewKeyPacket()
        val encryptedFile = encryptFile(source = file, destination = File("file.encrypted"), keyPacket)
        val signedFile = signFile(file)

        val fileSessionKey = decryptSessionKey(keyPacket)
        publicAddresses.map { publicAddress ->
            publicAddress.encryptSessionKey(context, fileSessionKey)
        }

        decryptFileOrNull(source = encryptedFile, destination = File("file.decrypted"), keyPacket)?.let {
            verifyFile(it, signedFile)
        }
    }
}

internal fun nestedKeyCreation(
    context: CryptoContext,
    keyHolder: KeyHolder
) {
    // Generate a new Nested Private Key from keyHolder keys.
    val decryptedNestedPrivateKey = keyHolder.useKeys(context) {
        val encryptedKey = encryptAndSignNestedKey(generateNestedPrivateKey("username", "domain"))
        check(encryptedKey.isEncrypted)

        // Use this parent to decrypt the nested Private Key.
        decryptAndVerifyNestedKeyOrThrow(encryptedKey)
    }
    // Then directly use the Private Key (e.g. for single crypto function call).
    decryptedNestedPrivateKey.privateKey.encryptText(context, "text")

    // Or convert NestedPrivateKey to KeyHolder for full KeyHolder Api usage.
    decryptedNestedPrivateKey.keyHolder().useKeys(context) {
        val message = "message"

        val encryptedText = encryptText(message)
        val signedText = signText(message)

        verifyText(decryptText(encryptedText), signedText)
    }
}

internal fun optionalOnPublicApi(
    context: CryptoContext,
    publicKey: PublicKey,
    publicKeyRing: PublicKeyRing,
    publicAddress: PublicAddress
) {
    val sessionKey = SessionKey("key".toByteArray())

    // PublicKey/PublicKeyRing/PublicAddress can encrypt and verify.
    publicKey.encryptText(context, "message")
    publicKey.encryptSessionKey(context, sessionKey)
    publicKey.verifyText(context, "decryptedMessage", "signature")

    publicKeyRing.encryptText(context, "message")
    publicKeyRing.encryptSessionKey(context, sessionKey)
    publicKeyRing.verifyText(context, "decryptedMessage", "signature")

    publicAddress.encryptText(context, "message")
    publicAddress.verifyText(context, "decryptedMessage", "signature")
}

internal fun optionalOnPrivateApi(
    context: CryptoContext,
    privateKey: PrivateKey,
    privateKeyRing: PrivateKeyRing
) {
    // Encrypt or Sign.
    privateKey.encryptText(context, "message")
    privateKey.signText(context, "message")

    // KeyPacket for File, for example.
    val keyPacket = "keyPacket".toByteArray()

    // PrivateKey can be unlocked (using embedded encrypted passphrase).
    val unlockedPrivateKey = privateKey.unlock(context)

    // Then UnlockedPrivateKey can decrypt and sign.
    unlockedPrivateKey.use { key ->
        with(key) {
            // Decrypt Text or Data -> throwing exceptions.
            decryptText(context, "encryptedMessage")
            decryptData(context, "encryptedMessage")
            decryptSessionKey(context, keyPacket)

            // Decrypt Text or Data -> using orNull extensions.
            decryptTextOrNull(context, "encryptedMessage")
            decryptDataOrNull(context, "encryptedMessage")

            // Sign Text or Data.
            signText(context, "message")
            signData(context, "message".toByteArray())

            // Finally lock - currently not used.
            lock(context, EncryptedByteArray("passphrase".toByteArray()))
        }
    }

    // PrivateKeyRing is automatically unlocked on demand (using embedded encrypted passphrase).
    privateKeyRing.use { key ->
        with(key) {
            decryptText("encryptedMessage")
            decryptSessionKey(keyPacket)
            signText("message")
        }
    }
    // All key resources are now closed/cleared from memory.
}

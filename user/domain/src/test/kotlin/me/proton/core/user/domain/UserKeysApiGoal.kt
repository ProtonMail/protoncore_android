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

package me.proton.core.user.domain

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.hmacSha256
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.decryptAndVerifyDataOrNull
import me.proton.core.key.domain.decryptAndVerifyText
import me.proton.core.key.domain.decryptAndVerifyTextOrNull
import me.proton.core.key.domain.decryptFile
import me.proton.core.key.domain.decryptAndVerifyHashKey
import me.proton.core.key.domain.decryptAndVerifyNestedKeyOrNull
import me.proton.core.key.domain.decryptAndVerifyNestedKeyOrThrow
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.decryptText
import me.proton.core.key.domain.decryptTextOrNull
import me.proton.core.key.domain.encryptAndSignData
import me.proton.core.key.domain.encryptAndSignText
import me.proton.core.key.domain.encryptFile
import me.proton.core.key.domain.encryptAndSignHashKey
import me.proton.core.key.domain.encryptAndSignNestedKey
import me.proton.core.key.domain.encryptSessionKey
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.NestedPrivateKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey
import me.proton.core.key.domain.extension.allowCompromisedKeys
import me.proton.core.key.domain.extension.keyHolder
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.generateNestedPrivateKey
import me.proton.core.key.domain.generateNewHashKey
import me.proton.core.key.domain.generateNewKeyPacket
import me.proton.core.key.domain.generateNewSessionKey
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.signText
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyText
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.util.kotlin.sha256
import java.io.File

internal fun userKeysApi(
    context: CryptoContext,
    user: User,
    userAddress: UserAddress,
) {
    // User extends KeyHolder.
    user.useKeys(context) {
        val message = "message"
        val data = message.toByteArray()

        // Encrypt + Sign Detached.
        val encryptedText = encryptText(message)
        val signedText = signText(message)

        val decryptedText = decryptText(encryptedText)
        verifyText(decryptedText, signedText)

        // Encrypt + Sign Embedded (more secure).
        val encryptedSignedText = encryptAndSignText(message)
        decryptAndVerifyText(encryptedSignedText)

        val encryptedSignedData = encryptAndSignData(data)
        decryptAndVerifyData(encryptedSignedData)
    }

    // UserAddress extends KeyHolder.
    userAddress.useKeys(context) {
        val message = "message"
        val data = message.toByteArray()

        // Encrypt + Sign Detached.
        val encryptedText = encryptText(message)
        val signedText = signText(message)

        decryptTextOrNull(encryptedText)?.let {
            verifyText(it, signedText)
        }

        // Encrypt + Sign Embedded (more secure).
        val encryptedSignedText = encryptAndSignText(message)
        decryptAndVerifyTextOrNull(encryptedSignedText)

        val encryptedSignedData = encryptAndSignData(data)
        decryptAndVerifyDataOrNull(encryptedSignedData)
    }
}

internal fun nestedKeyCreation(
    context: CryptoContext,
    userAddress: UserAddress
) {
    // Generate a new Nested Private Key from UserAddress keys.
    val encryptedNestedPrivateKey = userAddress.useKeys(context) {
        encryptAndSignNestedKey(userAddress.generateNestedPrivateKey(context))
    }
    check(encryptedNestedPrivateKey.isEncrypted)

    // Use parent, UserAddress to decrypt the nested Private Key.
    val decryptedNestedPrivateKey = userAddress.useKeys(context) {
        decryptAndVerifyNestedKeyOrThrow(encryptedNestedPrivateKey)
    }
    // Then directly use the Private Key (e.g. for single crypto function call).
    decryptedNestedPrivateKey.privateKey.encryptText(context, "text")
    decryptedNestedPrivateKey.privateKey.decryptText(context, "encryptedText")

    // Or convert NestedPrivateKey to KeyHolder for full KeyHolder Api usage.
    decryptedNestedPrivateKey.keyHolder().useKeys(context) {
        val message = "message"

        val encryptedText = encryptText(message)
        val signedText = signText(message)

        verifyText(decryptText(encryptedText), signedText)

        // Generate a new Nested Private Key from this KeyHolder keys.
        encryptAndSignNestedKey(generateNestedPrivateKey("username", "domain"))
    }
}

internal fun extendedKeyHolderApi(
    context: CryptoContext,
    userAddress: UserAddress,
    calendarPassphrase: EncryptedMessage,
    calendarPassphraseSignature: Signature,
    calendarKey: EncryptedMessage
) {
    data class CalendarPrivateKey(
        override val keyId: KeyId = KeyId("keyId"),
        override val privateKey: PrivateKey
    ) : KeyHolderPrivateKey

    // Just extend KeyHolder to benefit from KeyHolder Api (KeyHolder.useKeys).
    data class Calendar(
        // ...
        override val keys: List<CalendarPrivateKey>,
    ) : KeyHolder

    fun from(userAddress: UserAddress): Calendar {
        val nestedPrivateKey = userAddress.useKeys(context) {
            decryptAndVerifyNestedKeyOrThrow(calendarKey, calendarPassphrase, calendarPassphraseSignature)
        }
        // Build Calendar: specify privateKey + passphrase.
        return Calendar(listOf(CalendarPrivateKey(privateKey = nestedPrivateKey.privateKey)))
    }

    val calendar = from(userAddress)
    val message = "message"

    // Use KeyHolder Api.
    calendar.useKeys(context) {
        verifyText(decryptText(encryptText(message)), signText(message))
    }
}

internal fun convertToKeyHolderApi(
    context: CryptoContext,
    userAddress: UserAddress,
    calendarPassphrase: EncryptedMessage,
    calendarPassphraseSignature: Signature,
    calendarKey: EncryptedMessage
) {
    val nestedPrivateKey = userAddress.useKeys(context) {
        decryptAndVerifyNestedKeyOrThrow(calendarPassphrase, calendarPassphraseSignature, calendarKey)
    }

    // One unlock-able PrivateKey is enough to convert into KeyHolder.
    val calendar = nestedPrivateKey.keyHolder()
    val message = "message"

    // Use KeyHolder Api.
    calendar.useKeys(context) {
        verifyText(decryptText(encryptText(message)), signText(message))
    }
}

// For example, Drive Node that can be the parent of other Nodes.
data class Node(override val keys: List<KeyHolderPrivateKey>) : KeyHolder

@SuppressWarnings("UnusedPrivateMember")
internal fun nestedNodeKeyCreation(
    context: CryptoContext,
    parentNode: Node,
    userAddress: KeyHolder,
    decryptedSourceFile: File,
    encryptedDestinationFile: File,
    decryptedDestinationFile: File,
) {
    // Generate a new Nested Private Key from Parent Node.
    val username = "username"
    val domain = "domain.com"
    val encryptedNestedPrivateKey = userAddress.useKeys(context) {
        val nestedPrivateKey = NestedPrivateKey.generateNestedPrivateKey(context, username, domain)
        encryptAndSignNestedKey(nestedPrivateKey, encryptKeyRing = parentNode.publicKeyRing(context))
    }
    check(encryptedNestedPrivateKey.isEncrypted)

    val decryptedPrivateNestedKey = parentNode.useKeys(context) {
        val verificationKeyRing = userAddress.publicKeyRing(context)
        decryptAndVerifyNestedKeyOrNull(
            encryptedNestedPrivateKey,
            verifyKeyRing = verificationKeyRing
        ) ?: decryptAndVerifyNestedKeyOrThrow(
            encryptedNestedPrivateKey,
            // Retry decryption, but allowing the use of compromised keys
            verifyKeyRing = verificationKeyRing.allowCompromisedKeys()
        )
    }

    val currentNode = decryptedPrivateNestedKey.keyHolder()
    currentNode.useKeys(context) {
        // If KeyPacket is needed.
        val keyPacket = generateNewKeyPacket()
        encryptFile(decryptedSourceFile, encryptedDestinationFile, keyPacket)
        decryptFile(encryptedDestinationFile, decryptedDestinationFile, keyPacket)

        // If SessionKey is needed (for multiple chunk encryption in the same useKeys block).
        generateNewSessionKey().use { sessionKey ->
            val encryptedFile = encryptFile(decryptedSourceFile, encryptedDestinationFile, sessionKey)
            val decryptedFile = decryptFile(encryptedDestinationFile, decryptedDestinationFile, sessionKey)
            val nameHash = generateNewHashKey().use { hashKey ->
                val encryptedHashKey = userAddress.useKeys(context) {
                    encryptAndSignHashKey(hashKey, encryptKeyRing = currentNode.publicKeyRing(context))
                }
                val decryptedHashKey = currentNode.useKeys(context) {
                    decryptAndVerifyHashKey(encryptedHashKey, verifyKeyRing = userAddress.publicKeyRing(context))
                }
                hashKey.hmacSha256(decryptedSourceFile.name)
            }
            val fileHash = encryptedFile.sha256()
            val nodeKey = encryptedNestedPrivateKey.privateKey.key
            val nodePassphrase = encryptedNestedPrivateKey.passphrase
            val nodePassphraseSignature = encryptedNestedPrivateKey.passphraseSignature
            val contentKeyPacket = encryptSessionKey(sessionKey)
            val contentKeyPacketSignature = signData(contentKeyPacket)
        }

        // If you need to get the sessionKey from KeyPacket.
        val sessionKey = decryptSessionKey(keyPacket)
    }
}

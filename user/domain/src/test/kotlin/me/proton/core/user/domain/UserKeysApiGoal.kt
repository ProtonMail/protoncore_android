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
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.decryptAndVerifyDataOrNull
import me.proton.core.key.domain.decryptAndVerifyNestedKey
import me.proton.core.key.domain.decryptAndVerifyText
import me.proton.core.key.domain.decryptAndVerifyTextOrNull
import me.proton.core.key.domain.decryptText
import me.proton.core.key.domain.decryptTextOrNull
import me.proton.core.key.domain.encryptAndSignData
import me.proton.core.key.domain.encryptAndSignText
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey
import me.proton.core.key.domain.extension.keyHolder
import me.proton.core.key.domain.generateNestedPrivateKey
import me.proton.core.key.domain.signText
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyText
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress

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
    val nestedPrivateKey = userAddress.generateNestedPrivateKey(context)
    // Save those below to nested KeyHolder.
    checkNotNull(nestedPrivateKey.passphraseSignature)
    checkNotNull(nestedPrivateKey.passphrase)
    checkNotNull(nestedPrivateKey.privateKey)

    // Use parent, UserAddress to decrypt the nested Private Key.
    val decryptedNestedPrivateKey = userAddress.useKeys(context) {
        decryptAndVerifyNestedKey(nestedPrivateKey)
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
        generateNestedPrivateKey("username", "domain")
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
            decryptAndVerifyNestedKey(calendarKey, calendarPassphrase, calendarPassphraseSignature)
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
        decryptAndVerifyNestedKey(calendarPassphrase, calendarPassphraseSignature, calendarKey)
    }

    // One unlock-able PrivateKey is enough to convert into KeyHolder.
    val calendar = nestedPrivateKey.keyHolder()
    val message = "message"

    // Use KeyHolder Api.
    calendar.useKeys(context) {
        verifyText(decryptText(encryptText(message)), signText(message))
    }
}

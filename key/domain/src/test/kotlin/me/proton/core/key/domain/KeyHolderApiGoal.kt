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
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.crypto.common.keystore.use
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey

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

internal fun extendedKeyHolderApi(
    context: CryptoContext,
    userAddress: KeyHolder
) {
    data class CalendarPrivateKey(
        override val keyId: KeyId,
        override val privateKey: PrivateKey
    ) : KeyHolderPrivateKey

    // Just extend KeyHolder to benefit from KeyHolder Api (KeyHolder.useKeys).
    data class CalendarKeyHolder(
        override val keys: List<CalendarPrivateKey>
    ) : KeyHolder

    fun from(
        userAddress: KeyHolder,
        encryptedCalendarPassphrase: Armored,
        calendarKeyId: String,
        calendarPrivateKey: Armored
    ): CalendarKeyHolder {
        // Get Calendar passphrase.
        val calendarPassphrase = userAddress.useKeys(context) {
            decryptData(encryptedCalendarPassphrase)
        }

        // Encrypt passphrase as it should be stored in PrivateKey.
        val passphrase = calendarPassphrase.use {
            it.encryptWith(context.keyStoreCrypto)
        }

        // Build CalendarKeyHolder: specify privateKey + passphrase.
        return CalendarKeyHolder(
            listOf(
                CalendarPrivateKey(
                    KeyId(calendarKeyId),
                    PrivateKey(calendarPrivateKey, true, passphrase)
                )
            )
        )
    }

    val calendar = from(userAddress, "encryptedCalendarPassphrase", "calendarKeyId", "calendarPrivateKey")

    val message = "message"

    // Use KeyHolder Api.
    calendar.useKeys(context) {
        verifyText(decryptText(encryptText(message)), signText(message))
    }
}

internal fun optionalOnPublicApi(
    context: CryptoContext,
    publicKey: PublicKey,
    publicKeyRing: PublicKeyRing,
    publicAddress: PublicAddress
) {
    // PublicKey/PublicKeyRing/PublicAddress can encrypt and verify.
    publicKey.encryptText(context, "message")
    publicKey.verifyText(context, "decryptedMessage", "signature")

    publicKeyRing.encryptText(context, "message")
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

    // PrivateKey can be unlocked (using embedded encrypted passphrase).
    val unlockedPrivateKey = privateKey.unlock(context)

    // Then UnlockedPrivateKey can decrypt and sign.
    unlockedPrivateKey.use { key ->
        with(key) {
            // Decrypt Text or Data -> throwing exceptions.
            decryptText(context, "encryptedMessage")
            decryptData(context, "encryptedMessage")

            // Decrypt Text or Data -> using orNull extensions.
            decryptTextOrNull(context, "encryptedMessage")
            decryptDataOrNull(context, "encryptedMessage")

            // Sign Text or Data.
            signText(context, "message")
            signData(context, "message".toByteArray())

            // Finally lock - currently not used.
            lock(context, EncryptedByteArray("passphrase".toByteArray()), false)
        }
    }

    // PrivateKeyRing is automatically unlocked on demand (using embedded encrypted passphrase).
    privateKeyRing.use { key ->
        with(key) {
            decryptText("encryptedMessage")
            signText("message")
        }
    }
    // All key resources are now closed/cleared from memory.
}

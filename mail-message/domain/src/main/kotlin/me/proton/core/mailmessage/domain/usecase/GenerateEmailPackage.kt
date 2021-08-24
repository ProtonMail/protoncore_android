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

package me.proton.core.mailmessage.domain.usecase

import com.google.crypto.tink.subtle.Base64
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.dataPacket
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.crypto.common.pgp.split
import me.proton.core.key.domain.encryptSessionKey
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.Recipient
import me.proton.core.mailmessage.domain.entity.EncryptedPackage
import javax.inject.Inject

class GenerateEmailPackage @Inject constructor(
    private val cryptoContext: CryptoContext
) {
    operator fun invoke(
        arguments: SendEmailDirect.Arguments,
        recipient: PublicAddress,
        decryptedAttachmentSessionKeys: MutableList<ByteArray>,
        decryptedBodySessionKey: ByteArray,
        encryptedBodyDataPacket: ByteArray
    ): EncryptedPackage = when (requireNotNull(recipient.recipient) { "Unknown recipient type." }) {
        Recipient.Internal -> {
            // Encrypt package using recipient's key.
            val recipientEncryptedBodyCipherText = recipient
                .encryptText(cryptoContext, arguments.body)
                .split(cryptoContext.pgpCrypto)
            val encryptedAttachmentKeyPackets = decryptedAttachmentSessionKeys.map {
                Base64.encode(recipient.encryptSessionKey(cryptoContext, SessionKey(it)))
            }
            EncryptedPackage(
                addresses = mapOf(
                    recipient.email to EncryptedPackage.Address.Internal(
                        bodyKeyPacket = Base64.encode(recipientEncryptedBodyCipherText.keyPacket()),
                        attachmentKeyPackets = encryptedAttachmentKeyPackets
                    )
                ),
                mimeType = arguments.mimeType,
                body = Base64.encode(recipientEncryptedBodyCipherText.dataPacket()),
                type = 1 // TODO handle this when we get SendPreferences
            )
        }
        Recipient.External -> {
            // Use sender's encrypted packets, because we send plaintext out.
            val packageAttachmentKeys = decryptedAttachmentSessionKeys.map {
                EncryptedPackage.Key(Base64.encode(it), "aes256")
            }
            EncryptedPackage(
                addresses = mapOf(recipient.email to EncryptedPackage.Address.External),
                mimeType = arguments.mimeType,
                body = Base64.encode(encryptedBodyDataPacket),
                type = 4, // TODO handle this when we get SendPreferences
                attachmentKeys = packageAttachmentKeys,
                bodyKey = EncryptedPackage.Key(Base64.encode(decryptedBodySessionKey), "aes256")
            )
        }
    }
}

fun GenerateEmailPackage.invokeOrNull(
    arguments: SendEmailDirect.Arguments,
    recipientPublicAddress: PublicAddress,
    decryptedAttachmentSessionKeys: MutableList<ByteArray>,
    decryptedBodySessionKey: ByteArray,
    encryptedBodyDataPacket: ByteArray
): EncryptedPackage? = runCatching {
    invoke(
        arguments,
        recipientPublicAddress,
        decryptedAttachmentSessionKeys,
        decryptedBodySessionKey,
        encryptedBodyDataPacket
    )
}.getOrNull()

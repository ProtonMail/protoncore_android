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
import me.proton.core.crypto.common.pgp.dataPacket
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.crypto.common.pgp.split
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.encryptAndSignText
import me.proton.core.key.domain.useKeys
import me.proton.core.mailmessage.domain.encryptAndSignAttachmentOrNull
import me.proton.core.mailmessage.domain.entity.Email
import me.proton.core.mailmessage.domain.entity.EmailReceipt
import me.proton.core.mailmessage.domain.entity.EncryptedAttachment
import me.proton.core.mailmessage.domain.entity.EncryptedEmail
import me.proton.core.mailmessage.domain.entity.EncryptedPackage
import me.proton.core.mailmessage.domain.entity.Filename
import me.proton.core.mailmessage.domain.repository.EmailMessageRepository
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.util.kotlin.filterNullValues
import me.proton.core.util.kotlin.nullIfBlank
import java.io.InputStream
import javax.inject.Inject

/**
 * Sends an email using the "send direct" helper route. Only usable when email contains attachments.
 *
 * Body's MIME Type has to be text/plain.
 *
 * Ignores the "send preferences".
 */
class SendEmailDirect @Inject constructor(
    private val emailMessageRepository: EmailMessageRepository,
    private val getRecipientPublicAddresses: GetRecipientPublicAddresses,
    private val generateEmailPackage: GenerateEmailPackage,
    private val cryptoContext: CryptoContext
) {

    data class Arguments(
        val subject: String,
        val body: String,
        val mimeType: String,
        val toEmailList: List<String>,
        val attachments: List<Attachment>
    ) {
        data class Attachment(
            val fileName: Filename,
            val fileSize: Int,
            val mimeType: String,
            val inputStream: InputStream
        )
    }

    sealed class Result {
        data class Success(val receipt: EmailReceipt) : Result()

        sealed class Error : Result() {
            data class GettingPublicAddressKeys(val emailAddresses: List<String>) : Error()
            data class GeneratingEmailPackages(val emailAddresses: List<String>) : Error()
            data class EncryptingAttachments(val attachmentFileNames: List<String>) : Error()
            object GettingSenderAddress : Error()
        }
    }

    suspend operator fun invoke(
        sender: UserAddress,
        arguments: Arguments
    ): Result {

        // Get public address keys for recipients.
        val publicAddresses = getRecipientPublicAddresses.invoke(sender.userId, arguments.toEmailList)
        val failedEmails = publicAddresses.filterValues { it == null }.keys
        if (failedEmails.isNotEmpty())
            return Result.Error.GettingPublicAddressKeys(failedEmails.toList())

        // Encrypt and sign attachments and body, create payload for sender.
        val decryptedAttachmentSessionKeys = mutableListOf<ByteArray>()
        val encodedAttachmentKeyPackets = mutableListOf<String>()

        lateinit var decryptedBodySessionKey: ByteArray
        lateinit var encryptedBodyDataPacket: ByteArray
        lateinit var encryptedEmail: EncryptedEmail

        val attachments = mutableMapOf<Filename, EncryptedAttachment?>()
        sender.useKeys(cryptoContext) {
            arguments.attachments.forEach { attachment ->
                attachments[attachment.fileName] = encryptAndSignAttachmentOrNull(attachment)
            }
        }
        val failedAttachmentFilenames = attachments.filterValues { it == null }.keys
        if (failedAttachmentFilenames.isNotEmpty())
            return Result.Error.EncryptingAttachments(failedAttachmentFilenames.toList())

        val encryptedAttachments = attachments.filterNullValues().values

        sender.useKeys(cryptoContext) {
            val senderAttachments = encryptedAttachments.map {
                EncryptedEmail.Attachment(
                    fileName = it.fileName,
                    mimeType = it.mimeType,
                    contents = Base64.encode(it.keyPacket.packet + it.dataPacket.packet + it.signature.packet)
                )
            }
            // TODO: sending works with this empty as well
            // encodedAttachmentKeyPackets.add(Base64.encode(encryptedAttachment.keyPacket))

            // Decrypt session keys of all attachments for later creation of packages for plaintext recipients.
            decryptedAttachmentSessionKeys.addAll(encryptedAttachments.map { decryptSessionKey(it.keyPacket.packet) })
            val encryptedBodyPgpMessage = encryptAndSignText(arguments.body)

            encryptedEmail = EncryptedEmail(
                subject = arguments.subject,
                sender = EncryptedEmail.Address(
                    sender.email,
                    sender.displayName?.nullIfBlank() ?: sender.email
                ),
                to = arguments.toEmailList.map { EncryptedEmail.Address(it, it) },
                cc = emptyList(),
                bcc = emptyList(),
                body = encryptedBodyPgpMessage,
                mimeType = arguments.mimeType,
                attachments = senderAttachments
            )

            // Decrypt body's session key to send it for plaintext recipients.
            val encryptedBodySplit = encryptedBodyPgpMessage.split(cryptoContext.pgpCrypto)
            decryptedBodySessionKey = decryptSessionKey(encryptedBodySplit.keyPacket())
            encryptedBodyDataPacket = encryptedBodySplit.dataPacket()
        }

        // Generate package for each recipient.
        val emailPackages = mutableMapOf<Email, EncryptedPackage?>()
        publicAddresses.filterNullValues().values.forEach { recipientPublicAddress ->
            emailPackages[recipientPublicAddress.email] = generateEmailPackage.invokeOrNull(
                arguments,
                recipientPublicAddress,
                decryptedAttachmentSessionKeys,
                decryptedBodySessionKey,
                encryptedBodyDataPacket
            )
        }
        val failedPackageEmails = emailPackages.filterValues { it == null }.keys
        if (failedPackageEmails.isNotEmpty())
            return Result.Error.GeneratingEmailPackages(failedPackageEmails.toList())

        // Send Email, Packages and attachmentKeyPackets.
        val receipt = emailMessageRepository.sendEmailDirect(
            userId = sender.userId,
            encryptedEmail = encryptedEmail,
            encryptedPackages = emailPackages.filterNullValues().values.toList(),
            attachmentKeys = encodedAttachmentKeyPackets
        )
        return Result.Success(receipt)
    }
}

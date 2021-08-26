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

package me.proton.core.mailmessage.domain

import me.proton.core.crypto.common.pgp.EncryptedPacket
import me.proton.core.crypto.common.pgp.PacketType
import me.proton.core.crypto.common.pgp.dataPacket
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.key.domain.encryptData
import me.proton.core.key.domain.encryptFile
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.generateNewKeyPacket
import me.proton.core.key.domain.getEncryptedPackets
import me.proton.core.key.domain.getUnarmored
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.signFile
import me.proton.core.mailmessage.domain.entity.EncryptedAttachment
import me.proton.core.mailmessage.domain.usecase.SendEmailDirect
import java.io.File

private fun KeyHolderContext.encryptAndSignAttachment(
    attachment: SendEmailDirect.Arguments.Attachment,
    attachmentData: ByteArray
): EncryptedAttachment {
    val encryptedData = encryptData(attachmentData)
    val encryptedPackets = getEncryptedPackets(encryptedData)
    return EncryptedAttachment(
        fileName = attachment.fileName,
        mimeType = attachment.mimeType,
        fileSize = attachment.fileSize,
        signature = EncryptedPacket(getUnarmored(signData(attachmentData)), PacketType.Signature),
        keyPacket = EncryptedPacket(encryptedPackets.keyPacket(), PacketType.Key),
        dataPacket = EncryptedPacket(encryptedPackets.dataPacket(), PacketType.Data)
    )
}

private fun KeyHolderContext.encryptAndSignAttachment(
    attachment: SendEmailDirect.Arguments.Attachment,
    attachmentFile: File
): EncryptedAttachment {
    val destination = File.createTempFile("${attachment.fileName}.", ".encrypted")
    try {
        val keyPacket = generateNewKeyPacket()
        val encryptedFile = encryptFile(attachmentFile, destination, keyPacket)
        return EncryptedAttachment(
            fileName = attachment.fileName,
            mimeType = attachment.mimeType,
            fileSize = attachment.fileSize,
            signature = EncryptedPacket(getUnarmored(signFile(attachmentFile)), PacketType.Signature),
            keyPacket = EncryptedPacket(keyPacket, PacketType.Key),
            dataPacket = EncryptedPacket(encryptedFile.readBytes(), PacketType.Data)
        )
    } finally {
        destination.delete()
    }
}

fun KeyHolderContext.encryptAndSignAttachment(
    attachment: SendEmailDirect.Arguments.Attachment
): EncryptedAttachment {
    val maxByteBufferSize = 1 * 1000 * 1000 // 1MB.

    fun encryptUsingByteArray(): EncryptedAttachment {
        val attachmentData = attachment.inputStream.readBytes()
        return encryptAndSignAttachment(attachment, attachmentData)
    }

    fun encryptUsingFile(): EncryptedAttachment {
        var attachmentFile: File? = null
        try {
            attachmentFile = File.createTempFile("${attachment.fileName}.", "")
            attachment.inputStream.use { input -> attachmentFile.outputStream().use { output -> input.copyTo(output) } }
            return encryptAndSignAttachment(attachment, attachmentFile)
        } finally {
            attachmentFile?.delete()
        }
    }

    return when {
        attachment.fileSize < maxByteBufferSize -> encryptUsingByteArray()
        else -> encryptUsingFile()
    }
}

fun KeyHolderContext.encryptAndSignAttachmentOrNull(
    attachment: SendEmailDirect.Arguments.Attachment
): EncryptedAttachment? = runCatching { encryptAndSignAttachment(attachment) }.getOrNull()

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
import me.proton.core.crypto.common.pgp.PlainFile
import me.proton.core.key.domain.encryptFile
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.getUnarmored
import me.proton.core.key.domain.signFile
import me.proton.core.mailmessage.domain.entity.EncryptedAttachment
import me.proton.core.mailmessage.domain.usecase.SendEmailDirect

fun KeyHolderContext.encryptAndSignAttachment(
    attachment: SendEmailDirect.Arguments.Attachment
): EncryptedAttachment {
    val file = PlainFile(attachment.fileName, attachment.inputStream)
    val encryptedFile = encryptFile(file)
    return EncryptedAttachment(
        fileName = attachment.fileName,
        mimeType = attachment.mimeType,
        fileSize = attachment.fileSize,
        signature = EncryptedPacket(getUnarmored(signFile(file)), PacketType.Signature),
        keyPacket = EncryptedPacket(encryptedFile.keyPacket, PacketType.Key),
        dataPacket = EncryptedPacket(encryptedFile.dataPacket, PacketType.Data)
    )
}

fun KeyHolderContext.encryptAndSignAttachmentOrNull(
    attachment: SendEmailDirect.Arguments.Attachment
): EncryptedAttachment? = runCatching { encryptAndSignAttachment(attachment) }.getOrNull()

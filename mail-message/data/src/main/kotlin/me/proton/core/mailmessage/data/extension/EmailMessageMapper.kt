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

package me.proton.core.mailmessage.data.extension

import me.proton.core.mailmessage.data.api.request.EmailMessage
import me.proton.core.mailmessage.domain.entity.EncryptedEmail

internal fun EncryptedEmail.toEmailMessage(): EmailMessage = EmailMessage(
    subject = subject,
    sender = EmailMessage.Address(sender.address, sender.name),
    to = to.map { EmailMessage.Address(it.address, it.name) },
    cc = cc.map { EmailMessage.Address(it.address, it.name) },
    bcc = bcc.map { EmailMessage.Address(it.address, it.name) },
    body = body,
    mimeType = mimeType,
    attachments = attachments.map { EmailMessage.Attachment(it.fileName, it.mimeType, it.contents) }
)

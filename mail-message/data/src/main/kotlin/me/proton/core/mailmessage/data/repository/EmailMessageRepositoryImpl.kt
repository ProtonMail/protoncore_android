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

package me.proton.core.mailmessage.data.repository

import me.proton.core.domain.entity.UserId
import me.proton.core.mailmessage.data.api.MailMessageApi
import me.proton.core.mailmessage.data.api.request.SendDirectRequest
import me.proton.core.mailmessage.data.extension.toEmailMessage
import me.proton.core.mailmessage.data.extension.toEmailPackage
import me.proton.core.mailmessage.domain.entity.EmailReceipt
import me.proton.core.mailmessage.domain.entity.EncryptedEmail
import me.proton.core.mailmessage.domain.entity.EncryptedPackage
import me.proton.core.mailmessage.domain.repository.EmailMessageRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.util.kotlin.toInt
import javax.inject.Inject

class EmailMessageRepositoryImpl @Inject constructor(
    private val provider: ApiProvider
) : EmailMessageRepository {

    override suspend fun sendEmailDirect(
        userId: UserId,
        encryptedEmail: EncryptedEmail,
        encryptedPackages: List<EncryptedPackage>,
        attachmentKeys: List<String>?
    ): EmailReceipt =
        provider.get<MailMessageApi>(userId).invoke {
            val response = sendDirect(
                SendDirectRequest(
                    emailMessage = encryptedEmail.toEmailMessage(),
                    autoSaveContacts = false.toInt(),
                    packages = encryptedPackages.map { it.toEmailPackage() },
                    attachmentKeys = attachmentKeys
                )
            )
            EmailReceipt(response.deliveryTime)
        }.valueOrThrow
}

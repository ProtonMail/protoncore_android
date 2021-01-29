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

package me.proton.core.mailmessage.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendDirectRequest(
    @SerialName("Message")
    val emailMessage: EmailMessage,
    @SerialName("AutoSaveContacts")
    val autoSaveContacts: Int,
    @SerialName("Packages")
    val packages: List<EmailPackage>,
    @SerialName("AttachmentKeys")
    val attachmentKeys: List<String>? = null
)

@Serializable
data class EmailMessage(
    @SerialName("Subject")
    val subject: String,
    @SerialName("Sender")
    val sender: Address,
    @SerialName("ToList")
    val to: List<Address>,
    @SerialName("CCList")
    val cc: List<Address>,
    @SerialName("BCCList")
    val bcc: List<Address>,
    @SerialName("Body")
    val body: String,
    @SerialName("MIMEType")
    val mimeType: String,
    @SerialName("Attachments")
    val attachments: List<Attachment>,
) {
    @Serializable
    data class Address(
        @SerialName("Address")
        val address: String,
        @SerialName("Name")
        val name: String
    )

    @Serializable
    data class Attachment(
        @SerialName("Filename")
        val fileName: String,
        @SerialName("MIMEType")
        val mimeType: String,
        @SerialName("Contents")
        val contents: String
    )
}


@Serializable
data class EmailPackage(
    @SerialName("Addresses")
    val addresses: Map<String, Address>,
    @SerialName("MIMEType")
    val mimeType: String,
    @SerialName("Body")
    val body: String,
    @SerialName("Type")
    val type: Int,
    @SerialName("AttachmentKeys")
    val attachmentKeys: List<Key>? = null, // TODO create separate entity without nullables?
    @SerialName("BodyKey")
    val bodyKey: Key? = null,
) {
    @Serializable
    data class Address(
        @SerialName("Type")
        val type: Int,
        @SerialName("Signature")
        val signature: Int,
        @SerialName("BodyKeyPacket")
        val bodyKeyPacket: String? = null,
        @SerialName("AttachmentKeyPackets")
        val attachmentKeyPackets: List<String>? = null
    )

    @Serializable
    data class Key(
        @SerialName("Key")
        val key: String,
        @SerialName("Algorithm")
        val algorithm: String,
    )
}

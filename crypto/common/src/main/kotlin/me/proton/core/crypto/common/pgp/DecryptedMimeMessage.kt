/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.crypto.common.pgp

data class DecryptedMimeMessage(
    val headers: List<String>,
    val body: DecryptedMimeBody,
    val attachments: List<DecryptedMimeAttachment>,
    val verificationStatus: VerificationStatus = VerificationStatus.Unknown
)

data class DecryptedMimeBody(
    val mimeType: String,
    val content: String
)

data class DecryptedMimeAttachment(
    val headers: String,
    val content: ByteArray
) {

    override fun equals(other: Any?): Boolean =
        this === other || other is DecryptedMimeAttachment &&
            headers == other.headers && content.contentEquals(other.content)

    override fun hashCode(): Int = 31 * content.contentHashCode() + headers.hashCode()

}

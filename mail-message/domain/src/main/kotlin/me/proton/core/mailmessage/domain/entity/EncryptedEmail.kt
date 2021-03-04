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

package me.proton.core.mailmessage.domain.entity

data class EncryptedEmail(
    val subject: String,
    val sender: Address,
    val to: List<Address>,
    val cc: List<Address>,
    val bcc: List<Address>,
    val body: String,
    val mimeType: String,
    val attachments: List<Attachment>,
) {

    data class Address(
        val address: String,
        val name: String
    )

    data class Attachment(
        val fileName: Filename,
        val mimeType: String,
        val contents: String
    )
}

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

package me.proton.core.contact.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactId

@Serializable
data class GetContactResponse(
    @SerialName("Contact")
    val contact: ContactResponse
)

@Serializable
data class ContactResponse(
    @SerialName("ID")
    val id: String,
    @SerialName("Name")
    val name: String,
    // @Ignore ?
    @SerialName("ContactEmails")
    val contactEmails: List<ContactEmailResponse>,
    @SerialName("Cards")
    val cards: List<ContactCardResponse>
) {
    fun toContact() = Contact(
        ContactId(id),
        name,
        contactEmails.map { it.toContactEmail() },
        cards.map { it.toContactCard() }
    )
}

@Serializable
data class ContactCardResponse(
    @SerialName("Type")
    val type: Int,
    @SerialName("Data")
    val data: String,
    @SerialName("Signature")
    val signature: String? = null
) {
    fun toContactCard() = ContactCard(
        type,
        data,
        signature
    )
}

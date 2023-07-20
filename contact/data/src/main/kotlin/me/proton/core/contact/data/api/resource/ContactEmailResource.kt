/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.contact.data.api.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactEmailId
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.toBooleanOrFalse

@Serializable
data class ContactEmailResource(
    @SerialName("ID")
    val id: String,
    @SerialName("Name")
    val name: String,
    @SerialName("Email")
    val email: String,
    @SerialName("Defaults")
    val defaults: Int, // 0 if contact contains custom sending preferences or keys, 1 otherwise
    @SerialName("Order")
    val order: Int,
    @SerialName("ContactID")
    val contactId: String,
    @SerialName("CanonicalEmail")
    val canonicalEmail: String? = null,
    @SerialName("LabelIDs")
    val labelIds: List<String>,
    @SerialName("IsProton")
    val isProton: Int? = null
) {
    fun toContactEmail(userId: UserId): ContactEmail = ContactEmail(
        userId,
        ContactEmailId(id),
        name,
        email,
        defaults,
        order,
        ContactId(contactId),
        canonicalEmail,
        labelIds,
        isProton?.toBooleanOrFalse()
    )
}

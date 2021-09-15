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

package me.proton.core.contact.data.local.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactWithCards

data class ContactCompoundEntity(
    @Embedded val contact: ContactEntity,
    @Relation(
        parentColumn = "contactId",
        entityColumn = "contactId"
    )
    val cards: List<ContactCardEntity>,
    @Relation(
        parentColumn = "contactId",
        entityColumn = "contactId",
        entity = ContactEmailEntity::class
    )
    val emails: List<ContactEmailCompoundEntity>,
)

fun ContactCompoundEntity.toContactWithCards() = ContactWithCards(
    contact = Contact(
        id = contact.contactId,
        name = contact.name,
        contactEmails = emails.map { it.toContactEmail() },
    ),
    contactCards = cards.map { it.toContactCard() }
)

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

package me.proton.core.contact.data.local.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactEmailId
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.UserEntity

@Entity(
    primaryKeys = ["contactId"],
    indices = [
        Index("userId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContactEntity (
    val userId: UserId,
    val contactId: ContactId,
    val name: String,
)

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

@Entity(
    indices = [
        Index("contactId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["contactId"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContactCardEntity(
    val contactId: ContactId,
    val type: Int,
    val data: String,
    val signature: String?
) {
    @PrimaryKey(autoGenerate = true)
    var cardId: Long = 0
}

@Entity(
    primaryKeys = ["contactEmailId"],
    indices = [
        Index("userId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContactEmailEntity(
    val userId: UserId,
    val contactEmailId: ContactEmailId,
    val name: String,
    val email: String,
    val defaults: Int,
    val order: Int,
    val contactId: ContactId,
    val canonicalEmail: String?
)

@Entity(
    primaryKeys = ["contactEmailId", "labelId"],
    foreignKeys = [
        ForeignKey(
            entity = ContactEmailEntity::class,
            parentColumns = ["contactEmailId"],
            childColumns = ["contactEmailId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContactEmailLabelCrossRef(
    val contactEmailId: ContactEmailId,
    val labelId: String
)

data class ContactEmailCompoundEntity(
    @Embedded
    val contactEmail: ContactEmailEntity,
    @Relation(
        parentColumn = "contactEmailId",
        entityColumn = "contactEmailId",
        entity = ContactEmailLabelCrossRef::class,
        projection = ["labelId"]
    )
    val labelIds: List<String>
)

fun Contact.toContactEntity(userId: UserId) = ContactEntity(
    userId = userId,
    contactId = id,
    name = name,
)

fun ContactCompoundEntity.toContact() = Contact(
    id = contact.contactId,
    name = contact.name,
    contactEmails = emails.map { it.toContactEmail() },
    cards = cards.map { it.toContactCard() }
)

fun ContactCard.toContactCardEntity(contactId: ContactId) = ContactCardEntity(
    contactId = contactId,
    type = type,
    data = data,
    signature = signature
)

fun ContactCardEntity.toContactCard() = ContactCard(type, data, signature)

fun ContactEmail.toContactEmailEntity(userId: UserId) = ContactEmailEntity(
    userId = userId,
    contactEmailId = id,
    name = name,
    email = email,
    defaults = defaults,
    order = order,
    contactId = contactId,
    canonicalEmail = canonicalEmail
)

fun ContactEmailCompoundEntity.toContactEmail() = ContactEmail(
    id = contactEmail.contactEmailId,
    name = contactEmail.name,
    email = contactEmail.email,
    defaults = contactEmail.defaults,
    order = contactEmail.order,
    contactId = contactEmail.contactId,
    canonicalEmail = contactEmail.canonicalEmail,
    labelIds = labelIds
)

fun ContactEmail.toContactEmailLabelCrossRefs() = labelIds.map {
    ContactEmailLabelCrossRef(
        contactEmailId = id,
        labelId = it
    )
}

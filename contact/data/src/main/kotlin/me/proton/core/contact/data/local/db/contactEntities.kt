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

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactEmail
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
class ContactEntity (
    val userId: UserId,
    val contactId: String,
    val name: String,
)

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
class ContactEmailEntity(
    val userId: UserId,
    val contactEmailId: String,
    val name: String,
    val email: String,
    val defaults: Int,
    val order: Int,
    val contactId: String,
    val canonicalEmail: String?
)

fun Contact.toContactEntity(userId: UserId) = ContactEntity(
    userId = userId,
    contactId = id,
    name = name
)

fun Contact.toContactEmailEntities(userId: UserId) = contactEmails.map { contactEmail ->
    contactEmail.toContactEmailEntity(userId)
}

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

fun ContactEmailEntity.toContactEmail() = ContactEmail(
    id = contactEmailId,
    name = name,
    email = email,
    defaults = defaults,
    order = order,
    contactId = contactId,
    canonicalEmail = canonicalEmail
)

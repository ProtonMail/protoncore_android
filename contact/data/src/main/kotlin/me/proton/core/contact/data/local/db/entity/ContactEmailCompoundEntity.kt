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
import me.proton.core.contact.domain.entity.ContactEmail

data class ContactEmailCompoundEntity(
    @Embedded
    val contactEmail: ContactEmailEntity,
    @Relation(
        parentColumn = "contactEmailId",
        entityColumn = "contactEmailId",
        entity = ContactEmailLabelEntity::class,
        projection = ["labelId"]
    )
    val labelIds: List<String>
)

fun ContactEmailCompoundEntity.toContactEmail() = ContactEmail(
    userId = contactEmail.userId,
    id = contactEmail.contactEmailId,
    name = contactEmail.name,
    email = contactEmail.email,
    defaults = contactEmail.defaults,
    order = contactEmail.order,
    contactId = contactEmail.contactId,
    canonicalEmail = contactEmail.canonicalEmail,
    labelIds = labelIds
)

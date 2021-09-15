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

import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactEmailId

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
data class ContactEmailLabelCrossRefEntity(
    val contactEmailId: ContactEmailId,
    val labelId: String // Add foreign key on label when module available
)

fun ContactEmail.toContactEmailLabelCrossRefs() = labelIds.map {
    ContactEmailLabelCrossRefEntity(
        contactEmailId = id,
        labelId = it
    )
}

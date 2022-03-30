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
import androidx.room.Index
import androidx.room.PrimaryKey
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactCardType
import me.proton.core.contact.domain.entity.ContactId

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

fun ContactCard.toContactCardEntity(contactId: ContactId): ContactCardEntity {
    return when (this) {
        is ContactCard.ClearText -> ContactCardEntity(contactId, ContactCardType.ClearText.value, data, null)
        is ContactCard.Encrypted -> {
            if (signature != null)
                ContactCardEntity(contactId, ContactCardType.EncryptedAndSigned.value, data, signature)
            else
                ContactCardEntity(contactId, ContactCardType.Encrypted.value, data, null)
        }
        is ContactCard.Signed -> ContactCardEntity(contactId, ContactCardType.Signed.value, data, signature)
    }
}

fun ContactCardEntity.toContactCard(): ContactCard {
    return when (ContactCardType.enumOf(type)?.enum) {
        ContactCardType.ClearText -> ContactCard.ClearText(data)
        ContactCardType.Signed -> ContactCard.Signed(data, requireNotNull(signature))
        ContactCardType.Encrypted -> ContactCard.Encrypted(data, null)
        ContactCardType.EncryptedAndSigned -> ContactCard.Encrypted(data, requireNotNull(signature))
        else -> throw IllegalStateException("Unsupported contact type $type")
    }
}

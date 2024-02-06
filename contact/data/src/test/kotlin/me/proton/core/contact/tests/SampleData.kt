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

package me.proton.core.contact.tests

import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.contact.data.local.db.entity.ContactEntity
import me.proton.core.contact.data.local.db.entity.toContactCardEntity
import me.proton.core.contact.data.local.db.entity.toContactEmailEntity
import me.proton.core.contact.data.local.db.entity.toContactEmailLabel
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactEmailId
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.user.domain.entity.Type

object User0 {
    val userId = UserId("user0")
    val userEntity = userId.userEntity()
    val accountEntity = userId.accountEntity()
    object Contact0 {
        val contactId = ContactId("contact0")
        val contact = contact(userId, contactId, listOf(ContactEmail0.contactEmail))
        val contactWithCards = contactWithCards(
            userId,
            contactId,
            listOf(ContactEmail0.contactEmail),
            listOf(ContactCard0.contactCard)
        )
        val contactEntity = userId.contactEntity(contactId)
        fun createContactEmail(
            contactEmailId: ContactEmailId,
            labelIds: List<String>
        ) = contactEmail(userId, contactId, contactEmailId, labelIds)
        object ContactCard0 {
            val contactCard = contactCard("data0")
            val contactCardEntity = contactCard.toContactCardEntity(contactId)
        }
        object ContactEmail0 {
            val contactEmailId = ContactEmailId("contactEmail0")
            val contactEmail = contactEmail(userId, contactId, contactEmailId, listOf("label0"))
            val contactEmailEntity = contactEmail.toContactEmailEntity()
            val emailLabelEntities = contactEmail.toContactEmailLabel().toTypedArray()
        }
    }
}

fun UserId.userEntity() = UserEntity(
    userId = this,
    email = null,
    name = null,
    displayName = null,
    currency = "EUR",
    credit = 0,
    type = Type.Proton.value,
    createdAtUtc = 1000L,
    usedSpace = 0,
    maxSpace = 0,
    maxUpload = 0,
    role = null,
    isPrivate = false,
    subscribed = 0,
    services = 0,
    delinquent = null,
    recovery = null,
    passphrase = null,
    maxBaseSpace = null,
    maxDriveSpace = null,
    usedBaseSpace = null,
    usedDriveSpace = null,
)

fun UserId.accountEntity() = AccountEntity(
    userId = this,
    username = "",
    email = null,
    state = AccountState.Ready,
    sessionId = null,
    sessionState = null
)

fun UserId.contactEntity(contactId: ContactId) = ContactEntity(
    userId = this,
    contactId = contactId,
    name = "contact$contactId"
)

fun contact(userId: UserId, contactId: ContactId, emails: List<ContactEmail>) = Contact(
    userId = userId,
    id = contactId,
    name = "contact$contactId",
    contactEmails = emails,
)

fun contactWithCards(
    userId: UserId,
    contactId: ContactId,
    emails: List<ContactEmail>,
    cards: List<ContactCard>
) = ContactWithCards(
    contact = contact(userId, contactId, emails),
    contactCards = cards
)

fun contactCard(data: String) = ContactCard.ClearText(data = data)

fun contactEmail(
    userId: UserId,
    contactId: ContactId,
    contactEmailId: ContactEmailId,
    labelIds: List<String>
) = ContactEmail(
    userId = userId,
    id = contactEmailId,
    name = "",
    email = "",
    defaults = 0,
    order = 0,
    contactId = contactId,
    canonicalEmail = null,
    labelIds = labelIds,
    isProton = false
)

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
import me.proton.core.contact.data.local.db.ContactEntity
import me.proton.core.contact.data.local.db.toContactCardEntity
import me.proton.core.contact.data.local.db.toContactEmailEntity
import me.proton.core.contact.data.local.db.toContactEmailLabelCrossRefs
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactEmailId
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.UserEntity

object User0 {
    val userId = UserId("user0")
    val userEntity = userId.userEntity()
    val accountEntity = userId.accountEntity()
    object Contact0 {
        val contactId = ContactId("contact0")
        val contact = contactId.contact(
            listOf(ContactEmail0.contactEmail),
            listOf(ContactCard0.contactCard)
        )
        val contactEntity = userId.contactEntity(contactId)
        object ContactCard0 {
            val contactCard = contactCard("data0")
            val contactCardEntity = contactCard.toContactCardEntity(contactId)
        }
        object ContactEmail0 {
            val contactEmailId = ContactEmailId("contactEmail0")
            val contactEmail = contactId.contactEmail(contactEmailId, listOf("label0"))
            val contactEmailEntity = contactEmail.toContactEmailEntity(userId)
            val emailLabelEntities = contactEmail.toContactEmailLabelCrossRefs().toTypedArray()
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
    usedSpace = 0,
    maxSpace = 0,
    maxUpload = 0,
    role = null,
    private = false,
    subscribed = 0,
    services = 0,
    delinquent = null,
    passphrase = null
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

fun ContactId.contact(emails: List<ContactEmail>, cards: List<ContactCard>) = Contact(
    id = this,
    name = "contact$this",
    contactEmails = emails,
    cards = cards
)

fun contactCard(data: String) = ContactCard(
    type = 0,
    data = data,
    signature = null
)

fun ContactId.contactEmail(contactEmailId: ContactEmailId, labelIds: List<String>) = ContactEmail(
    id = contactEmailId,
    name = "",
    email = "",
    defaults = 0,
    order = 0,
    contactId = this,
    canonicalEmail = null,
    labelIds = labelIds
)

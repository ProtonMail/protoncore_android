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

package me.proton.core.contact.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactEmailId
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.domain.entity.UserId

interface ContactLocalDataSource {
    fun observeContact(contactId: ContactId): Flow<ContactWithCards>
    fun observeAllContacts(userId: UserId): Flow<List<Contact>>

    suspend fun updateContacts(contacts: List<Contact>)
    suspend fun updateContactEmails(emails: List<ContactEmail>)

    suspend fun deleteContact(contactId: ContactId)
    suspend fun deleteContacts(contactIds: List<ContactId>)
    suspend fun deleteContactEmails(emailIds: List<ContactEmailId>)
    suspend fun deleteAllContacts(userId: UserId)
    suspend fun deleteAllContacts()

    suspend fun mergeContacts(userId: UserId, contacts: List<Contact>)
    suspend fun mergeContactWithCards(userId: UserId, contactWithCards: ContactWithCards)
}
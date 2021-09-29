/*
 * Copyright (c) 2020 Proton Technologies AG
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
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId

interface ContactRepository {

    /**
     * Observe [ContactWithCards] from [userId] by [contactId].
     */
    fun observeContactWithCards(
        userId: UserId,
        contactId: ContactId,
        refresh: Boolean = false
    ): Flow<DataResult<ContactWithCards>>

    /**
     * Get [ContactWithCards] from [userId] by [contactId].
     */
    suspend fun getContactWithCards(userId: UserId, contactId: ContactId, refresh: Boolean = false): ContactWithCards

    /**
     * Observe all [Contact] from [userId].
     */
    fun observeAllContacts(userId: UserId, refresh: Boolean = false): Flow<DataResult<List<Contact>>>

    /**
     * Get all [Contact] from [userId].
     */
    suspend fun getAllContacts(userId: UserId, refresh: Boolean = false): List<Contact>

    /**
     * Observe all [ContactEmail] from [userId].
     */
    fun observeAllContactEmails(userId: UserId, refresh: Boolean = false): Flow<DataResult<List<ContactEmail>>>

    /**
     * Get all [ContactEmail] from [userId].
     */
    suspend fun getAllContactEmails(userId: UserId, refresh: Boolean = false): List<ContactEmail>

    suspend fun createContacts(userId: UserId, contactCards: List<ContactCard>)
}

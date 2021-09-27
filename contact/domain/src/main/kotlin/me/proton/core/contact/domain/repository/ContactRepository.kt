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
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId

interface ContactRepository {

    /**
     * Get [ContactWithCards] using [sessionUserId] and [contactId].
     */
    suspend fun getContactWithCards(
        sessionUserId: SessionUserId,
        contactId: ContactId,
        refresh: Boolean = false
    ): ContactWithCards

    /**
     * Get all [Contact] using [sessionUserId].
     */
    fun observeAllContacts(sessionUserId: SessionUserId, refresh: Boolean = false): Flow<DataResult<List<Contact>>>

    /**
     * Get all [ContactEmail] using [sessionUserId].
     */
    fun observeAllContactEmails(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<DataResult<List<ContactEmail>>>
}

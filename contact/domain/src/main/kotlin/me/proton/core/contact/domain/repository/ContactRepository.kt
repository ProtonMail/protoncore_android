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

package me.proton.core.key.domain.repository

import Contact
import ContactEmail
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId

interface ContactRepository {

    /**
     * Get [Contact] using [sessionUserId] and [contactId].
     */
    suspend fun getContact(sessionUserId: SessionUserId, contactId: String, refresh: Boolean = false): Contact

    /**
     * Clear all persisted Contact data, by [userId].
     */
    suspend fun clearContacts(userId: UserId)

    /**
     * Clear all persisted Contact data.
     */
    suspend fun clearAllContacts()

    /**
     * Get list of [ContactEmail] using [sessionUserId].
     */
    suspend fun getContactEmails(sessionUserId: SessionUserId, refresh: Boolean = false): List<ContactEmail>

    /**
     * Clear all persisted Contact Email data, by [userId].
     */
    suspend fun clearContactEmails(userId: UserId)

    /**
     * Clear all persisted Contact Email data.
     */
    suspend fun clearAllContactEmails()

}

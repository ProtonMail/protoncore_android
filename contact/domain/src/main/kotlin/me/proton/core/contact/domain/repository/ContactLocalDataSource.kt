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

    /**
     * Observe local [ContactWithCards] by [contactId].
     */
    fun observeContact(contactId: ContactId): Flow<ContactWithCards?>

    /**
     * Observe all [Contact] from [userId].
     */
    fun observeAllContacts(userId: UserId): Flow<List<Contact>>

    /**
     * Update or insert [ContactWithCards].
     */
    suspend fun upsertContactWithCards(contactWithCards: ContactWithCards)

    /**
     * Update or insert [Contact].
     *
     * @throws SQLiteConstraintException if corresponding user(s) doesn't exist.
     */
    suspend fun upsertContacts(vararg contacts: Contact)

    /**
     * Update or insert [ContactEmail].
     *
     * @throws SQLiteConstraintException if corresponding contact(s) doesn't exist.
     */
    suspend fun upsertContactEmails(vararg emails: ContactEmail)

    /**
     * Delete contact(s) by [contactIds].
     */
    suspend fun deleteContacts(vararg contactIds: ContactId)

    /**
     * Delete contact email(s) by [emailIds].
     */
    suspend fun deleteContactEmails(vararg emailIds: ContactEmailId)

    /**
     * Delete all contacts for [userId].
     */
    suspend fun deleteAllContacts(userId: UserId)

    /**
     * Delete all contacts, for every user.
     */
    suspend fun deleteAllContacts()

    /**
     * Merge given [contacts] with local contacts.
     * Merge is base on matches between given contact ids and local contact ids, using following strategy:
     * - Not matched local contacts are deleted.
     * - Matched local contacts are updated to given contacts.
     * - Not matched given contacts are inserted locally.
     *
     * @throws SQLiteConstraintException if corresponding user(s) doesn't exist.
     */
    suspend fun mergeContacts(vararg contacts: Contact)
}
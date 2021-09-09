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

package me.proton.core.contact.data.repository

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import me.proton.core.contact.data.api.ContactApi
import me.proton.core.contact.data.local.db.ContactDatabase
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.network.data.ApiProvider

class ContactRepositoryImpl(
    private val provider: ApiProvider,
    private val database: ContactDatabase
) : ContactRepository {

    private data class ContactStoreKey(val userId: UserId, val contactId: String)

    private val contactStore = StoreBuilder.from(
        fetcher = Fetcher.of { key: ContactStoreKey ->
            provider.get<ContactApi>(key.userId).invoke {
                getContact(key.contactId).contact.toContact()
            }.valueOrThrow
        },
    ).build()

    private val emailStore = StoreBuilder.from(
        fetcher = Fetcher.of { userId: UserId ->
            provider.get<ContactApi>(userId).invoke {
                getContactEmails().contactEmails.map { it.toContactEmail() }
            }.valueOrThrow
        }
    ).build()

    override suspend fun getContact(sessionUserId: SessionUserId, contactId: String, refresh: Boolean): Contact {
        val key = ContactStoreKey(sessionUserId, contactId)
        return if (refresh) contactStore.fresh(key) else contactStore.get(key)
    }

    override suspend fun clearContacts(userId: UserId) = contactStore.clear(ContactStoreKey(userId, ""))

    override suspend fun clearAllContacts() = contactStore.clearAll()

    override suspend fun getContactEmails(sessionUserId: SessionUserId, refresh: Boolean): List<ContactEmail> =
        if (refresh) emailStore.fresh(sessionUserId) else emailStore.get(sessionUserId)

    override suspend fun clearContactEmails(userId: UserId) = emailStore.clear(userId)

    override suspend fun clearAllContactEmails() = emailStore.clearAll()

}

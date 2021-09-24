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
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.contact.domain.repository.ContactLocalDataSource
import me.proton.core.contact.domain.repository.ContactRemoteDataSource
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.mapSuccess
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId

class ContactRepositoryImpl(
    private val remoteDataSource: ContactRemoteDataSource,
    private val localDataSource: ContactLocalDataSource
) : ContactRepository {

    private data class ContactStoreKey(val userId: UserId, val contactId: ContactId)

    private val contactWithCardsStore: Store<ContactStoreKey, ContactWithCards> = StoreBuilder.from(
        fetcher = Fetcher.of { key: ContactStoreKey ->
            remoteDataSource.getContactWithCards(key.userId, key.contactId)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { contactStoreKey -> localDataSource.observeContact(contactStoreKey.contactId) },
            writer = { _, contactWithCards -> localDataSource.mergeContactWithCards(contactWithCards) },
            delete = { key -> localDataSource.deleteContact(key.contactId) },
            deleteAll = localDataSource::deleteAllContacts
        )
    ).build()

    private val allContactsStore: Store<UserId, List<Contact>> = StoreBuilder.from(
        fetcher = Fetcher.of { userId: UserId ->
            remoteDataSource.getAllContacts(userId)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = localDataSource::observeAllContacts,
            writer = { _, contacts -> localDataSource.mergeContacts(contacts) },
            delete = { userId -> localDataSource.deleteAllContacts(userId) },
            deleteAll = localDataSource::deleteAllContacts
        )
    ).build()

    override suspend fun getContactWithCards(
        sessionUserId: SessionUserId,
        contactId: ContactId,
        refresh: Boolean
    ): ContactWithCards {
        val key = ContactStoreKey(sessionUserId, contactId)
        return if (refresh) contactWithCardsStore.fresh(key) else contactWithCardsStore.get(key)
    }

    override fun observeAllContacts(sessionUserId: SessionUserId, refresh: Boolean): Flow<DataResult<List<Contact>>> {
        return allContactsStore.stream(StoreRequest.cached(sessionUserId, refresh)).map { it.toDataResult() }
    }

    override fun observeAllContactEmails(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<List<ContactEmail>>> {
        return observeAllContacts(sessionUserId, refresh).mapSuccess { contactsResult ->
            DataResult.Success(
                source = contactsResult.source,
                value = contactsResult.value.flatMap { it.contactEmails }
            )
        }
    }
}

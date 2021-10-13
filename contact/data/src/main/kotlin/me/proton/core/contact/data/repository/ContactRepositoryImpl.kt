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
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.contact.domain.repository.ContactLocalDataSource
import me.proton.core.contact.domain.repository.ContactRemoteDataSource
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.mapSuccess
import me.proton.core.domain.entity.UserId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val remoteDataSource: ContactRemoteDataSource,
    private val localDataSource: ContactLocalDataSource
) : ContactRepository {

    private data class ContactStoreKey(val userId: UserId, val contactId: ContactId)

    private val contactDetailFetchedOnce = mutableMapOf<ContactId, Boolean>()
    private val contactWithCardsStore: Store<ContactStoreKey, ContactWithCards> = StoreBuilder.from(
        fetcher = Fetcher.of { key: ContactStoreKey ->
            val contactWithCards = remoteDataSource.getContactWithCards(key.userId, key.contactId)
            contactDetailFetchedOnce[key.contactId] = true
            contactWithCards
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { contactStoreKey ->
                localDataSource.observeContact(contactStoreKey.contactId).map {
                    return@map it?.let { contactWithCards ->
                        contactWithCards.takeIf {
                            val fetchedOnce = contactDetailFetchedOnce[contactStoreKey.contactId] ?: false
                            contactWithCards.contactCards.isNotEmpty() && fetchedOnce
                        }
                    }
                }
            },
            writer = { _, contactWithCards -> localDataSource.upsertContactWithCards(contactWithCards) },
            delete = { key -> localDataSource.deleteContacts(key.contactId) },
            deleteAll = localDataSource::deleteAllContacts
        )
    ).build()

    private val allContactsFetchedOnce = mutableMapOf<UserId, Boolean>()
    private val allContactsStore: Store<UserId, List<Contact>> = StoreBuilder.from(
        fetcher = Fetcher.of { userId: UserId ->
            val contacts = remoteDataSource.getAllContacts(userId)
            allContactsFetchedOnce[userId] = true
            contacts
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { userId ->
                localDataSource.observeAllContacts(userId).map { contacts ->
                    val fetchedOnce = allContactsFetchedOnce[userId] ?: false
                    contacts.takeIf { it.isNotEmpty() || fetchedOnce }
                }
            },
            writer = { _, contacts -> localDataSource.mergeContacts(*contacts.toTypedArray()) },
            delete = { userId -> localDataSource.deleteAllContacts(userId) },
            deleteAll = localDataSource::deleteAllContacts
        )
    ).build()

    override fun observeContactWithCards(
        userId: UserId,
        contactId: ContactId,
        refresh: Boolean
    ): Flow<DataResult<ContactWithCards>> {
        val key = ContactStoreKey(userId, contactId)
        return contactWithCardsStore.stream(StoreRequest.cached(key, refresh)).map { it.toDataResult() }
    }

    override suspend fun getContactWithCards(
        userId: UserId,
        contactId: ContactId,
        refresh: Boolean
    ): ContactWithCards {
        val key = ContactStoreKey(userId, contactId)
        return if (refresh) contactWithCardsStore.fresh(key) else contactWithCardsStore.get(key)
    }

    override fun observeAllContacts(userId: UserId, refresh: Boolean): Flow<DataResult<List<Contact>>> {
        return allContactsStore.stream(StoreRequest.cached(userId, refresh)).map { it.toDataResult() }
    }

    override suspend fun getAllContacts(userId: UserId, refresh: Boolean): List<Contact> {
        return if (refresh) allContactsStore.fresh(userId) else allContactsStore.get(userId)
    }

    override fun observeAllContactEmails(
        userId: UserId,
        refresh: Boolean
    ): Flow<DataResult<List<ContactEmail>>> {
        return observeAllContacts(userId, refresh).mapSuccess { contactsResult ->
            DataResult.Success(
                source = contactsResult.source,
                value = contactsResult.value.flatMap { it.contactEmails }
            )
        }
    }

    override suspend fun getAllContactEmails(userId: UserId, refresh: Boolean): List<ContactEmail> {
        return getAllContacts(userId, refresh).flatMap { it.contactEmails }
    }

    override suspend fun createContact(userId: UserId, contactCards: List<ContactCard>) {
        val createdContacts = remoteDataSource.createContacts(userId, listOf(contactCards))
        check(createdContacts.count() == 1)
        val createdContact = createdContacts.first()
        localDataSource.upsertContactWithCards(ContactWithCards(contact = createdContact, contactCards = contactCards))
    }

    override suspend fun deleteContacts(userId: UserId, contactIds: List<ContactId>) {
        remoteDataSource.deleteContacts(userId, contactIds)
        localDataSource.deleteContacts(*contactIds.toTypedArray())
    }

    override suspend fun updateContact(userId: UserId, contactId: ContactId, contactCards: List<ContactCard>) {
        val updatedContact = remoteDataSource.updateContact(userId, contactId, contactCards)
        localDataSource.upsertContactWithCards(ContactWithCards(contact = updatedContact, contactCards = contactCards))
    }
}

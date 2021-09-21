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

package me.proton.core.contact.data.api

import me.proton.core.contact.data.api.request.CreateContactsRequest
import me.proton.core.contact.data.api.resource.ContactCardsResource
import me.proton.core.contact.data.api.resource.ContactEmailResource
import me.proton.core.contact.data.api.resource.ShortContactResource
import me.proton.core.contact.data.api.resource.toContactCardResource
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.contact.domain.repository.ContactRemoteDataSource
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import javax.inject.Inject

class ContactRemoteDataSourceImpl @Inject constructor(private val apiProvider: ApiProvider): ContactRemoteDataSource {

    override suspend fun getContactWithCards(userId: UserId, contactId: ContactId): ContactWithCards {
        return apiProvider.get<ContactApi>(userId).invoke {
            getContact(contactId.id).contact.toContactWithCards(userId)
        }.valueOrThrow
    }

    override suspend fun getAllContacts(userId: UserId): List<Contact> {
        val apiContacts = getAllApiContacts(userId)
        val apiContactEmails = getAllApiContactsEmails(userId)
        return apiContacts.entries.map { entry ->
            val contactEmails = (apiContactEmails[entry.key] ?: emptyList()).map { it.toContactEmail(userId) }
            Contact(
                userId = userId,
                id = entry.key,
                name = entry.value.name,
                contactEmails = contactEmails
            )
        }
    }

    private suspend fun getAllApiContacts(userId: UserId): Map<ContactId, ShortContactResource> {
        return apiProvider.get<ContactApi>(userId).invoke {
            val contacts = mutableMapOf<ContactId, ShortContactResource>()
            var pageIndex = 0
            val pageSize = 1000
            var shouldContinuePaging = true
            while (shouldContinuePaging) {
                val apiResult = getContacts(page = pageIndex, pageSize = pageSize)
                apiResult.contacts.forEach { contacts[ContactId(it.id)] = it }
                shouldContinuePaging = pageIndex < apiResult.total / pageSize
                pageIndex++
            }
            contacts
        }.valueOrThrow
    }

    private suspend fun getAllApiContactsEmails(userId: UserId): Map<ContactId, List<ContactEmailResource>> {
        return apiProvider.get<ContactApi>(userId).invoke {
            val contactEmails = mutableMapOf<ContactId, MutableList<ContactEmailResource>>()
            var pageIndex = 0
            val pageSize = 1000
            var shouldContinuePaging = true
            while (shouldContinuePaging) {
                val apiResult = getContactEmails(page = pageIndex, pageSize = pageSize)
                apiResult.contactEmails.forEach {
                    contactEmails.getOrPut(ContactId(it.contactId)) { mutableListOf() }.add(it)
                }
                shouldContinuePaging = pageIndex < apiResult.total / pageSize
                pageIndex++
            }
            contactEmails
        }.valueOrThrow
    }

    override suspend fun createContact(userId: UserId, contactCards: List<ContactCard>) {
        val request = CreateContactsRequest.create(
            contacts = listOf(ContactCardsResource(cards = contactCards.map { it.toContactCardResource() })),
            overwrite = false,
        )
        apiProvider.get<ContactApi>(userId).invoke {
            createContacts(request)
        }.valueOrThrow
    }
}

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

package me.proton.core.contact.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.contact.data.api.resource.ContactEmailResource
import me.proton.core.contact.data.local.db.ContactDatabase
import me.proton.core.contact.domain.entity.ContactEmailId
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.repository.ContactLocalDataSource
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.util.kotlin.deserializeOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ContactEmailsEvents(
    @SerialName("ContactEmails")
    val contactEmails: List<ContactEmailEvent>
)

@Serializable
data class ContactEmailEvent(
    @SerialName("ID")
    val id: String,
    @SerialName("Action")
    val action: Int,
    @SerialName("ContactEmail")
    val contactEmail: ContactEmailResource? = null
)

@Singleton
class ContactEmailEventListener @Inject constructor(
    private val db: ContactDatabase,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val contactRepository: ContactRepository,
    private val contactEventListener: ContactEventListener
) : EventListener<String, ContactEmailResource>() {

    override val type = Type.Core
    override val order = 2

    override suspend fun deserializeEvents(response: EventsResponse): List<Event<String, ContactEmailResource>>? {
        return response.body.deserializeOrNull<ContactEmailsEvents>()?.contactEmails?.map {
            Event(requireNotNull(Action.map[it.action]), it.id, it.contactEmail)
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R {
        return db.inTransaction(block)
    }

    override suspend fun onPrepare(userId: UserId, entities: List<ContactEmailResource>) {
        // Don't fetch Contacts that will be created in this set of modifications.
        val contactActions = contactEventListener.getActionMap(userId)
        val createContactIds = contactActions[Action.Create].orEmpty().map { it.key }.toHashSet()
        // Make sure we'll fetch other Contacts.
        entities.filterNot { createContactIds.contains(it.contactId) }.forEach {
            contactRepository.getContactWithCards(userId, ContactId(it.contactId), refresh = false)
        }
    }

    override suspend fun onCreate(userId: UserId, entities: List<ContactEmailResource>) {
        entities.forEach { contactLocalDataSource.upsertContactEmails(it.toContactEmail(userId)) }
    }

    override suspend fun onUpdate(userId: UserId, entities: List<ContactEmailResource>) {
        entities.forEach { contactLocalDataSource.upsertContactEmails(it.toContactEmail(userId)) }
    }

    override suspend fun onDelete(userId: UserId, keys: List<String>) {
        contactLocalDataSource.deleteContactEmails(*keys.map { ContactEmailId(it) }.toTypedArray())
    }

    override suspend fun onResetAll(userId: UserId) {
        // Already handled in ContactEventListener:
        // contactLocalDataSource.deleteAllContactEmails(userId)
        // contactRepository.getAllContactEmails(userId, refresh = true)
    }
}

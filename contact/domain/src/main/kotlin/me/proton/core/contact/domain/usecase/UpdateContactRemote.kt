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

package me.proton.core.contact.domain.usecase

import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.extension.suspend
import javax.inject.Inject

class UpdateContactRemote @Inject constructor(
    private val contactRepository: ContactRepository,
    private val eventManagerProvider: EventManagerProvider,
) {
    // Called by UpdateContactWorker (unique name: userId+contactId, ExistingWorkPolicy.APPEND_OR_REPLACE).
    // Prerequisite: Local optimistic update Contact.
    suspend operator fun invoke(userId: UserId, contactId: ContactId, contactCards: List<ContactCard>) {
        eventManagerProvider.suspend(EventManagerConfig.Core(userId)) {
            contactRepository.updateContact(userId, contactId, contactCards)
        }
    }
}

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

package me.proton.core.eventmanager.data

import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerConfigProvider
import me.proton.core.eventmanager.domain.repository.EventMetadataRepository
import javax.inject.Inject

class EventManagerConfigProviderImpl @Inject constructor(
    private val eventMetadataRepository: EventMetadataRepository
) : EventManagerConfigProvider {

    override suspend fun getAll(userId: UserId): List<EventManagerConfig> =
        eventMetadataRepository.getAll(userId).map { it.config }.toSet().toList()

    override suspend fun getAll(userId: UserId, type: EventListener.Type): List<EventManagerConfig> =
        getAll(userId).filter { it.listenerType == type }
}

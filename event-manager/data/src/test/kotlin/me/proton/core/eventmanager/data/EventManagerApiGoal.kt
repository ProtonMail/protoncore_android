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
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.extension.suspend

class EventManagerApiGoal {

    lateinit var provider: EventManagerProvider

    suspend fun onAccountReady(userId: UserId) {
        provider.get(EventManagerConfig.Core(userId)).start()
    }

    suspend fun onPerformAction(userId: UserId) {
        suspend fun callApi() = Unit
        provider.suspend(EventManagerConfig.Core(userId)) {
            callApi()
        }
    }

    suspend fun onCalendarActive(userId: UserId, calendarId: String) {
        provider.get(EventManagerConfig.Calendar(userId, calendarId)).start()
    }

    suspend fun onCalendarInactive(userId: UserId, calendarId: String) {
        provider.get(EventManagerConfig.Calendar(userId, calendarId)).stop()
    }

    suspend fun onDriveShareActive(userId: UserId, shareId: String) {
        provider.get(EventManagerConfig.Drive(userId, shareId)).start()
    }

    suspend fun onDriveShareInactive(userId: UserId, shareId: String) {
        provider.get(EventManagerConfig.Drive(userId, shareId)).stop()
    }
}

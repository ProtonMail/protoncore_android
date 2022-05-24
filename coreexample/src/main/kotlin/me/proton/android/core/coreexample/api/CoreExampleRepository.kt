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

package me.proton.android.core.coreexample.api

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.TimeoutOverride
import me.proton.core.user.data.api.UserApi

class CoreExampleRepository(
    private val provider: ApiProvider
) {

    suspend fun triggerHumanVerification(userId: UserId) =
        provider.get<CoreExampleApi>(userId).invoke {
            triggerHumanVerification()
        }

    suspend fun usernameAvailable() =
        provider.get<UserApi>().invoke {
            usernameAvailable("username")
        }.valueOrNull

    suspend fun triggerConfirmPasswordLockedScope(userId: UserId) =
        provider.get<CoreExampleApi>(userId).invoke {
            triggerConfirmPasswordLockedScope()
        }

    suspend fun triggerConfirmPasswordPasswordScope(userId: UserId) =
        provider.get<CoreExampleApi>(userId).invoke {
            triggerConfirmPasswordForPasswordScope()
        }

    suspend fun markAsRead(userId: UserId, messageId: String) =
        provider.get<CoreExampleApi>(userId).invoke {
            markAsRead(CoreExampleApi.MarkAsReadRequest(listOf(messageId)))
        }.valueOrThrow

    suspend fun ping(timeoutOverride: TimeoutOverride) =
        provider.get<CoreExampleApi>().invoke {
            ping(timeoutOverride)
        }
}

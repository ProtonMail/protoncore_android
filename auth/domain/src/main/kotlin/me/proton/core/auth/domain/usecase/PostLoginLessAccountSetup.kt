/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.domain.usecase

import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.server.ServerClock
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.USER_SERVICE_MASK_VPN
import javax.inject.Inject

class PostLoginLessAccountSetup @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val userCheck: PostLoginAccountSetup.UserCheck,
    private val userManager: UserManager,
    private val sessionManager: SessionManager,
    private val serverClock: ServerClock,
) {
    suspend operator fun invoke(
        userId: UserId,
    ): PostLoginAccountSetup.UserCheckResult {
        // Refresh scopes.
        sessionManager.refreshScopes(checkNotNull(sessionManager.getSessionId(userId)))

        // First, create the User to invoke UserCheck.
        val user = makeUser(userId)
        userManager.addUser(user, emptyList())

        val userCheckResult = userCheck.invoke(user)
        when (userCheckResult) {
            is PostLoginAccountSetup.UserCheckResult.Error -> {
                // Disable account and prevent login.
                accountWorkflow.handleAccountDisabled(userId)
            }
            is PostLoginAccountSetup.UserCheckResult.Success -> {
                // Last step, change account state to Ready.
                accountWorkflow.handleAccountReady(userId)
            }
        }
        return userCheckResult
    }

    private fun makeUser(userId: UserId): User = User(
        userId = userId,
        email = null,
        name = null,
        displayName = null,
        currency = "",
        credit = 0,
        type = Type.CredentialLess,
        createdAtUtc = serverClock.getCurrentTimeUTC().toEpochMilli(),
        usedSpace = 0,
        maxSpace = 0,
        maxUpload = 0,
        role = null,
        private = true,
        services = USER_SERVICE_MASK_VPN,
        subscribed = 0,
        delinquent = null,
        recovery = null,
        keys = emptyList(),
        flags = emptyMap(),
        maxBaseSpace = 0,
        maxDriveSpace = 0,
        usedBaseSpace = 0,
        usedDriveSpace = 0,
    )
}

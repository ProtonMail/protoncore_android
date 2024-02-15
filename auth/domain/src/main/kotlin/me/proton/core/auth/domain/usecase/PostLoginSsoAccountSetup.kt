/*
 * Copyright (c) 2023 Proton AG
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
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

/**
 * Performs the account check after SSO logging in to determine what actions are needed.
 */
class PostLoginSsoAccountSetup @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val userCheck: PostLoginAccountSetup.UserCheck,
    private val userManager: UserManager,
    private val sessionManager: SessionManager,
) {
    suspend operator fun invoke(
        userId: UserId,
    ): PostLoginAccountSetup.UserCheckResult {
        // Refresh scopes.
        sessionManager.refreshScopes(checkNotNull(sessionManager.getSessionId(userId)))
        // First get the User to invoke UserCheck.
        val user = userManager.getUser(userId, refresh = true)
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
}

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

package me.proton.core.auth.presentation

import android.content.Context
import kotlinx.coroutines.flow.first
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup.UserCheckResult
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Delinquent
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.hasSubscription
import me.proton.core.user.domain.extension.isCredentialLess
import me.proton.core.util.kotlin.coroutine.result
import javax.inject.Inject

/**
 * Check [User] succeed if:
 * - [User.delinquent] is not [Delinquent.InvoiceDelinquent] or [Delinquent.InvoiceMailDisabled].
 * - [User.hasSubscription] is false and currentFreeUserCount + 1 <= maxFreeUserCount.
 */
open class DefaultUserCheck @Inject constructor(
    private val context: Context,
    private val accountManager: AccountManager,
    private val userManager: UserManager
) : PostLoginAccountSetup.UserCheck {

    private val maxFreeUserCount: Int by lazy {
        context.resources.getInteger(R.integer.core_feature_auth_user_check_max_free_user_count)
    }

    private fun errorMaxFreeUser() = UserCheckResult.Error(
        localizedMessage = context.resources.getQuantityString(
            R.plurals.auth_user_check_max_free_error,
            maxFreeUserCount,
            maxFreeUserCount
        )
    )

    private fun errorDelinquent() = UserCheckResult.Error(
        localizedMessage = context.getString(R.string.auth_user_check_delinquent_error),
        action = UserCheckAction.OpenUrl(
            name = context.getString(R.string.auth_user_check_delinquent_action),
            url = context.getString(R.string.login_link)
        )
    )

    private suspend fun currentFreeUserCount(): Int =
        accountManager.getAccounts(AccountState.Ready).first()
            .map { userManager.getUser(it.userId) }
            .filterNot { it.isCredentialLess() }
            .fold(0) { acc, user -> if (!user.hasSubscription()) acc + 1 else acc }

    override suspend fun invoke(user: User): UserCheckResult =
        result("defaultUserCheck") {
            when {
                user.delinquent in delinquentStates -> {
                    errorDelinquent()
                }

                !user.hasSubscription() && (currentFreeUserCount() + 1 > maxFreeUserCount) -> {
                    errorMaxFreeUser()
                }

                else -> UserCheckResult.Success
            }
        }

    companion object {
        private val delinquentStates = listOf(
            Delinquent.InvoiceDelinquent,
            Delinquent.InvoiceMailDisabled
        )
    }
}

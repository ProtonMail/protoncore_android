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

package me.proton.core.auth.presentation

import android.content.Context
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.first
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.auth.domain.usecase.SetupAccountCheck
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Delinquent
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.hasSubscription
import javax.inject.Inject

/**
 * Check [User] succeed if:
 * - [User.delinquent] is not [Delinquent.InvoiceDelinquent] or [Delinquent.InvoiceMailDisabled].
 * - [User.hasSubscription] is true or all existing [Account] in [AccountState.Ready] have a subscription.
 */
class DefaultUserCheck @Inject constructor(
    private val context: Context,
    private val accountManager: AccountManager,
    private val userManager: UserManager
) : SetupAccountCheck.UserCheck {

    private fun errorMessage(@StringRes message: Int) = SetupAccountCheck.UserCheckResult.Error(
        localizedMessage = context.getString(message)
    )

    private fun errorDelinquent() = SetupAccountCheck.UserCheckResult.Error(
        localizedMessage = context.getString(R.string.auth_user_check_delinquent_error),
        action = SetupAccountCheck.Action.OpenUrl(
            name = context.getString(R.string.auth_user_check_delinquent_action),
            url = context.getString(R.string.login_link)
        )
    )

    private suspend fun allReadyHaveSubscription(): Boolean =
        accountManager.getAccounts(AccountState.Ready).first().map { it.userId }.all {
            userManager.getUser(it).hasSubscription()
        }

    override suspend fun invoke(user: User): SetupAccountCheck.UserCheckResult = when {
        user.delinquent in listOf(Delinquent.InvoiceDelinquent, Delinquent.InvoiceMailDisabled) -> {
            errorDelinquent()
        }
        !user.hasSubscription() && !allReadyHaveSubscription() -> {
            errorMessage(R.string.auth_user_check_one_free_error)
        }
        else -> SetupAccountCheck.UserCheckResult.Success
    }
}

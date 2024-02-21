/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.accountmanager.presentation.compose.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.getDisplayName
import me.proton.core.user.domain.extension.getEmail
import me.proton.core.user.domain.extension.getInitials
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    accountManager: AccountManager,
    private val userManager: UserManager,
    override val telemetryManager: TelemetryManager
) : ProtonViewModel(), ProductMetricsDelegate {

    override val productGroup: String
        get() = "account.any.signup"
    override val productFlow: String
        get() = "mobile_signup_full"

    val initialState = AccountSettingsViewState.Hidden

    val state: StateFlow<AccountSettingsViewState> = accountManager.getPrimaryAccount()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it.userId) }
        .map { it.toAccountSettingsViewState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = initialState
        )
}

sealed class AccountSettingsViewState {
    object Hidden : AccountSettingsViewState()
    data class CredentialLess(
        val userId: UserId
    ) : AccountSettingsViewState()

    data class LoggedIn(
        val userId: UserId,
        val initials: String?,
        val displayName: String?,
        val email: String?
    ) : AccountSettingsViewState()

}

private fun User?.toAccountSettingsViewState(): AccountSettingsViewState = when {
    this == null -> AccountSettingsViewState.Hidden
    type == Type.CredentialLess -> AccountSettingsViewState.CredentialLess(userId)
    else -> AccountSettingsViewState.LoggedIn(
        userId = userId,
        initials = getInitials(count = 2),
        displayName = getDisplayName(),
        email = getEmail()
    )
}

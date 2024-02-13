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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.getInitials
import me.proton.core.util.kotlin.takeIfNotBlank
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    accountManager: AccountManager,
    private val userManager: UserManager
) : ProtonViewModel() {
    val state: StateFlow<AccountSettingsViewState> =
        accountManager.getPrimaryAccount()
            .filterNotNull()
            .map { userManager.getUser(it.userId) }
            .map { it.toAccountSettingsViewState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), INITIAL_STATE)

    internal companion object {
        val INITIAL_STATE = AccountSettingsViewState.Hidden
    }
}

sealed class AccountSettingsViewState {
    object Hidden : AccountSettingsViewState()
    data class CredentialLess(
        val userId: UserId
    ) : AccountSettingsViewState()

    data class LoggedIn(
        val userId: UserId,
        val shortName: String?,
        val displayName: String?,
        val email: String?
    ) : AccountSettingsViewState()

}

private fun User.toAccountSettingsViewState(): AccountSettingsViewState = when (type) {
    Type.CredentialLess -> AccountSettingsViewState.CredentialLess(userId)
    Type.Proton,
    Type.Managed,
    Type.External,
    null -> AccountSettingsViewState.LoggedIn(userId, getInitials(), displayName, email)
}

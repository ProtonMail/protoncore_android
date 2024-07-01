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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserRecovery
import me.proton.core.user.domain.extension.getDisplayName
import me.proton.core.user.domain.extension.getEmail
import me.proton.core.user.domain.extension.getInitials
import me.proton.core.user.domain.usecase.ObserveUser
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    accountManager: AccountManager,
    private val isFido2Enabled: IsFido2Enabled,
    private val observeUser: ObserveUser,
    private val observeUserSettings: ObserveUserSettings,
    override val telemetryManager: TelemetryManager,
) : ProtonViewModel(), ProductMetricsDelegate {

    override val productGroup: String
        get() = "account.any.signup"
    override val productFlow: String
        get() = "mobile_signup_full"

    val initialState = AccountSettingsViewState.Hidden

    val state: StateFlow<AccountSettingsViewState> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                observeUser(userId),
                observeUserSettings(userId)
            ) { user, settings ->
                user.toAccountSettingsViewState(
                    isFido2Enabled = lazy { isFido2Enabled(userId) },
                    settings
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = initialState
        )
}

sealed class AccountSettingsViewState {
    data object Hidden : AccountSettingsViewState()
    data class CredentialLess(
        val userId: UserId
    ) : AccountSettingsViewState()

    data class LoggedIn(
        val userId: UserId,
        val initials: String? = null,
        val displayName: String? = null,
        val email: String? = null,
        val recoveryState: UserRecovery.State? = null,
        val recoveryEmail: String? = null,
        val registeredSecurityKeys: List<Fido2RegisteredKey> = emptyList(),
        val securityKeysVisible: Boolean = false
    ) : AccountSettingsViewState()

    companion object {
        val Null = LoggedIn(
            userId = UserId("userId"),
            initials = "DU",
            displayName = "Display Name",
            email = "example@proton.me",
            recoveryState = UserRecovery.State.Grace,
            recoveryEmail = "example@domain.com",
            registeredSecurityKeys = emptyList()
        )
    }
}

private fun User?.toAccountSettingsViewState(
    isFido2Enabled: Lazy<Boolean>,
    settings: UserSettings?
): AccountSettingsViewState = when {
    this == null -> AccountSettingsViewState.Hidden
    type == Type.CredentialLess -> AccountSettingsViewState.CredentialLess(userId)
    else -> AccountSettingsViewState.LoggedIn(
        userId = userId,
        initials = getInitials(count = 2),
        displayName = getDisplayName(),
        email = getEmail(),
        recoveryState = recovery?.state?.enum,
        recoveryEmail = settings?.email?.value,
        registeredSecurityKeys = settings?.twoFA?.registeredKeys ?: emptyList(),
        securityKeysVisible = isFido2Enabled.value
    )
}

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

package me.proton.core.account.domain.entity

import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId

data class Account(
    val userId: UserId,
    val username: String?,
    val email: String?,
    val state: AccountState,
    val sessionId: SessionId?,
    val sessionState: SessionState?,
    val details: AccountDetails
)

data class AccountDetails(
    val account: AccountMetadataDetails? = null,
    val session: SessionDetails?
)

data class AccountMetadataDetails(
    val primaryAtUtc: Long,
    val migrations: List<String>
)

data class SessionDetails(
    val initialEventId: String,
    val requiredAccountType: AccountType,
    val secondFactorEnabled: Boolean,
    val twoPassModeEnabled: Boolean,
    val password: EncryptedString?,
    val fido2AuthenticationOptions: Fido2AuthenticationOptions?
)

fun Account.isReady() = state == AccountState.Ready
fun Account.isDisabled() = state == AccountState.Disabled

fun Account.isTwoPassModeNeeded() = state == AccountState.TwoPassModeNeeded
fun Account.isCreateAddressNeeded() = state == AccountState.CreateAddressNeeded

fun Account.isAuthenticated() = sessionState == SessionState.Authenticated
fun Account.isSecondFactorNeeded() = sessionState == SessionState.SecondFactorNeeded

fun Account.isStepNeeded(): Boolean {
    val isAccountStateStepNeeded = when (state) {
        AccountState.MigrationNeeded,
        AccountState.TwoPassModeNeeded,
        AccountState.CreateAddressNeeded,
        AccountState.CreateAccountNeeded -> true
        AccountState.NotReady,
        AccountState.TwoPassModeSuccess,
        AccountState.TwoPassModeFailed,
        AccountState.CreateAddressSuccess,
        AccountState.CreateAddressFailed,
        AccountState.CreateAccountSuccess,
        AccountState.CreateAccountFailed,
        AccountState.Ready,
        AccountState.Disabled,
        AccountState.UnlockFailed,
        AccountState.UserKeyCheckFailed,
        AccountState.UserAddressKeyCheckFailed,
        AccountState.Removed -> false
    }
    val isSessionStateStepNeeded = when (sessionState) {
        SessionState.SecondFactorNeeded -> true
        SessionState.SecondFactorSuccess,
        SessionState.SecondFactorFailed,
        SessionState.Authenticated,
        SessionState.ForceLogout,
        null -> false
    }
    return isAccountStateStepNeeded || isSessionStateStepNeeded
}

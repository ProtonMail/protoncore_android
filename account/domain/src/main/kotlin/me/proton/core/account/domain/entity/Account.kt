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
    val username: String,
    val email: String?,
    val state: AccountState,
    val sessionId: SessionId?,
    val sessionState: SessionState?,
    val details: AccountDetails
)

data class AccountDetails(
    val session: SessionDetails?
)

data class SessionDetails(
    val initialEventId: String,
    val requiredAccountType: AccountType,
    val secondFactorEnabled: Boolean,
    val twoPassModeEnabled: Boolean,
    val password: EncryptedString?
)

fun Account.isReady() = state == AccountState.Ready
fun Account.isDisabled() = state == AccountState.Disabled
fun Account.isTwoPassModeNeeded() = state == AccountState.TwoPassModeNeeded

fun Account.isAuthenticated() = sessionState == SessionState.Authenticated
fun Account.isSecondFactorNeeded() = sessionState == SessionState.SecondFactorNeeded

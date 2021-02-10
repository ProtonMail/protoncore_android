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

package me.proton.core.auth.domain

import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId

/**
 * Handler for all Account/Authentication Workflow, like SecondFactor or HumanVerification.
 *
 * Note: Implementation of [AccountManager] should probably also implement this one.
 */
interface AccountWorkflowHandler {
    /**
     * Handle a new [Session] for a new or existing [Account] from Login workflow.
     */
    suspend fun handleSession(account: Account, session: Session)

    /**
     * Handle TwoPassMode success.
     */
    suspend fun handleTwoPassModeSuccess(userId: UserId)

    /**
     * Handle TwoPassMode failure.
     *
     * Note: The Workflow must succeed within maximum 10 min of authentication.
     */
    suspend fun handleTwoPassModeFailed(userId: UserId)

    /**
     * Handle SecondFactor success.
     *
     * @param updatedScopes the new updated full list of scopes.
     */
    suspend fun handleSecondFactorSuccess(sessionId: SessionId, updatedScopes: List<String>)

    /**
     * Handle SecondFactor failure.
     *
     * Note: Maximum number of failure is 3, then the session will be invalidated and API will return HTTP 401.
     */
    suspend fun handleSecondFactorFailed(sessionId: SessionId)

    /**
     * Handle HumanVerification success.
     *
     * Note: TokenType and tokenCode must be part of the next API calls.
     */
    suspend fun handleHumanVerificationSuccess(sessionId: SessionId, tokenType: String, tokenCode: String)

    /**
     * Handle HumanVerification failure.
     */
    suspend fun handleHumanVerificationFailed(sessionId: SessionId)

    /**
     * Directly sets the account into a [AccountState.ChangePasswordNeeded] state.
     */
    suspend fun handleAccountChangePasswordNeeded(userId: UserId)

    /**
     * Directly sets the account into a [AccountState.CreateAddressNeeded] state.
     */
    suspend fun handleAccountCreateAddressNeeded(userId: UserId)

    /**
     * Handle Create Address success.
     */
    suspend fun handleAccountCreateAddressSuccess(userId: UserId)

    /**
     * Handle Create Address failure.
     */
    suspend fun handleAccountCreateAddressFailed(userId: UserId)

    /**
     * Handle Unlock failure.
     */
    suspend fun handleAccountUnlockFailed(userId: UserId)

    /**
     * Directly sets the account into a [AccountState.Ready] state.
     */
    suspend fun handleAccountReady(userId: UserId)

    /**
     * Directly sets the account into a [AccountState.NotReady] state.
     */
    suspend fun handleAccountNotReady(userId: UserId)
}

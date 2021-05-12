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

package me.proton.core.humanverification.data

import kotlinx.coroutines.flow.Flow
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.session.ClientId
import me.proton.core.network.domain.session.HumanVerificationProvider

class HumanVerificationManagerImpl(
    private val repository: HumanVerificationRepository
) : HumanVerificationManager, HumanVerificationProvider, HumanVerificationWorkflowHandler {
    /**
     * @param initialState if true, initial state for all accounts will be raised on subscription.
     */
    override fun onHumanVerificationStateChanged(initialState: Boolean): Flow<HumanVerificationDetails> =
        repository.onHumanVerificationStateChanged(initialState = initialState)

    /**
     * Handle HumanVerification success.
     *
     * Note: TokenType and tokenCode must be part of the next API call (as a request headers).
     */
    override suspend fun handleHumanVerificationSuccess(clientId: ClientId, tokenType: String, tokenCode: String) {
        repository.updateHumanVerificationState(
            clientId,
            HumanVerificationState.HumanVerificationSuccess,
            tokenType,
            tokenCode
        )
    }

    /**
     * Handle HumanVerification failure.
     */
    override suspend fun handleHumanVerificationFailed(clientId: ClientId) {
        repository.updateHumanVerificationState(clientId, HumanVerificationState.HumanVerificationFailed)
    }

    /**
     * Handle HumanVerification canceled by the user.
     * Basically this will remove the database entry.
     */
    override suspend fun handleHumanVerificationCanceled(clientId: ClientId) {
        repository.updateHumanVerificationCompleted(clientId)
    }

    /**
     * Get [HumanVerificationDetails], by clientId.
     */
    override suspend fun getHumanVerificationDetails(clientId: ClientId): HumanVerificationDetails? =
        repository.getHumanVerificationDetails(clientId)
}

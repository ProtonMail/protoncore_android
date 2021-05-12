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

package me.proton.core.humanverification.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.session.ClientId

interface HumanVerificationRepository {

    /**
     * Get [HumanVerificationDetails], if exist, by sessionId.
     */
    suspend fun getHumanVerificationDetails(clientId: ClientId): HumanVerificationDetails?

    /**
     * Gets all [HumanVerificationDetails] items from the table.
     */
    suspend fun getAllHumanVerificationDetails(): Flow<List<HumanVerificationDetails>>

    /**
     * Set [HumanVerificationDetails], by clientId. Basically it inserts new record in the DB.
     */
    suspend fun insertHumanVerificationDetails(details: HumanVerificationDetails)

    /**
     * Marks the human verification details as completed (successfully).
     */
    suspend fun updateHumanVerificationCompleted(clientId: ClientId)

    /**
     * Clears the table.
     */
    suspend fun clear()

    /**
     * Sets new state for a human verification flow, along with the token type and token code if needed.
     */
    suspend fun updateHumanVerificationState(
        clientId: ClientId,
        state: HumanVerificationState,
        tokenType: String? = null,
        tokenCode: String? = null
    )

    fun onHumanVerificationStateChanged(initialState: Boolean): Flow<HumanVerificationDetails>
}

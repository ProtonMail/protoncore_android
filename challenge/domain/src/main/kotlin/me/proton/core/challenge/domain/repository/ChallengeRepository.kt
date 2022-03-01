/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.challenge.domain.repository

import me.proton.core.challenge.domain.ChallengeId
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.network.domain.client.ClientId

interface ChallengeRepository {

    /**
     * Get a list of [ChallengeFrameDetails] if exist, by clientId.
     */
    suspend fun getFramesByClientId(clientId: ClientId): List<ChallengeFrameDetails>?

    /**
     * Get a list of [ChallengeFrameDetails] if exist, by challengeId.
     */
    suspend fun getFramesByChallengeId(challengeId: ChallengeId): List<ChallengeFrameDetails>?

    /**
     * Get a list of [ChallengeFrameDetails] if exist, by clientId and challengeId.
     */
    suspend fun getFramesByClientAndChallengeId(
        clientId: ClientId,
        challengeId: ChallengeId
    ): List<ChallengeFrameDetails>?

    /**
     * Insert new [ChallengeFrameDetails].
     */
    suspend fun insertFrameDetails(challengeFrameDetails: ChallengeFrameDetails)

    /**
     * Delete all [ChallengeFrameDetails] by clientId and challengeId.
     */
    suspend fun deleteFrames(clientId: ClientId)

    /**
     * Delete all [ChallengeFrameDetails].
     */
    suspend fun deleteFrames()

    /**
     * Update existing [ChallengeFrameDetails] by clientId and challengeId.
     */
    suspend fun updateFrame(
        clientId: ClientId,
        challengeId: ChallengeId,
        challengeFrameDetails: ChallengeFrameDetails
    )
}

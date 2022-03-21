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

import me.proton.core.challenge.domain.entity.ChallengeFrameDetails

interface ChallengeRepository {

    /**
     * Get a [ChallengeFrameDetails] list if exist, by flow name.
     */
    suspend fun getFramesByFlow(flow: String): List<ChallengeFrameDetails>?

    /**
     * Get a [ChallengeFrameDetails] if exist, by flow and frame name.
     */
    suspend fun getFramesByFlowAndFrame(flow: String, frame: String): ChallengeFrameDetails?

    /**
     * Insert new [ChallengeFrameDetails].
     */
    suspend fun insertFrameDetails(challengeFrameDetails: ChallengeFrameDetails)

    /**
     * Delete all [ChallengeFrameDetails] by flow name.
     */
    suspend fun deleteFrames(flow: String)

    /**
     * Delete all [ChallengeFrameDetails].
     */
    suspend fun deleteFrames()
}

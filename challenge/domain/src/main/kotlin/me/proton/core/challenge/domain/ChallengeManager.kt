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

package me.proton.core.challenge.domain

import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.network.domain.client.ClientId
import java.util.UUID

typealias ChallengeId = UUID

interface ChallengeManager {

    suspend fun addOrUpdateFrame(
        challengeType: ChallengeFrameType,
        focusTime: Long, // milliseconds
        clicks: Int,
        copies: List<String>,
        pastes: List<String>,
        keys: List<Char>
    )

    suspend fun removeFrames()

    suspend fun getFramesByClientId(clientId: ClientId): List<ChallengeFrameDetails>

    suspend fun getFramesByChallengeId(challengeId: ChallengeId): List<ChallengeFrameDetails>

    suspend fun getFramesByClientIdAndChallengeId(clientId: ClientId, challengeId: ChallengeId): List<ChallengeFrameDetails>
}

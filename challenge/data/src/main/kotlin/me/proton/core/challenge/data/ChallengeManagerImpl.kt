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

package me.proton.core.challenge.data

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import me.proton.core.challenge.domain.ChallengeFrameType
import me.proton.core.challenge.domain.ChallengeId
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.challenge.domain.repository.ChallengeRepository
import me.proton.core.network.domain.client.ClientId

@AssistedFactory
interface ChallengeManagerFactory {
    fun create(id: ChallengeId, clientId: ClientId): ChallengeManagerImpl
}

class ChallengeManagerImpl @AssistedInject constructor(
    private val challengeRepository: ChallengeRepository,
    @Assisted val id: ChallengeId,
    @Assisted val clientId: ClientId
) : ChallengeManager {

    override suspend fun addOrUpdateFrame(
        challengeType: ChallengeFrameType,
        focusTime: Long,
        clicks: Int,
        copies: List<String>,
        pastes: List<String>,
        keys: List<Char>
    ) {
        val frame = ChallengeFrameDetails(
            clientId = clientId,
            challengeId = id,
            challengeTypeChallenge = challengeType,
            focusTime = focusTime,
            clicks = clicks,
            copy = copies,
            paste = pastes,
            keys = keys
        )
        challengeRepository.insertFrameDetails(frame)
    }

    override suspend fun removeFrames() = challengeRepository.deleteFrames(clientId)

    override suspend fun getFramesByClientId(clientId: ClientId): List<ChallengeFrameDetails> =
        challengeRepository.getFramesByClientId(clientId) ?: emptyList()

    override suspend fun getFramesByChallengeId(challengeId: ChallengeId): List<ChallengeFrameDetails> =
        challengeRepository.getFramesByChallengeId(challengeId) ?: emptyList()

    override suspend fun getFramesByClientIdAndChallengeId(
        clientId: ClientId,
        challengeId: ChallengeId
    ): List<ChallengeFrameDetails> =
        challengeRepository.getFramesByClientAndChallengeId(clientId, challengeId) ?: emptyList()
}

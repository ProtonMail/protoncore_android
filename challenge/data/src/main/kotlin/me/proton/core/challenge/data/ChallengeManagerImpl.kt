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

import dagger.assisted.AssistedInject
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.challenge.domain.repository.ChallengeRepository
import me.proton.core.network.domain.client.ClientId

class ChallengeManagerImpl @AssistedInject constructor(
    private val challengeRepository: ChallengeRepository
) : ChallengeManager {
    override suspend fun startNewFlow(clientId: ClientId, flow: String) {
        challengeRepository.deleteFrames(clientId, flow)
    }

    override suspend fun finishFlow(clientId: ClientId, flow: String) {
        challengeRepository.deleteFrames(clientId, flow)
    }

    override suspend fun addOrUpdateFrameToFlow(
        clientId: ClientId,
        flow: String,
        challenge: String,
        focusTime: Long,
        clicks: Int,
        copies: List<String>,
        pastes: List<String>,
        keys: List<Char>
    ) {
        val frame = ChallengeFrameDetails(
            flow = flow,
            clientId = clientId,
            challengeFrame = challenge,
            focusTime = focusTime,
            clicks = clicks,
            copy = copies,
            paste = pastes,
            keys = keys
        )
        challengeRepository.insertFrameDetails(frame)
    }

    override suspend fun getFramesByFlowName(clientId: ClientId, flow: String): List<ChallengeFrameDetails> =
        challengeRepository.getFramesByClientIdAndFlow(clientId, flow) ?: emptyList()

    override suspend fun getFrameByFrameName(clientId: ClientId, frame: String): ChallengeFrameDetails? =
        challengeRepository.getFramesByClientIdAndFrame(clientId, frame)
}

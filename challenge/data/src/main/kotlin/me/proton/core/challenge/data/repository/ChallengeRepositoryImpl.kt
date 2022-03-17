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

package me.proton.core.challenge.data.repository

import me.proton.core.challenge.data.db.ChallengeDatabase
import me.proton.core.challenge.data.entity.ChallengeFrameEntity
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.challenge.domain.repository.ChallengeRepository
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.getType

class ChallengeRepositoryImpl(
    private val db: ChallengeDatabase
) : ChallengeRepository {

    private val challengeDao = db.challengeFramesDao()

    override suspend fun getFramesByClientId(clientId: ClientId): List<ChallengeFrameDetails>? =
        challengeDao.getByClientId(clientId.id)?.map { it.toFrameDetails() }

    override suspend fun getFramesByClientIdAndFrame(clientId: ClientId, frame: String): ChallengeFrameDetails? =
        challengeDao.getByClientIdAndFrame(clientId.id, frame)?.toFrameDetails()

    override suspend fun getFramesByClientIdAndFlow(clientId: ClientId, flow: String): List<ChallengeFrameDetails>? =
        challengeDao.getByClientIdAndFlow(clientId.id, flow)?.map { it.toFrameDetails() }

    override suspend fun insertFrameDetails(challengeFrameDetails: ChallengeFrameDetails) {
        val clientId = challengeFrameDetails.clientId
        db.inTransaction {
            val savedFrame = challengeDao.getByClientIdAndFrame(clientId.id, challengeFrameDetails.challengeFrame)
            val currentFocus = savedFrame?.focusTime ?: 0
            challengeDao.insertOrUpdate(
                ChallengeFrameEntity(
                    clientId = clientId.id,
                    clientIdType = clientId.getType(),
                    challengeFrame = challengeFrameDetails.challengeFrame,
                    flow = challengeFrameDetails.flow,
                    focusTime = currentFocus + challengeFrameDetails.focusTime,
                    clicks = challengeFrameDetails.clicks,
                    copy = challengeFrameDetails.copy,
                    paste = challengeFrameDetails.paste,
                    keys = challengeFrameDetails.keys
                )
            )
        }
    }

    override suspend fun deleteFrames(clientId: ClientId, flow: String) =
        challengeDao.deleteByClientIdAndFlow(clientId.id, flow)

    override suspend fun deleteFrames() {
        challengeDao.deleteAll()
    }

    override suspend fun updateFrame(
        clientId: ClientId,
        challengeFrameDetails: ChallengeFrameDetails
    ) {
        db.inTransaction {
            challengeDao.updateFrame(
                clientId.id,
                challengeFrameDetails.focusTime,
                challengeFrameDetails.clicks,
                challengeFrameDetails.copy,
                challengeFrameDetails.paste
            )
        }
    }
}

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
import javax.inject.Inject

public class ChallengeRepositoryImpl @Inject constructor(
    private val db: ChallengeDatabase
) : ChallengeRepository {

    private val challengeDao = db.challengeFramesDao()

    override suspend fun getFramesByFlow(flow: String): List<ChallengeFrameDetails>? =
        challengeDao.getByFlow(flow)?.map { it.toFrameDetails() }

    override suspend fun getFramesByFlowAndFrame(flow: String, frame: String): ChallengeFrameDetails? =
        challengeDao.getByFlowAndFrame(flow = flow, frame = frame)?.toFrameDetails()

    override suspend fun insertFrameDetails(challengeFrameDetails: ChallengeFrameDetails) {
        db.inTransaction {
            val frameDetails = ChallengeFrameEntity(
                challengeFrame = challengeFrameDetails.challengeFrame,
                flow = challengeFrameDetails.flow,
                focusTime = challengeFrameDetails.focusTime,
                clicks = challengeFrameDetails.clicks,
                copy = challengeFrameDetails.copy,
                paste = challengeFrameDetails.paste,
                keys = challengeFrameDetails.keys
            )
            challengeDao.insertOrUpdate(frameDetails)
        }
    }

    override suspend fun deleteFrames(flow: String) {
        challengeDao.deleteByFlow(flow)
    }

    override suspend fun deleteFrames() {
        challengeDao.deleteAll()
    }
}

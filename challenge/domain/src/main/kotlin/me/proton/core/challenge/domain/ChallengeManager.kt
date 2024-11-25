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

public interface ChallengeManager {

    public suspend fun resetFlow(flow: String)

    public suspend fun addOrUpdateFrameToFlow(frame: ChallengeFrameDetails)

    public suspend fun getFramesByFlowName(flow: String): List<ChallengeFrameDetails>

    public suspend fun getFrameByFlowAndFrameName(flow: String, frame: String): ChallengeFrameDetails?
}

public suspend inline fun <R> ChallengeManager.useFlow(
    flowName: String,
    block: (List<ChallengeFrameDetails>) -> R,
): R {
    val frames = getFramesByFlowName(flowName)
    val result = block(frames)
    resetFlow(flowName)
    return result
}

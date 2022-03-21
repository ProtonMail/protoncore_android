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

package me.proton.core.challenge.data.api

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.challenge.domain.ChallengeFrameType
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails

const val CHALLENGE_VERSION = "2.0.0"

@Serializable
data class Payload(
    @SerialName("android-challenge-0")
    val usernameFrame: UsernameFrame?,
    @SerialName("android-challenge-1")
    val recoveryFrame: RecoveryFrame?
) {
    companion object {
        fun createFromFrames(context: Context, frames: List<ChallengeFrameDetails>): Payload =
            Payload(
                UsernameFrame.from(
                    context,
                    frames.firstOrNull { it.challengeTypeChallenge == ChallengeFrameType.Username }
                ),
                RecoveryFrame.from(
                    context,
                    frames.firstOrNull { it.challengeTypeChallenge == ChallengeFrameType.Recovery }
                )
            )
    }
}

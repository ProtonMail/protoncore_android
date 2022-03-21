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

package me.proton.core.challenge.data.entity

import androidx.room.Entity
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdType
import me.proton.core.network.domain.client.CookieSessionId
import me.proton.core.network.domain.session.SessionId

@Entity(
    primaryKeys = ["challengeFrame"]
)
data class ChallengeFrameEntity(
    val challengeFrame: String,
    val flow: String,
    val focusTime: Long,
    val clicks: Int,
    val copy: List<String>,
    val paste: List<String>,
    val keys: List<Char>
) {
    fun toFrameDetails() = ChallengeFrameDetails(
        flow = flow,
        challengeFrame = challengeFrame,
        focusTime = focusTime,
        clicks = clicks,
        copy = copy,
        paste = paste,
        keys = keys
    )
}

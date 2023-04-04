/*
 * Copyright (c) 2023 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.network.domain.deviceverification

import me.proton.core.network.domain.session.SessionId

interface DeviceVerificationProvider {

    /**
     * Get the solved challenge associated with the given session ID.
     *
     * @param sessionId The session ID to look up.
     * @return The solved challenge, or null if not found.
     */
    suspend fun getSolvedChallenge(sessionId: SessionId?): String?

    /**
     * Get the solved challenge associated with the given challenge payload.
     *
     * @param challengePayload The challenge payload to look up.
     * @return The solved challenge, or null if not found.
     */
    suspend fun getSolvedChallenge(challengePayload: String): String?

    /**
     * Set the solved challenge for the given session ID and challenge payload.
     *
     * @param sessionId The session ID to associate with the solved challenge.
     * @param challengePayload The challenge payload to associate with the solved challenge.
     * @param solved The solved challenge.
     */
    suspend fun setSolvedChallenge(sessionId: SessionId, challengePayload: String, solved: String)
}

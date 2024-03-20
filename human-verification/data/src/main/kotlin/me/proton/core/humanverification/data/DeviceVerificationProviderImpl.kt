/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.humanverification.data

import me.proton.core.network.domain.deviceverification.DeviceVerificationProvider
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject
import io.github.reactivecircus.cache4k.Cache
import me.proton.core.util.android.datetime.Monotonic
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

internal val expireAfterWrite = 3.minutes

/**
 * An implementation of the DeviceVerificationProvider interface.
 */
class DeviceVerificationProviderImpl @Inject constructor(
    @Monotonic timeSource: TimeSource
) : DeviceVerificationProvider {

    // Cache for storing session IDs and their corresponding solved challenges.
    private val sessionCache = Cache.Builder()
        .expireAfterWrite(expireAfterWrite)
        .timeSource(timeSource)
        .build<SessionId, String>()

    // Cache for storing challenge payloads and their corresponding solved challenges.
    private val solvedCache = Cache.Builder()
        .expireAfterWrite(expireAfterWrite)
        .timeSource(timeSource)
        .build<String, String>()

    /**
     * Get the solved challenge associated with the given session ID.
     *
     * @param sessionId The session ID to look up.
     * @return The solved challenge, or null if not found.
     */
    override suspend fun getSolvedChallenge(sessionId: SessionId?): String? {
        sessionId ?: return null
        return sessionCache.get(sessionId)
    }

    /**
     * Get the solved challenge associated with the given challenge payload.
     *
     * @param challengePayload The challenge payload to look up.
     * @return The solved challenge, or null if not found.
     */
    override suspend fun getSolvedChallenge(challengePayload: String): String? {
        return solvedCache.get(challengePayload)
    }

    /**
     * Set the solved challenge for the given session ID and challenge payload.
     *
     * @param sessionId The session ID to associate with the solved challenge.
     * @param challengePayload The challenge payload to associate with the solved challenge.
     * @param solved The solved challenge.
     */
    override suspend fun setSolvedChallenge(
        sessionId: SessionId,
        challengePayload: String,
        solved: String
    ) {
        sessionCache.put(sessionId, solved)
        solvedCache.put(challengePayload, solved)
    }
}

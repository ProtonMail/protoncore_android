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

import kotlinx.coroutines.test.runTest
import me.proton.core.network.domain.session.SessionId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.TestTimeSource

class DeviceVerificationProviderImplTest {
    private lateinit var timeSource: TestTimeSource
    private lateinit var tested: DeviceVerificationProviderImpl

    @BeforeTest
    fun setUp() {
        timeSource = TestTimeSource()
        tested = DeviceVerificationProviderImpl(timeSource)
    }

    @Test
    fun `no solved challenge`() = runTest {
        assertNull(tested.getSolvedChallenge(sessionId = null))
        assertNull(tested.getSolvedChallenge(SessionId("session_id")))
        assertNull(tested.getSolvedChallenge("payload"))
    }

    @Test
    fun `store solved challenge`() = runTest {
        // GIVEN
        val sessionId = SessionId("session_id")
        val challenge = "challenge"
        val solved = "solved"

        // WHEN
        tested.setSolvedChallenge(sessionId, challenge, solved)

        // THEN
        assertEquals(solved, tested.getSolvedChallenge(sessionId))
        assertEquals(solved, tested.getSolvedChallenge(challenge))
        assertNull(tested.getSolvedChallenge("other-challenge"))

        // WHEN
        timeSource.plusAssign(expireAfterWrite)

        // THEN
        assertNull(tested.getSolvedChallenge(sessionId))
        assertNull(tested.getSolvedChallenge(challenge))
        assertNull(tested.getSolvedChallenge("other-challenge"))
    }
}

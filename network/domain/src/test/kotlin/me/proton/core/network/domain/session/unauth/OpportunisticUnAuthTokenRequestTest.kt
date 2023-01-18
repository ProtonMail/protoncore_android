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

package me.proton.core.network.domain.session.unauth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.network.domain.UnAuthSessionsRepository
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import org.junit.Before
import org.junit.Test

class OpportunisticUnAuthTokenRequestTest {

    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val sessionListener = mockk<SessionListener>(relaxed = true)
    private val repository = mockk<UnAuthSessionsRepository>(relaxed = true)

    private lateinit var usecase: OpportunisticUnAuthTokenRequest

    @Before
    fun beforeEveryTest() {
        usecase = OpportunisticUnAuthTokenRequest(sessionProvider, sessionListener, repository)
    }

    @Test
    fun `no saved sessions calls request token`() = runTest {
        // GIVEN
        coEvery { sessionProvider.getSessions() } returns emptyList()
        // WHEN
        usecase()
        // THEN
        coVerify(exactly = 1) {
            sessionProvider.getSessions()
            repository.requestToken()
            sessionListener.onSessionTokenCreated(any())
        }
    }

    @Test
    fun `saved sessions does not call request token`() = runTest {
        // GIVEN
        coEvery { sessionProvider.getSessions() } returns listOf(mockk())
        // WHEN
        usecase()
        // THEN
        coVerify(exactly = 1) {
            sessionProvider.getSessions()
        }
        coVerify(exactly = 0) {
            repository.requestToken()
            sessionListener.onSessionTokenCreated(any())
        }
    }
}

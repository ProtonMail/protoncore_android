/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.entity.SecondFactor
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

/**
 * @author Dino Kadrikj.
 */
class PerformLoginSuccessApiResultsTest {

    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val srpCrypto = mockk<SrpCrypto>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    private val challengeManager = mockk<ChallengeManager>(relaxed = true)

    // endregion
    // region test data
    private val testUsername = "test-username"
    private val testPassword = "test-password"

    private val testModulus = "test-modulus"
    private val testEphemeral = "test-ephemeral"
    private val testSalt = "test-salt"
    private val testSrpSession = "test-srpSession"
    private val testVersion = 1

    private val testSrpProofs = SrpProofs(
        clientEphemeral = "test-clientEphemeral",
        clientProof = "test-clientProof",
        expectedServerProof = "test-expectedServerProof",
    )

    private val loginInfoResult = AuthInfo(
        username = testUsername, modulus = testModulus, serverEphemeral = testEphemeral, version = testVersion,
        salt = testSalt, srpSession = testSrpSession, secondFactor = null
    )
    private val sessionInfoResult = SessionInfo(
        username = testUsername,
        accessToken = "",
        tokenType = "",
        scopes = emptyList(),
        sessionId = SessionId(""),
        userId = UserId(""),
        refreshToken = "",
        eventId = "",
        serverProof = "",
        localId = 1,
        passwordMode = 1,
        secondFactor = null,
        temporaryPassword = false,
    )

    private val loginChallengeConfig = LoginChallengeConfig()
    private lateinit var useCase: PerformLogin

    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase =
            PerformLogin(authRepository, srpCrypto, keyStoreCrypto, challengeManager, loginChallengeConfig, mockk())
        every {
            srpCrypto.generateSrpProofs(any(), any(), any(), any(), any(), any())
        } returns testSrpProofs
        every { keyStoreCrypto.decrypt(any<String>()) } returns testPassword
        every { keyStoreCrypto.encrypt(any<String>()) } returns testPassword
        coEvery { authRepository.getAuthInfo(null, testUsername) } returns loginInfoResult
        coEvery { authRepository.performLogin(any(), any(), any(), any()) } returns sessionInfoResult
    }

    @Test
    fun `login happy path invocations works correctly`() = runTest {
        coEvery { challengeManager.getFramesByFlowName(loginChallengeConfig.flowName) } returns emptyList()
        useCase.invoke(testUsername, testPassword)

        coVerify { authRepository.getAuthInfo(null, testUsername) }
        coVerify(exactly = 1) {
            authRepository.performLogin(
                testUsername,
                testSrpProofs,
                testSrpSession,
                frames = emptyList()
            )
        }
        verify(exactly = 1) {
            srpCrypto.generateSrpProofs(
                testUsername,
                any(), // testPassword.toByteArray(),
                loginInfoResult.version.toLong(),
                loginInfoResult.salt,
                loginInfoResult.modulus,
                loginInfoResult.serverEphemeral
            )
        }
    }

    @Test
    fun `login happy path events work correctly`() = runTest {
        val sessionInfo = useCase.invoke(testUsername, testPassword)
        assertNotNull(sessionInfo)
    }

    @Test
    fun `correct handling single password account second factor returned`() = runTest {
        coEvery { authRepository.performLogin(any(), any(), any(), any()) } returns sessionInfoResult.copy(
            secondFactor = SecondFactor.Enabled(emptySet())
        )

        val sessionInfo = useCase.invoke(testUsername, testPassword)
        assertNotNull(sessionInfo)
    }

    @Test
    fun `correct handling two password account second factor returned`() = runTest {
        coEvery { authRepository.performLogin(any(), any(), any(), any()) } returns sessionInfoResult.copy(
            passwordMode = 2,
            secondFactor = SecondFactor.Enabled(emptySet())
        )
        val sessionInfo = useCase.invoke(testUsername, testPassword)
        assertNotNull(sessionInfo)
    }
}

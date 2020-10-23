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

import com.google.crypto.tink.subtle.Base64
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.crypto.SrpProofProvider
import me.proton.core.auth.domain.crypto.SrpProofs
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.entity.SecondFactor
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class PerformLoginSuccessApiResultsTest {

    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val srpProofProvider = mockk<SrpProofProvider>(relaxed = true)

    // endregion
    // region test data
    private val testUsername = "test-username"
    private val testPassword = "test-password"

    private val testClientSecret = "test-secret"
    private val testModulus = "test-modulus"
    private val testEphemeral = "test-ephemeral"
    private val testSalt = "test-salt"
    private val testSrpSession = "test-srpSession"
    private val testVersion = 1

    private val testClientEphemeral = "test-clientEphemeral"
    private val testClientProof = "test-clientProof"
    private val testExpectedServerProof = "test-expectedServerProof"

    private val loginInfoResult = LoginInfo(
        username = testUsername, modulus = testModulus, serverEphemeral = testEphemeral, version = testVersion,
        salt = testSalt, srpSession = testSrpSession
    )
    private val sessionInfoResult = SessionInfo(
        username = testUsername, accessToken = "", expiresIn = 1, tokenType = "", scope = "", scopes = emptyList(),
        sessionId = "", userId = "", refreshToken = "", eventId = "", serverProof = "", localId = 1, passwordMode = 1,
        secondFactor = null
    )

    private lateinit var useCase: PerformLogin
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = PerformLogin(authRepository, srpProofProvider, testClientSecret)
        every {
            srpProofProvider.generateSrpProofs(any(), any(), any())
        } returns SrpProofs(
            testClientEphemeral.toByteArray(),
            testClientProof.toByteArray(),
            testExpectedServerProof.toByteArray()
        )
        coEvery {
            authRepository.getLoginInfo(testUsername, testClientSecret)
        } returns DataResult.Success(loginInfoResult, ResponseSource.Remote)
        coEvery {
            authRepository.performLogin(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(sessionInfoResult, ResponseSource.Remote)
    }

    @Test
    fun `login happy path invocations works correctly`() = runBlockingTest {
        useCase.invoke(testUsername, testPassword.toByteArray()).toList()
        coVerify { authRepository.getLoginInfo(testUsername, testClientSecret) }
        coVerify(exactly = 1) {
            authRepository.performLogin(
                testUsername,
                testClientSecret,
                Base64.encode(testClientEphemeral.toByteArray()),
                Base64.encode(testClientProof.toByteArray()),
                testSrpSession
            )
        }
        verify(exactly = 1) {
            srpProofProvider.generateSrpProofs(
                testUsername,
                testPassword.toByteArray(),
                loginInfoResult
            )
        }
    }

    @Test
    fun `login happy path events work correctly`() = runBlockingTest {
        val listOfEvents = useCase.invoke(testUsername, testPassword.toByteArray()).toList()
        assertEquals(2, listOfEvents.size)
        val firstEvent = listOfEvents[0]
        val secondEvent = listOfEvents[1]
        assertTrue(firstEvent is PerformLogin.LoginState.Processing)
        assertTrue(secondEvent is PerformLogin.LoginState.Success)
        assertNotNull(secondEvent.sessionInfo)
        assertNull(secondEvent.sessionInfo.loginPassword)
    }

    @Test
    fun `login empty username emits error`() = runBlockingTest {
        val listOfEvents = useCase.invoke("", testPassword.toByteArray()).toList()
        assertEquals(1, listOfEvents.size)
        assertIs<PerformLogin.LoginState.Error.EmptyCredentials>(listOfEvents[0])
    }

    @Test
    fun `login empty password emits error`() = runBlockingTest {
        val listOfEvents = useCase.invoke(testUsername, "".toByteArray()).toList()
        assertEquals(1, listOfEvents.size)
        assertIs<PerformLogin.LoginState.Error.EmptyCredentials>(listOfEvents[0])
    }

    @Test
    fun `correct handling single password account second factor returned`() = runBlockingTest {
        coEvery {
            authRepository.performLogin(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(
            sessionInfoResult.copy(secondFactor = SecondFactor(true, null)),
            ResponseSource.Remote
        )
        val listOfEvents = useCase.invoke(testUsername, testPassword.toByteArray()).toList()
        assertEquals(2, listOfEvents.size)
        val firstEvent = listOfEvents[0]
        val secondEvent = listOfEvents[1]
        assertTrue(firstEvent is PerformLogin.LoginState.Processing)
        assertTrue(secondEvent is PerformLogin.LoginState.Success)
        assertNotNull(secondEvent.sessionInfo)
        assertNotNull(secondEvent.sessionInfo.loginPassword)
    }

    @Test
    fun `correct handling two password account second factor returned`() = runBlockingTest {
        coEvery {
            authRepository.performLogin(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(
            sessionInfoResult.copy(passwordMode = 2, secondFactor = SecondFactor(true, null)),
            ResponseSource.Remote
        )
        val listOfEvents = useCase.invoke(testUsername, testPassword.toByteArray()).toList()
        assertEquals(2, listOfEvents.size)
        val firstEvent = listOfEvents[0]
        val secondEvent = listOfEvents[1]
        assertTrue(firstEvent is PerformLogin.LoginState.Processing)
        assertTrue(secondEvent is PerformLogin.LoginState.Success)
        assertNotNull(secondEvent.sessionInfo)
        assertNull(secondEvent.sessionInfo.loginPassword)
    }
}

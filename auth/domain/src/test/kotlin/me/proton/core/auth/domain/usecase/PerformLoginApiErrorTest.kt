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
import io.mockk.called
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
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.usecase.PerformLogin.Companion.RESPONSE_CODE_INCORRECT_CREDENTIALS
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class PerformLoginApiErrorTest {

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
        username = testUsername,
        modulus = testModulus,
        serverEphemeral = testEphemeral,
        version = testVersion,
        salt = testSalt,
        srpSession = testSrpSession
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
        } returns DataResult.Error.Remote(
            message = "auth-info error",
            protonCode = 1234,
            httpCode = 401
        )
        coEvery {
            authRepository.performLogin(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Error.Remote(
            message = "auth error",
            protonCode = 1234,
            httpCode = 401
        )
    }

    @Test
    fun `login info error invocations work correctly`() = runBlockingTest {
        // WHEN
        useCase.invoke(testUsername, testPassword.toByteArray()).toList()
        // THEN
        verify {
            srpProofProvider.generateSrpProofs(
                testUsername,
                testPassword.toByteArray(),
                loginInfoResult
            ) wasNot called
        }
        coVerify {
            authRepository.performLogin(
                testUsername,
                testClientSecret,
                Base64.encode(testClientEphemeral.toByteArray()),
                Base64.encode(testClientProof.toByteArray()),
                testSrpSession
            ) wasNot called
        }
    }

    @Test
    fun `login info error events work correctly`() = runBlockingTest {
        // WHEN
        val listOfEvents = useCase.invoke(testUsername, testPassword.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        val firstEvent = listOfEvents[0]
        val secondEvent = listOfEvents[1]
        assertTrue(firstEvent is PerformLogin.State.Processing)
        assertTrue(secondEvent is PerformLogin.State.Error.Message)
        assertFalse(secondEvent.validation)
    }

    @Test
    fun `login error invocations work correctly`() = runBlockingTest {
        // GIVEN
        coEvery {
            authRepository.getLoginInfo(testUsername, testClientSecret)
        } returns DataResult.Success(ResponseSource.Remote, loginInfoResult)
        // WHEN
        useCase.invoke(testUsername, testPassword.toByteArray()).toList()
        // THEN
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
    fun `login error events work correctly`() = runBlockingTest {
        // GIVEN
        coEvery {
            authRepository.getLoginInfo(testUsername, testClientSecret)
        } returns DataResult.Success(ResponseSource.Remote, loginInfoResult)
        // WHEN
        val listOfEvents = useCase.invoke(testUsername, testPassword.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        val firstEvent = listOfEvents[0]
        val secondEvent = listOfEvents[1]
        assertTrue(firstEvent is PerformLogin.State.Processing)
        assertTrue(secondEvent is PerformLogin.State.Error.Message)
        assertFalse(secondEvent.validation)
    }

    @Test
    fun `login incorrect credentials error events work correctly`() = runBlockingTest {
        // GIVEN
        coEvery {
            authRepository.getLoginInfo(testUsername, testClientSecret)
        } returns DataResult.Success(ResponseSource.Remote, loginInfoResult)

        coEvery {
            authRepository.performLogin(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Error.Remote(
            message = "auth error",
            protonCode = RESPONSE_CODE_INCORRECT_CREDENTIALS,
        )
        // WHEN
        val listOfEvents = useCase.invoke(testUsername, testPassword.toByteArray()).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        val firstEvent = listOfEvents[0]
        val secondEvent = listOfEvents[1]
        assertTrue(firstEvent is PerformLogin.State.Processing)
        assertTrue(secondEvent is PerformLogin.State.Error.Message)
        assertTrue(secondEvent.validation)
    }
}

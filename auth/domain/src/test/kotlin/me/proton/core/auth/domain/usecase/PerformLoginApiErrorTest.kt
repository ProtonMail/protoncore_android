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
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * @author Dino Kadrikj.
 */
class PerformLoginApiErrorTest {

    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val srpCrypto = mockk<SrpCrypto>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)

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
        useCase = PerformLogin(authRepository, srpCrypto, keyStoreCrypto, testClientSecret)
        every {
            srpCrypto.generateSrpProofs(any(), any(), any(), any(), any(), any())
        } returns SrpProofs(
            testClientEphemeral.toByteArray(),
            testClientProof.toByteArray(),
            testExpectedServerProof.toByteArray()
        )
        coEvery { authRepository.getLoginInfo(testUsername, testClientSecret) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 401,
                message = "auth-info error",
                proton = ApiResult.Error.ProtonData(1234, "error")
            )
        )
        coEvery { authRepository.performLogin(any(), any(), any(), any(), any()) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 401,
                message = "auth-info error",
                proton = ApiResult.Error.ProtonData(1234, "auth error"),
            )
        )
    }

    @Test(expected = ApiException::class)
    fun `login info error invocations work correctly`() = runBlockingTest {
        // WHEN
        useCase.invoke(testUsername, testPassword)
        // THEN
        verify {
            srpCrypto.generateSrpProofs(
                testUsername,
                testPassword.toByteArray(),
                loginInfoResult.version.toLong(),
                loginInfoResult.salt,
                loginInfoResult.modulus,
                loginInfoResult.serverEphemeral
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
        val throwable = assertFailsWith(ApiException::class) {
            useCase.invoke(testUsername, testPassword)
        }
        // THEN
        assertIs<ApiResult.Error.Http>(throwable.error)
    }

    @Test(expected = ApiException::class)
    fun `login error invocations work correctly`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.getLoginInfo(testUsername, testClientSecret) } returns loginInfoResult
        // WHEN
        useCase.invoke(testUsername, testPassword)
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
            srpCrypto.generateSrpProofs(
                testUsername,
                testPassword.toByteArray(),
                loginInfoResult.version.toLong(),
                loginInfoResult.salt,
                loginInfoResult.modulus,
                loginInfoResult.serverEphemeral
            )
        }
    }
}

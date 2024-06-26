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

package me.proton.core.auth.domain.usecase.scopes

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.repository.UserRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObtainPasswordScopeTest {
    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val cryptoContext = mockk<CryptoContext>(relaxed = true)
    private val srpCrypto = mockk<SrpCrypto>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    // endregion

    // region test data
    private val testUserIdString = "test-user-id"
    private val testSessionIdString = "test-session-id"
    private val testUsername = "test-username"
    private val testPassword = "test-password"
    private val testPasswordEncrypted = "test-password-encrypted"
    private val test2FACode = SecondFactorProof.SecondFactorCode("test-2fa")
    private val testUserId = UserId(testUserIdString)
    private val testSessionId = SessionId(testSessionIdString)
    private val testModulus = "test-modulus"
    private val testEphemeral = "test-ephemeral"
    private val testSalt = "test-salt"
    private val testSrpSession = "test-srpSession"
    private val testVersion = 1

    private val testSrpProofs = SrpProofs(
        clientEphemeral = "test-clientEphemeral",
        clientProof = "test-clientProof",
        expectedServerProof = "test-expectedServerProof"
    )

    private val authInfoResult = AuthInfo.Srp(
        username = testUsername,
        modulus = testModulus,
        serverEphemeral = testEphemeral,
        version = testVersion,
        salt = testSalt,
        srpSession = testSrpSession,
        secondFactor = null
    )
    // endregion

    private lateinit var useCase: ObtainPasswordScope

    @Before
    fun beforeEveryTest() {
        // GIVEN
        every { keyStoreCrypto.decrypt(any<String>()) } returns testPassword
        every { keyStoreCrypto.encrypt(any<String>()) } returns testPasswordEncrypted
        every { cryptoContext.srpCrypto } returns srpCrypto
        every { cryptoContext.keyStoreCrypto } returns keyStoreCrypto

        coEvery {
            srpCrypto.generateSrpProofs(any(), any(), any(), any(), any(), any())
        } returns testSrpProofs

        coEvery { authRepository.getAuthInfoSrp(testSessionId, testUsername) } returns authInfoResult
        coEvery {
            userRepository.unlockUserForPasswordScope(
                testUserId,
                testSrpProofs,
                authInfoResult.srpSession,
                null
            )
        } returns true
        useCase = ObtainPasswordScope(authRepository, userRepository, cryptoContext)
    }

    @Test
    fun testUnlockingPasswordNo2FASuccess() = runTest {
        val result = useCase.invoke(testUserId, testSessionId, testUsername, testPasswordEncrypted, null)

        coVerify { authRepository.getAuthInfoSrp(testSessionId, testUsername) }
        assertTrue(result)
    }

    @Test
    fun testUnlockingPassword2FASuccess() = runTest {
        coEvery {
            userRepository.unlockUserForPasswordScope(
                testUserId,
                testSrpProofs,
                authInfoResult.srpSession,
                test2FACode
            )
        } returns true
        val result = useCase.invoke(testUserId, testSessionId, testUsername, testPasswordEncrypted, test2FACode)

        coVerify { authRepository.getAuthInfoSrp(testSessionId, testUsername) }
        assertTrue(result)
    }

    @Test
    fun testUnlockingPasswordNo2FAFailure() = runTest {
        coEvery {
            userRepository.unlockUserForPasswordScope(
                testUserId,
                testSrpProofs,
                authInfoResult.srpSession,
                null
            )
        } returns false
        val result = useCase.invoke(testUserId, testSessionId, testUsername, testPasswordEncrypted, null)

        coVerify { authRepository.getAuthInfoSrp(testSessionId, testUsername) }
        assertFalse(result)
    }

    @Test
    fun testUnlockingPassword2FAFailure() = runTest {
        coEvery {
            userRepository.unlockUserForPasswordScope(
                testUserId,
                testSrpProofs,
                authInfoResult.srpSession,
                test2FACode
            )
        } returns false
        val result = useCase.invoke(testUserId, testSessionId, testUsername, testPasswordEncrypted, test2FACode)

        coVerify { authRepository.getAuthInfoSrp(testSessionId, testUsername) }
        assertFalse(result)
    }

    @Test
    fun testUnlockingPasswordThrowsException() = runTest {
        coEvery {
            userRepository.unlockUserForPasswordScope(
                testUserId,
                testSrpProofs,
                authInfoResult.srpSession,
                null
            )
        } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 1234,
                message = "error",
                proton = ApiResult.Error.ProtonData(1234, "Invalid input")
            )
        )

        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            useCase.invoke(testUserId, testSessionId, testUsername, testPasswordEncrypted, null)
        }

        // THEN
        assertEquals("Invalid input", throwable.message)
    }
}

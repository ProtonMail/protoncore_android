/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase.scopes

import com.google.crypto.tink.subtle.Base64
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.user.domain.repository.UserRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObtainLockedScopeTest {

    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val cryptoContext = mockk<CryptoContext>(relaxed = true)
    private val srpCrypto = mockk<SrpCrypto>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    // endregion

    // region test data
    private val testUserIdString = "test-user-id"
    private val testUsername = "test-username"
    private val testPassword = "test-password"
    private val testPasswordEncrypted = "test-password-encrypted"
    private val testUserId = UserId(testUserIdString)
    private val testModulus = "test-modulus"
    private val testEphemeral = "test-ephemeral"
    private val testSalt = "test-salt"
    private val testSrpSession = "test-srpSession"
    private val testVersion = 1

    private val testClientEphemeral = "test-clientEphemeral"
    private val testClientProof = "test-clientProof"
    private val testExpectedServerProof = "test-expectedServerProof"

    private val authInfoResult = AuthInfo(
        username = testUsername,
        modulus = testModulus,
        serverEphemeral = testEphemeral,
        version = testVersion,
        salt = testSalt,
        srpSession = testSrpSession,
        secondFactor = null
    )
    // endregion

    private lateinit var useCase: ObtainLockedScope

    @Before
    fun beforeEveryTest() {
        // GIVEN
        every { keyStoreCrypto.decrypt(testPasswordEncrypted) } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns testPasswordEncrypted
        every { cryptoContext.srpCrypto } returns srpCrypto
        every { cryptoContext.keyStoreCrypto } returns keyStoreCrypto

        every {
            srpCrypto.generateSrpProofs(any(), any(), any(), any(), any(), any())
        } returns SrpProofs(
            testClientEphemeral.toByteArray(),
            testClientProof.toByteArray(),
            testExpectedServerProof.toByteArray()
        )

        coEvery { authRepository.getAuthInfo(testUserId, testUsername) } returns authInfoResult
        coEvery {
            userRepository.unlockUserForLockedScope(
                testUserId,
                Base64.encode(testClientEphemeral.toByteArray()),
                Base64.encode(testClientProof.toByteArray()),
                authInfoResult.srpSession
            )
        } returns true
        useCase = ObtainLockedScope(authRepository, userRepository, cryptoContext)
    }

    @Test
    fun testUnlockingSuccess() = runBlockingTest {
        val result = useCase.invoke(testUserId, testUsername, testPasswordEncrypted)

        coVerify { authRepository.getAuthInfo(testUserId, testUsername) }
        assertTrue(result)
    }

    @Test
    fun testUnlockingFailure() = runBlockingTest {
        coEvery {
            userRepository.unlockUserForLockedScope(
                testUserId,
                Base64.encode(testClientEphemeral.toByteArray()),
                Base64.encode(testClientProof.toByteArray()),
                authInfoResult.srpSession
            )
        } returns false
        val result = useCase.invoke(testUserId, testUsername, testPasswordEncrypted)

        coVerify { authRepository.getAuthInfo(testUserId, testUsername) }
        assertFalse(result)
    }

    @Test
    fun testUnlockingException() = runBlockingTest {
        coEvery {
            userRepository.unlockUserForLockedScope(
                testUserId,
                Base64.encode(testClientEphemeral.toByteArray()),
                Base64.encode(testClientProof.toByteArray()),
                authInfoResult.srpSession
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
            useCase.invoke(testUserId, testUsername, testPasswordEncrypted)
        }

        // THEN
        assertEquals("Invalid input", throwable.message)
    }
}

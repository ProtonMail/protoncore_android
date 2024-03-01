/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.usersettings.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Role
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PerformResetUserPasswordTest {
    // region mocks
    private val accountRepository = mockk<AccountRepository>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val userAddressRepository = mockk<UserAddressRepository>(relaxed = true)
    private val organizationRepository = mockk<OrganizationRepository>(relaxed = true)
    private val srpCrypto = mockk<SrpCrypto>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    private val cryptoContext = mockk<CryptoContext>(relaxed = true)
    private val userManager = mockk<UserManager>(relaxed = true)
    private val testPGPCrypto = mockk<PGPCrypto>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = SessionId("test-session-id")
    private val testUserId = UserId("test-user-id")
    private val testUsername = "test-username"
    private val testNewLoginPassword = "test-new-login-password"

    private val testModulusId = "test-modulus-id"
    private val testModulus = "test-modulus"
    private val testSalt = "test-salt"
    private val testKeySalt = "test-keysalt"

    private val testAuth = Auth(
        version = 1,
        modulusId = testModulusId,
        salt = testSalt,
        verifier = "test-verifier"
    )

    private val testUser = User(
        userId = testUserId,
        email = null,
        name = testUsername,
        displayName = null,
        currency = "test-curr",
        credit = 0,
        type = Type.Proton,
        createdAtUtc = 1000L,
        usedSpace = 0,
        maxSpace = 100,
        maxUpload = 100,
        role = null,
        private = true,
        services = 1,
        subscribed = 0,
        delinquent = null,
        recovery = null,
        keys = emptyList()
    )
    // endregion

    private lateinit var useCase: PerformResetUserPassword

    @Before
    fun beforeEveryTest() {
        every { keyStoreCrypto.decrypt("encrypted-test-new-login-password") } returns testNewLoginPassword
        every { keyStoreCrypto.encrypt(testNewLoginPassword) } returns "encrypted-test-new-login-password"

        coEvery { accountRepository.getAccountOrNull(testUserId) } returns mockk {
            every { sessionId } returns testSessionId
        }

        coEvery { authRepository.randomModulus(any()) } returns Modulus(
            modulusId = testModulusId,
            modulus = testModulus
        )

        coEvery { organizationRepository.getOrganizationKeys(testUserId, any()) } returns mockk()
        every { cryptoContext.pgpCrypto } returns testPGPCrypto

        every {
            testPGPCrypto.generateNewKeySalt()
        } returns testKeySalt

        every { cryptoContext.srpCrypto } returns srpCrypto
        every { cryptoContext.keyStoreCrypto } returns keyStoreCrypto

        coEvery {
            srpCrypto.calculatePasswordVerifier(
                testUsername,
                testNewLoginPassword.toByteArray(),
                testModulusId,
                testModulus
            )
        } returns testAuth

        useCase = PerformResetUserPassword(
            context = cryptoContext,
            userManager = userManager,
            accountRepository = accountRepository,
            authRepository = authRepository,
            userRepository = userRepository,
            userAddressRepository = userAddressRepository
        )
    }

    @Test
    fun `reset password no email and no username fails`() = runTest {
        coEvery { userRepository.getUser(testUserId) } returns testUser.copy(name = null)

        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                newPassword = keyStoreCrypto.encrypt(testNewLoginPassword),
            )
        }
    }

    @Test
    fun `reset password account null fails`() = runTest {
        coEvery { userRepository.getUser(testUserId) } returns testUser

        coEvery { accountRepository.getAccountOrNull(testUserId) } returns null

        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                newPassword = keyStoreCrypto.encrypt(testNewLoginPassword),
            )
        }
    }

    @Test
    fun `reset password account session id null fails`() = runTest {
        coEvery { userRepository.getUser(testUserId) } returns testUser

        coEvery { accountRepository.getAccountOrNull(testUserId) } returns mockk {
            every { sessionId } returns null
        }

        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                newPassword = keyStoreCrypto.encrypt(testNewLoginPassword),
            )
        }
    }

    @Test
    fun `reset password no username fails returns true`() = runTest {
        coEvery { userRepository.getUser(testUserId) } returns testUser.copy(email = "test-email@test.com", name = null)

        coEvery {
            srpCrypto.calculatePasswordVerifier(
                "test-email@test.com",
                testNewLoginPassword.toByteArray(),
                testModulusId,
                testModulus
            )
        } returns testAuth

        coEvery {
            userManager.resetPassword(
                sessionUserId = testUserId,
                newPassword = any(),
                auth = testAuth
            )
        } returns true

        val result = useCase.invoke(
            userId = testUserId,
            newPassword = keyStoreCrypto.encrypt(testNewLoginPassword),
        )
        assertTrue(result)
    }

    @Test
    fun `reset password no email returns true`() = runTest {
        coEvery { userRepository.getUser(testUserId) } returns testUser

        coEvery {
            userManager.resetPassword(
                sessionUserId = testUserId,
                newPassword = any(),
                auth = testAuth
            )
        } returns true

        val result = useCase.invoke(
            userId = testUserId,
            newPassword = keyStoreCrypto.encrypt(testNewLoginPassword),
        )
        assertTrue(result)
    }

    @Test
    fun `reset password organization admin`() = runTest {
        coEvery { userRepository.getUser(testUserId) } returns testUser.copy(role = Role.OrganizationAdmin)

        coEvery {
            organizationRepository.getOrganizationKeys(
                sessionUserId = testUserId,
                refresh = any()
            )
        } returns mockk {
            every { privateKey } returns "armored-private-key"
        }

        coEvery {
            userManager.resetPassword(
                sessionUserId = testUserId,
                newPassword = any(),
                auth = testAuth
            )
        } returns true

        val result = useCase.invoke(
            userId = testUserId,
            newPassword = keyStoreCrypto.encrypt(testNewLoginPassword),
        )
        assertTrue(result)
    }

    @Test
    fun `reset password no email returns false`() = runTest {
        coEvery { userRepository.getUser(testUserId) } returns testUser

        coEvery {
            userManager.resetPassword(
                sessionUserId = testUserId,
                newPassword = any(),
                auth = testAuth
            )
        } returns false

        val result = useCase.invoke(
            userId = testUserId,
            newPassword = keyStoreCrypto.encrypt(testNewLoginPassword),
        )
        assertFalse(result)
    }
}

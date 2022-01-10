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

package me.proton.core.usersettings.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class PerformUpdateUserPasswordTest {
    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val organizationRepository = mockk<OrganizationRepository>(relaxed = true)
    private val srpCrypto = mockk<SrpCrypto>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    private val cryptoContext = mockk<CryptoContext>(relaxed = true)
    private val userManager = mockk<UserManager>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testClientSecret = "test-client-secret"
    private val testUsername = "test-username"
    private val testSecondFactor = "123456"
    private val testKeySalt = "test-keysalt"
    private val testSrpProofs = SrpProofs(
        clientEphemeral = "test-client-ephemeral",
        clientProof = "test-client-proof",
        expectedServerProof = "test-server-proof"
    )
    private val testSrpSession = "test-srp-session"
    private val testLoginPassword = "test-login-password"
    private val testNewMailboxPassword = "test-new-mailbox-password"
    private val testModulusId = "test-modulus-id"
    private val testModulus = "test-modulus"
    private val testServerEphemeral = "test-server-ephemeral"
    private val testSalt = "test-salt"

    private val testAuth = Auth(
        version = 1,
        modulusId = testModulusId,
        salt = testSalt,
        verifier = "test-verifier"
    )
    private val testPGPCrypto = mockk<PGPCrypto>(relaxed = true)
    private val testUser = User(
        userId = testUserId,
        email = null,
        name = testUsername,
        displayName = null,
        currency = "test-curr",
        credit = 0,
        usedSpace = 0,
        maxSpace = 100,
        maxUpload = 100,
        role = null,
        private = true,
        services = 1,
        subscribed = 0,
        delinquent = null,
        keys = emptyList()
    )
    // endregion

    private lateinit var useCase: PerformUpdateUserPassword

    @Before
    fun beforeEveryTest() {
        every { keyStoreCrypto.decrypt("encrypted-test-login-password") } returns testLoginPassword
        every { keyStoreCrypto.encrypt(testLoginPassword) } returns "encrypted-test-login-password"
        every { keyStoreCrypto.decrypt("encrypted-test-new-mailbox-password") } returns testNewMailboxPassword
        every { keyStoreCrypto.encrypt(testNewMailboxPassword) } returns "encrypted-test-new-mailbox-password"

        useCase = PerformUpdateUserPassword(
            context = cryptoContext,
            authRepository = authRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository,
            clientSecret = testClientSecret,
            userManager = userManager
        )

        coEvery {
            userManager.changePassword(
                userId = testUserId,
                newPassword = any(),
                secondFactorCode = any(),
                proofs = any(),
                srpSession = any(),
                auth = any(),
                orgPrivateKey = any()
            )
        } returns true

        coEvery { authRepository.getLoginInfo(testUsername, testClientSecret) } returns LoginInfo(
            username = testUsername,
            modulus = testModulus,
            serverEphemeral = testServerEphemeral,
            version = 1,
            salt = testSalt,
            srpSession = testSrpSession
        )
        coEvery { authRepository.randomModulus() } returns Modulus(
            modulusId = testModulusId,
            modulus = testModulus
        )
        coEvery { organizationRepository.getOrganizationKeys(testUserId, any()) } returns mockk()
        every { cryptoContext.pgpCrypto } returns testPGPCrypto
        every {
            testPGPCrypto.generateNewKeySalt()
        } returns testKeySalt

        every { cryptoContext.srpCrypto } returns srpCrypto

        every {
            srpCrypto.calculatePasswordVerifier(
                testUsername,
                testNewMailboxPassword.toByteArray(),
                testModulusId,
                testModulus
            )
        } returns testAuth

        coEvery { userRepository.getUser(testUserId) } returns testUser

        every {
            srpCrypto.generateSrpProofs(
                username = testUsername,
                password = testLoginPassword.toByteArray(),
                version = 1,
                salt = testSalt,
                modulus = testModulus,
                serverEphemeral = testServerEphemeral
            )
        } returns testSrpProofs
    }

    @Test
    fun `update mailbox password no username fails`() = runBlockingTest {
        coEvery { userRepository.getUser(testUserId) } returns testUser.copy(name = null)

        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                secondFactorCode = testSecondFactor,
                loginPassword = keyStoreCrypto.encrypt(testLoginPassword),
                newPassword = keyStoreCrypto.encrypt(testNewMailboxPassword),
                twoPasswordMode = false
            )
        }
    }
}

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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.entity.Flags
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class PerformUpdateLoginPasswordTest {
    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val repository = mockk<UserSettingsRepository>(relaxed = true)
    private val srpCrypto = mockk<SrpCrypto>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    private val cryptoContext = mockk<CryptoContext>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testClientSecret = "test-client-secret"
    private val testUsername = "test-username"
    private val testPassword = "test-password"
    private val testSecondFactor = "123456"
    private val testSrpProofs = SrpProofs(
        clientEphemeral = "test-client-ephemeral",
        clientProof = "test-client-proof",
        expectedServerProof = "test-server-proof"
    )
    private val testSrpSession = "test-srp-session"
    private val testModulus = "test-modulus"
    private val testServerEphemeral = "test-server-ephemeral"
    private val testNewPassword = "test-new-password"

    private val testSalt = "test-salt"
    private val testModulusId = "test-modulus-id"
    private val testVerifier = "test-verifier"
    private val testAuth = Auth(
        version = 1,
        modulusId = testModulusId,
        salt = testSalt,
        verifier = testVerifier
    )

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

    private val testUserSettingsResponse = UserSettings(
        userId = testUserId,
        email = RecoverySetting("test-email", 1, notify = true, reset = true),
        phone = null,
        twoFA = null,
        password = PasswordSetting(mode = 1, expirationTime = null),
        news = 0,
        locale = "en",
        logAuth = UserSettings.LogAuth.enumOf(1),
        density = UserSettings.Density.enumOf(1),
        invoiceText = "",
        dateFormat = UserSettings.DateFormat.enumOf(1),
        timeFormat = UserSettings.TimeFormat.enumOf(2),
        themeType = 1,
        weekStart = UserSettings.WeekStart.enumOf(7),
        welcome = true,
        earlyAccess = true,
        theme = "test-theme",
        flags = Flags(true)
    )
    // endregion

    private lateinit var useCase: PerformUpdateLoginPassword

    @Before
    fun beforeEveryTest() {
        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"
        every { keyStoreCrypto.decrypt("encrypted-test-new-password") } returns testNewPassword
        every { keyStoreCrypto.encrypt(testNewPassword) } returns "encrypted-test-new-password"

        every { cryptoContext.srpCrypto } returns srpCrypto
        every { cryptoContext.keyStoreCrypto } returns keyStoreCrypto

        useCase = PerformUpdateLoginPassword(
            authRepository = authRepository,
            userRepository = userRepository,
            userSettingsRepository = repository,
            clientSecret = testClientSecret,
            context = cryptoContext
        )


        coEvery {
            repository.updateLoginPassword(
                sessionUserId = testUserId,
                srpProofs = any(),
                srpSession = any(),
                secondFactorCode = any(),
                auth = testAuth
            )
        } returns testUserSettingsResponse

        coEvery { userRepository.getUser(any()) } returns testUser
    }

    @Test
    fun `update login password returns success`() = runBlockingTest {
        // GIVEN
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
        every {
            srpCrypto.generateSrpProofs(
                username = testUsername,
                password = testPassword.toByteArray(),
                version = 1,
                salt = testSalt,
                modulus = testModulus,
                serverEphemeral = testServerEphemeral
            )
        } returns testSrpProofs
        every {
            srpCrypto.calculatePasswordVerifier(testUsername, testNewPassword.toByteArray(), testModulusId, testModulus)
        } returns testAuth

        // WHEN
        val result = useCase.invoke(
            userId = testUserId,
            password = keyStoreCrypto.encrypt(testPassword),
            newPassword = keyStoreCrypto.encrypt(testNewPassword),
            secondFactorCode = testSecondFactor
        )
        // THEN
        coVerify(exactly = 1) {
            repository.updateLoginPassword(
                sessionUserId = testUserId,
                srpProofs = testSrpProofs,
                srpSession = testSrpSession,
                secondFactorCode = testSecondFactor,
                auth = testAuth
            )
        }
        assertNotNull(result)
    }
}

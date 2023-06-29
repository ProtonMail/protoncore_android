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

package me.proton.core.usersettings.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class PerformUpdateRecoveryEmailTest {
    // region mocks
    private val accountRepository = mockk<AccountRepository>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val userSettingsRepository = mockk<UserSettingsRepository>(relaxed = true)
    private val srpCrypto = mockk<SrpCrypto>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = SessionId("test-session-id")
    private val testUserId = UserId("test-user-id")
    private val testUsername = "test-username"
    private val testPassword = "test-password"
    private val testSecondFactor = "123456"
    private val testSrpSession = "test-srp-session"
    private val testModulus = "test-modulus"
    private val testServerEphemeral = "test-server-ephemeral"
    private val testSalt = "test-salt"

    private val testUser = mockk<User> {
        every { userId } returns testUserId
        every { name } returns testUsername
        every { email } returns null
    }

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
        dateFormat = UserSettings.DateFormat.enumOf(1),
        timeFormat = UserSettings.TimeFormat.enumOf(2),
        weekStart = UserSettings.WeekStart.enumOf(7),
        earlyAccess = true
    )

    // endregion
    private lateinit var useCase: PerformUpdateRecoveryEmail

    @Before
    fun beforeEveryTest() {
        coEvery { userRepository.getUser(any()) } returns testUser
        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"

        useCase = PerformUpdateRecoveryEmail(
            accountRepository,
            authRepository,
            userRepository,
            userSettingsRepository,
            srpCrypto,
            keyStoreCrypto
        )

        coEvery {
            userSettingsRepository.updateRecoveryEmail(
                testUserId,
                any(),
                any(),
                any(),
                any()
            )
        } returns testUserSettingsResponse

        coEvery { accountRepository.getAccountOrNull(any<SessionId>()) } returns mockk {
            every { sessionId } returns testSessionId
        }
    }

    @Test
    fun `update recovery email empty returns success`() = runTest {
        // GIVEN
        coEvery { authRepository.getAuthInfoSrp(testSessionId, testUsername) } returns AuthInfo.Srp(
            username = testUsername,
            modulus = testModulus,
            serverEphemeral = testServerEphemeral,
            version = 1,
            salt = testSalt,
            srpSession = testSrpSession,
            secondFactor = null
        )

        coEvery {
            srpCrypto.generateSrpProofs(
                username = testUsername,
                password = testPassword.toByteArray(),
                version = 1,
                salt = testSalt,
                modulus = testModulus,
                serverEphemeral = testServerEphemeral
            )
        } returns mockk()

        // WHEN
        val result = useCase.invoke(
            sessionUserId = testUserId,
            newRecoveryEmail = "",
            password = keyStoreCrypto.encrypt(testPassword),
            secondFactorCode = testSecondFactor
        )
        coVerify(exactly = 1) {
            userSettingsRepository.updateRecoveryEmail(
                sessionUserId = testUserId,
                email = "",
                srpProofs = any(),
                srpSession = any(),
                secondFactorCode = testSecondFactor
            )
        }
        assertNotNull(result)
    }

    @Test
    fun `update recovery non empty email returns success`() = runTest {
        // GIVEN
        coEvery { authRepository.getAuthInfoSrp(testSessionId, testUsername) } returns AuthInfo.Srp(
            username = testUsername,
            modulus = testModulus,
            serverEphemeral = testServerEphemeral,
            version = 1,
            salt = testSalt,
            srpSession = testSrpSession,
            secondFactor = null
        )

        coEvery {
            srpCrypto.generateSrpProofs(
                username = testUsername,
                password = any(),
                version = 1,
                salt = testSalt,
                modulus = testModulus,
                serverEphemeral = testServerEphemeral
            )
        } returns mockk()

        // WHEN
        val result = useCase.invoke(
            sessionUserId = testUserId,
            newRecoveryEmail = "",
            password = testPassword,
            secondFactorCode = testSecondFactor
        )
        coVerify(exactly = 1) {
            userSettingsRepository.updateRecoveryEmail(
                sessionUserId = testUserId,
                email = "",
                srpProofs = any(),
                srpSession = any(),
                secondFactorCode = testSecondFactor
            )
        }
        assertNotNull(result)
    }
}

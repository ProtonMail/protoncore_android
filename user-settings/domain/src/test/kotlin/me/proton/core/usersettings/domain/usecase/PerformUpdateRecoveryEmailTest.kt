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

import com.google.crypto.tink.subtle.Base64
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.entity.Flags
import me.proton.core.usersettings.domain.entity.Password
import me.proton.core.usersettings.domain.entity.Setting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class PerformUpdateRecoveryEmailTest {
    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val repository = mockk<UserSettingsRepository>(relaxed = true)
    private val srpCrypto = mockk<SrpCrypto>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testClientSecret = "test-client-secret"
    private val testUsername = "test-username"
    private val testPassword = "test-password"
    private val testSecondFactor = "123456"
    private val testClientEphemeral = "test-client-ephemeral"
    private val testClientProof = "test-client-proof"
    private val testExpectedServerProof = "test-server-proof"
    private val testSrpSession = "test-srp-session"
    private val testModulus = "test-modulus"
    private val testServerEphemeral = "test-server-ephemeral"
    private val testSalt = "test-salt"

    private val testUserSettingsResponse = UserSettings(
        email = Setting("test-email", 1, 1, 1),
        phone = null,
        twoFA = null,
        password = Password(mode = 1, expirationTime = null),
        news = 0,
        locale = "en",
        logAuth = 1,
        density = 1,
        invoiceText = "",
        dateFormat = 1,
        timeFormat = 2,
        themeType = 1,
        weekStart = 7,
        welcome = 1,
        earlyAccess = 1,
        theme = "test-theme",
        flags = Flags(1)
    )

    // endregion
    private lateinit var useCase: PerformUpdateRecoveryEmail

    @Before
    fun beforeEveryTest() {
        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"

        useCase = PerformUpdateRecoveryEmail(
            authRepository,
            repository,
            srpCrypto,
            keyStoreCrypto,
            testClientSecret
        )

        coEvery {
            repository.updateRecoveryEmail(
                testUserId,
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns testUserSettingsResponse
    }

    @Test
    fun `update recovery email empty returns success`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.getLoginInfo(testUsername, testClientSecret) } returns LoginInfo(
            username = testUsername,
            modulus = testModulus,
            serverEphemeral = testServerEphemeral,
            version = 1,
            salt = testSalt,
            srpSession = testSrpSession
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
        } returns
            SrpProofs(
                testClientEphemeral.toByteArray(),
                testClientProof.toByteArray(),
                testExpectedServerProof.toByteArray()
            )
        // WHEN
        val result = useCase.invoke(
            sessionUserId = testUserId,
            newRecoveryEmail = "",
            username = testUsername,
            password = keyStoreCrypto.encrypt(testPassword),
            secondFactorCode = testSecondFactor
        )
        coVerify(exactly = 1) {
            repository.updateRecoveryEmail(
                sessionUserId = testUserId,
                email = "",
                clientEphemeral = Base64.encode(testClientEphemeral.toByteArray()),
                clientProof = Base64.encode(testClientProof.toByteArray()),
                srpSession = testSrpSession,
                secondFactorCode = testSecondFactor
            )
        }
        assertNotNull(result)
    }

    @Test
    fun `update recovery non empty email returns success`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.getLoginInfo(testUsername, testClientSecret) } returns LoginInfo(
            username = testUsername,
            modulus = testModulus,
            serverEphemeral = testServerEphemeral,
            version = 1,
            salt = testSalt,
            srpSession = testSrpSession
        )

        every {
            srpCrypto.generateSrpProofs(
                username = testUsername,
                password = any(),
                version = 1,
                salt = testSalt,
                modulus = testModulus,
                serverEphemeral = testServerEphemeral
            )
        } returns
            SrpProofs(
                testClientEphemeral.toByteArray(),
                testClientProof.toByteArray(),
                testExpectedServerProof.toByteArray()
            )
        // WHEN
        val result = useCase.invoke(
            sessionUserId = testUserId,
            newRecoveryEmail = "",
            username = testUsername,
            password = testPassword,
            secondFactorCode = testSecondFactor
        )
        coVerify(exactly = 1) {
            repository.updateRecoveryEmail(
                sessionUserId = testUserId,
                email = "",
                clientEphemeral = Base64.encode(testClientEphemeral.toByteArray()),
                clientProof = Base64.encode(testClientProof.toByteArray()),
                srpSession = testSrpSession,
                secondFactorCode = testSecondFactor
            )
        }
        assertNotNull(result)
    }
}
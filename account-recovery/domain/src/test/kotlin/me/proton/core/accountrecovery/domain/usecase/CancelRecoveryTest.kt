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

package me.proton.core.accountrecovery.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountrecovery.domain.repository.AccountRecoveryRepository
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val testUserId = UserId("user-id")

class CancelRecoveryTest {
    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var accountRecoveryRepository: AccountRecoveryRepository

    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK
    private lateinit var cryptoContext: CryptoContext

    private lateinit var tested: CancelRecovery

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        every { cryptoContext.keyStoreCrypto } returns mockk {
            every { decrypt(any<String>()) } answers { firstArg() }
        }
        every { cryptoContext.srpCrypto } returns mockk {
            every { generateSrpProofs(any(), any(), any(), any(), any(), any()) } returns mockk()
        }

        tested = CancelRecovery(
            accountManager,
            accountRecoveryRepository,
            authRepository,
            cryptoContext
        )
    }

    @Test
    fun noAccountForUser() = runTest {
        every { accountManager.getAccount(testUserId) } returns flowOf(null)
        assertFailsWith<IllegalArgumentException> { tested("password", testUserId) }
    }

    @Test
    fun noSessionForUser() = runTest {
        val account = mockk<Account> {
            every { sessionId } returns null
        }
        every { accountManager.getAccount(testUserId) } returns flowOf(account)
        assertFailsWith<IllegalArgumentException> { tested("password", testUserId) }
    }

    @Test
    fun cancellingRecoveryAttempt() = runTest {
        // GIVEN
        val testSessionId = SessionId("session-id")
        val testUsername = "username"
        val account = mockk<Account> {
            every { sessionId } returns testSessionId
            every { username } returns testUsername
        }
        every { accountManager.getAccount(testUserId) } returns flowOf(account)
        coEvery { authRepository.getAuthInfoSrp(testSessionId, testUsername) } returns mockk {
            every { version } returns 0
            every { salt } returns "salt"
            every { modulus } returns "modulus"
            every { serverEphemeral } returns "serverEphemeral"
            every { srpSession } returns "srpSession"
        }
        every { cryptoContext.srpCrypto } returns mockk {
            every { generateSrpProofs(any(), any(), any(), any(), any(), any()) } returns mockk()
        }
        coEvery {
            accountRecoveryRepository.cancelRecoveryAttempt(
                any(),
                any(),
                testUserId
            )
        } returns true

        // WHEN
        val result = tested("password", testUserId)

        // THEN
        assertTrue(result)
    }
}

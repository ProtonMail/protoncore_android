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

package me.proton.core.accountrecovery.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import me.proton.core.accountrecovery.data.api.AccountRecoveryApi
import me.proton.core.accountrecovery.data.api.response.CancelRecoveryAttemptResponse
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.runTestWithResultContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class AccountRecoveryRepositoryImplTest {
    @MockK
    private lateinit var apiProvider: ApiProvider

    @MockK
    private lateinit var validateServerProof: ValidateServerProof

    private lateinit var tested: AccountRecoveryRepositoryImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = AccountRecoveryRepositoryImpl(apiProvider, validateServerProof)
    }

    @Test
    fun cancellingRecoveryAttempt() = runTestWithResultContext {
        // GIVEN
        val testUserId = UserId("user-id")
        val srpProofs = mockk<SrpProofs>(relaxed = true)
        val accountRecoveryApi = mockk<AccountRecoveryApi> {
            coEvery { cancelRecoveryAttempt(any()) } returns
                    CancelRecoveryAttemptResponse(ResponseCodes.OK, "serverProof")
        }
        coEvery { apiProvider.get(AccountRecoveryApi::class, any()) } returns
                TestApiManager(accountRecoveryApi)
        every { apiProvider.sessionProvider } returns mockk {
            coEvery { getSessionId(testUserId) } returns SessionId("session-id")
        }
        justRun { validateServerProof(any(), any(), any()) }

        // WHEN
        tested.cancelRecoveryAttempt(srpProofs, "srpSession", testUserId)

        // THEN
        assertTrue(assertSingleResult("account_recovery.cancellation").isSuccess)
    }

    @Test
    fun cancellingRecoveryAttemptUnsuccessful() = runTestWithResultContext {
        // GIVEN
        val testUserId = UserId("user-id")
        val srpProofs = mockk<SrpProofs>(relaxed = true)
        val accountRecoveryApi = mockk<AccountRecoveryApi> {
            coEvery { cancelRecoveryAttempt(any()) } returns
                    CancelRecoveryAttemptResponse(ResponseCodes.NOT_ALLOWED, "")
        }
        coEvery { apiProvider.get(AccountRecoveryApi::class, any()) } coAnswers {
            TestApiManager(accountRecoveryApi)
        }
        every { apiProvider.sessionProvider } returns mockk {
            coEvery { getSessionId(testUserId) } returns SessionId("session-id")
        }
        justRun { validateServerProof(any(), any(), any()) }

        assertFails {
            // WHEN
            tested.cancelRecoveryAttempt(srpProofs, "srpSession", testUserId)
        }

        // THEN
        assertTrue(assertSingleResult("account_recovery.cancellation").isFailure)
    }
}

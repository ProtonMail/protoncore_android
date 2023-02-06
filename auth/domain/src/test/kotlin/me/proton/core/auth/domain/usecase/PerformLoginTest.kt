/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.SignupLoginTotalV1
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PerformLoginTest {
    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK(relaxUnitFun = true)
    private lateinit var challengeManager: ChallengeManager

    @MockK
    private lateinit var keyStoreCrypto: KeyStoreCrypto

    @MockK(relaxUnitFun = true)
    private lateinit var observabilityManager: ObservabilityManager

    private lateinit var tested: PerformLogin

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { authRepository.getAuthInfo(any(), any()) } returns mockk(relaxed = true)
        coEvery { authRepository.performLogin(any(), any(), any(), any()) } returns mockk(relaxed = true)
        coEvery { challengeManager.getFramesByFlowName(any()) } returns mockk()
        every { keyStoreCrypto.decrypt(any<String>()) } returns "test-password"

        tested = PerformLogin(
            authRepository,
            mockk(relaxed = true),
            keyStoreCrypto,
            challengeManager,
            LoginChallengeConfig(),
            observabilityManager
        )
    }

    @Test
    fun `login after signup observability data`() = runTest {
        // WHEN
        tested.invoke(
            username = "test-username",
            password = "encrypted-test-password",
            loginMetricData = { SignupLoginTotalV1(it) }
        )

        // THEN
        val loginEventSlot = slot<SignupLoginTotalV1>()
        verify { observabilityManager.enqueue(capture(loginEventSlot), any()) }
        assertEquals(HttpApiStatus.http2xx, loginEventSlot.captured.Labels.status)
    }
}

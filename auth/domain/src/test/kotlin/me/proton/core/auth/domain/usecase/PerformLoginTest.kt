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

package me.proton.core.auth.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import kotlin.test.BeforeTest

class PerformLoginTest {
    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK(relaxUnitFun = true)
    private lateinit var challengeManager: ChallengeManager

    @MockK
    private lateinit var keyStoreCrypto: KeyStoreCrypto

    private lateinit var tested: PerformLogin

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { authRepository.getAuthInfoSrp(any(), any()) } returns mockk(relaxed = true)
        coEvery { authRepository.performLogin(any(), any(), any(), any()) } returns mockk(relaxed = true)
        coEvery { challengeManager.getFramesByFlowName(any()) } returns mockk()
        every { keyStoreCrypto.decrypt(any<String>()) } returns "test-password"

        tested = PerformLogin(
            authRepository,
            mockk(relaxed = true),
            keyStoreCrypto,
            challengeManager,
            LoginChallengeConfig()
        )
    }
}

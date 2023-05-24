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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.repository.AuthRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class PerformLoginSsoTest {

    private val authRepository: AuthRepository = mockk {
        coEvery { this@mockk.performLoginSso(any(), any()) } returns mockk()
    }

    private lateinit var tested: PerformLoginSso

    @BeforeTest
    fun setUp() {
        tested = PerformLoginSso(authRepository)
    }

    @Test
    fun `call performLoginSso`() = runTest {
        // WHEN
        tested.invoke(email = "username@domain.com", token = "token")

        // THEN
        coVerify { authRepository.performLoginSso(any(), any()) }
    }
}

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
import me.proton.core.util.kotlin.coroutine.withResultContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GetAuthSsoTest {

    private val authRepository: AuthRepository = mockk {
        coEvery { this@mockk.getAuthInfoSso(any()) } returns mockk()
    }

    private val testEmail = "username@domain.com"

    private lateinit var tested: GetAuthInfoSso

    @BeforeTest
    fun setUp() {
        tested = GetAuthInfoSso(authRepository)
    }

    @Test
    fun `call getAuthInfoSso and return result`() = runTest {
        // WHEN
        var result: Result<*>? = null
        withResultContext {
            onResult("getAuthInfoSso") { result = this }
            tested.invoke(email = testEmail)
        }
        // THEN
        coVerify { authRepository.getAuthInfoSso(testEmail) }
        assertTrue(requireNotNull(result).isSuccess)
    }
}

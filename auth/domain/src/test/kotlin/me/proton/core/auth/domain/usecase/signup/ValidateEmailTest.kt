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

package me.proton.core.auth.domain.usecase.signup

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.repository.AuthRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateEmailTest {
    private val authSignupRepository = mockk<AuthRepository>(relaxed = true)

    private lateinit var useCase: ValidateEmail
    private val testEmail = "test-email"

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = ValidateEmail(authSignupRepository)
        coEvery { authSignupRepository.validateEmail("") } returns false
        coEvery { authSignupRepository.validateEmail(testEmail) } returns true
    }

    @Test
    fun `validate email happy path`() = runBlockingTest {
        // WHEN
        val response = useCase.invoke(testEmail)
        // THEN
        assertTrue(response)
    }

    @Test
    fun `validate empty email returns error`() = runBlockingTest {
        // WHEN
        val response = useCase.invoke("")
        // THEN
        assertFalse(response)
    }

    @Test
    fun `validate email happy path invocations are correct`() = runBlockingTest {
        // WHEN
        useCase.invoke(testEmail)
        // THEN
        coVerify(exactly = 1) { authSignupRepository.validateEmail(testEmail) }
    }
}

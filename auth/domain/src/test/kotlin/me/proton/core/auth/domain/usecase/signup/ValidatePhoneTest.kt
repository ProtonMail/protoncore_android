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
import me.proton.core.auth.domain.repository.AuthSignupRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidatePhoneTest {
    private val authSignupRepository = mockk<AuthSignupRepository>(relaxed = true)

    private lateinit var useCase: ValidatePhone
    private val testPhoneNumber = "test-phone-number"

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = ValidatePhone(authSignupRepository)
        coEvery { authSignupRepository.validatePhone("") } returns false
        coEvery { authSignupRepository.validatePhone(testPhoneNumber) } returns true
    }

    @Test
    fun `validate phone happy path`() = runBlockingTest {
        // WHEN
        val response = useCase.invoke(testPhoneNumber)
        // THEN
        assertTrue(response)
    }

    @Test
    fun `validate empty phone returns error`() = runBlockingTest {
        // WHEN
        val response = useCase.invoke("")
        // THEN
        assertFalse(response)
    }

    @Test
    fun `validate phone happy path invocations are correct`() = runBlockingTest {
        // WHEN
        useCase.invoke(testPhoneNumber)
        // THEN
        coVerify(exactly = 1) { authSignupRepository.validatePhone(testPhoneNumber) }
    }
}

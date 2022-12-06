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

package me.proton.core.auth.presentation.viewmodel.signup

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.auth.presentation.viewmodel.signup.ChooseExternalEmailViewModel.State
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChooseExternalEmailViewModelTest : ArchTest, CoroutinesTest {

    private val accountAvailability = mockk<AccountAvailability>(relaxed = true)

    private lateinit var viewModel: ChooseExternalEmailViewModel

    @Test
    fun `check email success`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { accountAvailability.checkExternalEmail(testEmail) } returns Unit
        viewModel = ChooseExternalEmailViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkExternalEmail(testEmail)
            // THEN
            val item = expectMostRecentItem() as State.Success
            assertEquals(testEmail, item.email)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check email error`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { accountAvailability.checkExternalEmail(testEmail) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 12106,
                    error = "email not available"
                )
            )
        )
        // WHEN
        viewModel = ChooseExternalEmailViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkExternalEmail(testEmail)
            // THEN
            assertTrue(expectMostRecentItem() is State.Error.Message)
            cancelAndConsumeRemainingEvents()
        }
    }
}

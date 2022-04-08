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
import io.mockk.coVerify
import io.mockk.mockk
import me.proton.core.auth.domain.usecase.signup.ValidateEmail
import me.proton.core.auth.domain.usecase.signup.ValidatePhone
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecoveryMethodViewModelTest : ArchTest, CoroutinesTest {
    // region mocks
    private val validateEmail = mockk<ValidateEmail>(relaxed = true)
    private val validatePhone = mockk<ValidatePhone>(relaxed = true)
    // endregion

    private lateinit var viewModel: RecoveryMethodViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = RecoveryMethodViewModel(validateEmail, validatePhone)
    }

    @Test
    fun `email validation returns true`() = coroutinesTest {
        // GIVEN
        val testEmail = "test-email"
        coEvery { validateEmail.invoke(testEmail) } returns true

        viewModel.validationResult.test {
            // WHEN
            viewModel.setActiveRecoveryMethod(RecoveryMethodType.EMAIL, testEmail)
            viewModel.validateRecoveryDestinationInput()
            // THEN
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.None)
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.Processing)
            val successItem = awaitItem()
            assertTrue(successItem is RecoveryMethodViewModel.ValidationState.Success)
            assertTrue(successItem.value)
            coVerify(exactly = 1) {
                validateEmail.invoke(testEmail)
            }
        }
    }

    @Test
    fun `email validation returns false`() = coroutinesTest {
        // GIVEN
        val testEmail = "test-email"
        coEvery { validateEmail.invoke(testEmail) } returns false

        viewModel.validationResult.test {
            // WHEN
            viewModel.setActiveRecoveryMethod(RecoveryMethodType.EMAIL, testEmail)
            viewModel.validateRecoveryDestinationInput()
            // THEN
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.None)
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.Processing)
            val successItem = awaitItem()
            assertTrue(successItem is RecoveryMethodViewModel.ValidationState.Success)
            assertFalse(successItem.value)
            coVerify(exactly = 1) {
                validateEmail.invoke(testEmail)
            }
        }
    }

    @Test
    fun `email validation API error`() = coroutinesTest {
        // GIVEN
        val testEmail = "test-email"
        coEvery { validateEmail.invoke(testEmail) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "API error validation."
                )
            )
        )

        viewModel.validationResult.test {
            // WHEN
            viewModel.setActiveRecoveryMethod(RecoveryMethodType.EMAIL, testEmail)
            viewModel.validateRecoveryDestinationInput()
            // THEN
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.None)
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.Processing)
            val errorItem = awaitItem()
            assertTrue(errorItem is RecoveryMethodViewModel.ValidationState.Error)
            coVerify(exactly = 1) {
                validateEmail.invoke(testEmail)
            }
        }
    }

    @Test
    fun `phone validation returns true`() = coroutinesTest {
        // GIVEN
        val testPhone = "test-phone"
        coEvery { validatePhone.invoke(testPhone) } returns true

        viewModel.validationResult.test {
            // WHEN
            viewModel.setActiveRecoveryMethod(RecoveryMethodType.SMS, testPhone)
            viewModel.validateRecoveryDestinationInput()
            // THEN
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.None)
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.Processing)
            val successItem = awaitItem()
            assertTrue(successItem is RecoveryMethodViewModel.ValidationState.Success)
            assertTrue(successItem.value)
            coVerify(exactly = 1) {
                validatePhone.invoke(testPhone)
            }
        }
    }

    @Test
    fun `phone validation returns false`() = coroutinesTest {
        // GIVEN
        val testPhone = "test-phone"
        coEvery { validatePhone.invoke(testPhone) } returns false

        viewModel.validationResult.test {
            // WHEN
            viewModel.setActiveRecoveryMethod(RecoveryMethodType.SMS, testPhone)
            viewModel.validateRecoveryDestinationInput()
            // THEN
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.None)
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.Processing)
            val successItem = awaitItem()
            assertTrue(successItem is RecoveryMethodViewModel.ValidationState.Success)
            assertFalse(successItem.value)
            coVerify(exactly = 1) {
                validatePhone.invoke(testPhone)
            }
        }
    }

    @Test
    fun `phone validation API error`() = coroutinesTest {
        // GIVEN
        val testPhone = "test-phone"
        coEvery { validatePhone.invoke(testPhone) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "API error validation."
                )
            )
        )

        viewModel.validationResult.test {
            // WHEN
            viewModel.setActiveRecoveryMethod(RecoveryMethodType.SMS, testPhone)
            viewModel.validateRecoveryDestinationInput()
            // THEN
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.None)
            assertTrue(awaitItem() is RecoveryMethodViewModel.ValidationState.Processing)
            val errorItem = awaitItem()
            assertTrue(errorItem is RecoveryMethodViewModel.ValidationState.Error)

            coVerify(exactly = 1) {
                validatePhone.invoke(testPhone)
            }
        }
    }
}

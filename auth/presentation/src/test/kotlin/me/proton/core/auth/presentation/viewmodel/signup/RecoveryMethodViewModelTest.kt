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

package me.proton.core.auth.presentation.viewmodel.signup

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.proton.core.auth.domain.usecase.signup.ValidateEmail
import me.proton.core.auth.domain.usecase.signup.ValidatePhone
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.flowTest
import me.proton.core.util.kotlin.coroutine.result
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecoveryMethodViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {
    // region mocks
    private val validateEmail = mockk<ValidateEmail>(relaxed = true)
    private val validatePhone = mockk<ValidatePhone>(relaxed = true)
    private val telemetryManager = mockk<TelemetryManager>(relaxed = true)
    // endregion

    private lateinit var viewModel: RecoveryMethodViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = RecoveryMethodViewModel(validateEmail, validatePhone, telemetryManager)
    }

    @Test
    fun `email validation returns true`() = coroutinesTest {
        // GIVEN
        val testEmail = "test-email"
        coEvery { validateEmail.invoke(testEmail) } coAnswers {
            result("validateEmail") { true }
        }

        flowTest(viewModel.validationResult) {
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

            verifyTelemetryEventEnqueued().apply {
                assertEquals("user.recovery_method.verify", name)
                assertEquals(
                    ProductMetricsDelegate.VALUE_SUCCESS,
                    dimensions[ProductMetricsDelegate.KEY_RESULT]
                )
            }
        }
    }

    @Test
    fun `email validation returns false`() = coroutinesTest {
        // GIVEN
        val testEmail = "test-email"
        coEvery { validateEmail.invoke(testEmail) } returns false

        flowTest(viewModel.validationResult) {
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

        flowTest(viewModel.validationResult) {
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
        coEvery { validatePhone.invoke(testPhone) } coAnswers {
            result("validateEmail") { true }
        }

        flowTest(viewModel.validationResult) {
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

            verifyTelemetryEventEnqueued().apply {
                assertEquals("user.recovery_method.verify", name)
                assertEquals(
                    ProductMetricsDelegate.VALUE_SUCCESS,
                    dimensions[ProductMetricsDelegate.KEY_RESULT]
                )
            }
        }
    }

    @Test
    fun `phone validation returns false`() = coroutinesTest {
        // GIVEN
        val testPhone = "test-phone"
        coEvery { validatePhone.invoke(testPhone) } returns false

        flowTest(viewModel.validationResult) {
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

        flowTest(viewModel.validationResult) {
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

    private fun verifyTelemetryEventEnqueued(): TelemetryEvent {
        val eventSlot = slot<TelemetryEvent>()
        verify { telemetryManager.enqueue(null, capture(eventSlot)) }
        return eventSlot.captured
    }
}

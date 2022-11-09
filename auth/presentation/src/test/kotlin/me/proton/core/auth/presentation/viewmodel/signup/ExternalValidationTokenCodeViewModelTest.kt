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
import io.mockk.slot
import me.proton.core.auth.presentation.viewmodel.ExternalValidationTokenCodeViewModel
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.usecase.CheckCreationTokenValidity
import me.proton.core.humanverification.domain.usecase.ResendVerificationCodeToDestination
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExternalValidationTokenCodeViewModelTest : ArchTest, CoroutinesTest {
    // region mocks
    private val resendVerificationCodeToDestination = mockk<ResendVerificationCodeToDestination>(relaxed = true)
    private val checkCreationTokenValidity = mockk<CheckCreationTokenValidity>(relaxed = true)
    // endregion

    private lateinit var viewModel: ExternalValidationTokenCodeViewModel

    @Before
    fun beforeEveryTest() {
        viewModel =
            ExternalValidationTokenCodeViewModel(resendVerificationCodeToDestination, checkCreationTokenValidity)
    }

    @Test
    fun `verification token sent success`() = coroutinesTest {
        // GIVEN
        val testDestination = "test-destination"
        val testCode = "test-code"
        val testToken = viewModel.getToken(testDestination, testCode)
        coEvery {
            checkCreationTokenValidity.invoke(
                null,
                any(),
                TokenType.EMAIL
            )
        } returns Unit
        viewModel.validationState.test {
            // WHEN
            viewModel.validateToken(null, testToken, TokenType.EMAIL)
            // THEN
            assertTrue(awaitItem() is ViewModelResult.Processing)
            val successItem = awaitItem()
            assertTrue(successItem is ViewModelResult.Success)
            assertEquals("test-destination:test-code", successItem.value)

            val destinationTokenSlot = slot<String>()
            coVerify(exactly = 1) {
                checkCreationTokenValidity.invoke(
                    any(),
                    capture(destinationTokenSlot),
                    TokenType.EMAIL
                )
            }
            assertEquals("test-destination:test-code", destinationTokenSlot.captured)
        }
    }

    @Test
    fun `verification token sent error`() = coroutinesTest {
        // GIVEN
        val testDestination = "test-destination"
        val testCode = "test-code"
        val testToken = viewModel.getToken(testDestination, testCode)
        coEvery {
            checkCreationTokenValidity.invoke(
                null,
                any(),
                TokenType.EMAIL
            )
        } throws Exception("Sending to destination error.")

        viewModel.validationState.test {
            // WHEN
            viewModel.validateToken(null, testToken, TokenType.EMAIL)
            // THEN
            assertTrue(awaitItem() is ViewModelResult.Processing)
            val errorItem = awaitItem()
            assertTrue(errorItem is ViewModelResult.Error)
            assertEquals("Sending to destination error.", errorItem.throwable?.message)

            val destinationTokenSlot = slot<String>()
            coVerify(exactly = 1) {
                checkCreationTokenValidity.invoke(
                    null,
                    capture(destinationTokenSlot),
                    TokenType.EMAIL
                )
            }
            assertEquals("test-destination:test-code", destinationTokenSlot.captured)
        }
    }

    @Test
    fun `verification token sent API error`() = coroutinesTest {
        // GIVEN
        val testDestination = "test-destination"
        val testCode = "test-code"
        val testToken = viewModel.getToken(testDestination, testCode)
        coEvery {
            checkCreationTokenValidity.invoke(
                null,
                any(),
                TokenType.EMAIL,
            )
        } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "API error."
                )
            )
        )

        viewModel.validationState.test {
            // WHEN
            viewModel.validateToken(null, testToken, TokenType.EMAIL)
            // THEN
            assertTrue(awaitItem() is ViewModelResult.Processing)
            val errorItem = awaitItem()
            assertTrue(errorItem is ViewModelResult.Error)
            assertEquals("API error.", errorItem.throwable?.message)

            val destinationTokenSlot = slot<String>()
            coVerify(exactly = 1) {
                checkCreationTokenValidity.invoke(
                    null,
                    capture(destinationTokenSlot),
                    TokenType.EMAIL
                )
            }
            assertEquals("test-destination:test-code", destinationTokenSlot.captured)
        }
    }

    @Test
    fun `resend code success`() = coroutinesTest {
        // GIVEN
        val testDestination = "test-destination"
        val testTokenType = TokenType.EMAIL
        coEvery {
            resendVerificationCodeToDestination.invoke(
                sessionId = null,
                tokenType = testTokenType,
                destination = testDestination
            )
        } returns Unit
        viewModel.verificationCodeResendState.test {
            // WHEN
            viewModel.resendCode(null, testDestination, testTokenType)
            // THEN
            assertTrue(awaitItem() is ViewModelResult.Processing)
            val successItem = awaitItem()
            assertTrue(successItem is ViewModelResult.Success)
            assertTrue(successItem.value)

            val destinationTokenSlot = slot<TokenType>()
            coVerify(exactly = 1) {
                resendVerificationCodeToDestination.invoke(
                    sessionId = null,
                    destination = testDestination,
                    tokenType = capture(destinationTokenSlot)
                )
            }
            assertEquals(TokenType.EMAIL, destinationTokenSlot.captured)
        }
    }

    @Test
    fun `resend code error`() = coroutinesTest {
        // GIVEN
        val testDestination = "test-destination"
        val testTokenType = TokenType.EMAIL
        coEvery {
            resendVerificationCodeToDestination.invoke(
                sessionId = null,
                tokenType = testTokenType,
                destination = testDestination
            )
        } throws Exception("Error resending code.")
        viewModel.verificationCodeResendState.test {
            // WHEN
            viewModel.resendCode(null, testDestination, testTokenType)
            // THEN
            assertTrue(awaitItem() is ViewModelResult.Processing)
            val errorItem = awaitItem()
            assertTrue(errorItem is ViewModelResult.Error)
            val throwable = errorItem.throwable
            assertNotNull(throwable)
            assertEquals("Error resending code.", throwable.message)

            val destinationTokenSlot = slot<TokenType>()
            coVerify(exactly = 1) {
                resendVerificationCodeToDestination.invoke(
                    sessionId = null,
                    destination = testDestination,
                    tokenType = capture(destinationTokenSlot)
                )
            }
            assertEquals(TokenType.EMAIL, destinationTokenSlot.captured)
        }
    }

    @Test
    fun `resend code API error`() = coroutinesTest {
        // GIVEN
        val testDestination = "test-destination"
        val testTokenType = TokenType.EMAIL
        coEvery {
            resendVerificationCodeToDestination.invoke(
                sessionId = null,
                tokenType = testTokenType,
                destination = testDestination
            )
        } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "API error resend code."
                )
            )
        )
        viewModel.verificationCodeResendState.test {
            // WHEN
            viewModel.resendCode(null, testDestination, testTokenType)
            // THEN
            assertTrue(awaitItem() is ViewModelResult.Processing)
            val errorItem = awaitItem()
            assertTrue(errorItem is ViewModelResult.Error)
            val throwable = errorItem.throwable
            assertNotNull(throwable)
            assertEquals("API error resend code.", throwable.message)

            val destinationTokenSlot = slot<TokenType>()
            coVerify(exactly = 1) {
                resendVerificationCodeToDestination.invoke(
                    sessionId = null,
                    destination = testDestination,
                    tokenType = capture(destinationTokenSlot)
                )
            }
            assertEquals(TokenType.EMAIL, destinationTokenSlot.captured)
        }
    }
}

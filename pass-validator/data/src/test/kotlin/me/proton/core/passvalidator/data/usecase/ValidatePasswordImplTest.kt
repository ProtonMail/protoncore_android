package me.proton.core.passvalidator.data.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.feature.IsCommonPasswordCheckEnabled
import me.proton.core.passvalidator.data.validator.CommonPasswordValidator
import me.proton.core.passvalidator.data.validator.MinLengthPasswordValidator
import me.proton.core.passvalidator.data.validator.PasswordPolicyValidator
import me.proton.core.passvalidator.domain.entity.PasswordValidationType
import me.proton.core.passvalidator.domain.entity.PasswordValidatorResult
import me.proton.core.presentation.utils.InvalidPasswordProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValidatePasswordImplTest {
    @MockK
    private lateinit var observePasswordPolicyValidators: ObservePasswordPolicyValidators

    @MockK
    private lateinit var isCommonPasswordCheckEnabled: IsCommonPasswordCheckEnabled

    @MockK
    private lateinit var invalidPasswordProvider: InvalidPasswordProvider

    @MockK
    private lateinit var commonPasswordValidator: CommonPasswordValidator

    @MockK
    private lateinit var minLengthPasswordValidator: MinLengthPasswordValidator

    private lateinit var dispatcherProvider: TestDispatcherProvider

    private lateinit var tested: ValidatePasswordImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        dispatcherProvider = TestDispatcherProvider()
        tested = ValidatePasswordImpl(
            dispatcherProvider,
            observePasswordPolicyValidators,
            isCommonPasswordCheckEnabled,
            invalidPasswordProvider,
            commonPasswordValidator,
            minLengthPasswordValidator
        )
    }

    @Test
    fun `default validators only`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { observePasswordPolicyValidators(null) } returns flowOf(emptyList())
        every { isCommonPasswordCheckEnabled(null) } returns false
        every { minLengthPasswordValidator.validate(any()) } returns validatorResult()

        // WHEN
        tested(passwordValidationType = PasswordValidationType.Main, "password", null).test {
            val result = awaitItem()

            // THEN
            assertEquals(1, result.results.size)
            assertNotNull(result.token)
            awaitComplete()

            verify(exactly = 1) { minLengthPasswordValidator.validate(any()) }
        }
    }

    @Test
    fun `password policies with common passwords validator`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val customValidator = mockk<PasswordPolicyValidator> {
            every { this@mockk.validate(any()) } returns validatorResult()
        }
        every { commonPasswordValidator.validate(any()) } returns validatorResult()
        coEvery { observePasswordPolicyValidators(null) } returns flowOf(listOf(customValidator))
        coJustRun { invalidPasswordProvider.init(any()) }
        every { isCommonPasswordCheckEnabled(null) } returns true

        // WHEN
        tested(passwordValidationType = PasswordValidationType.Main, "password", null).test {
            val result = awaitItem()

            // THEN
            assertEquals(2, result.results.size)
            assertNotNull(result.token)
            awaitComplete()

            verify(exactly = 1) { customValidator.validate(any()) }
            verify(exactly = 1) { commonPasswordValidator.validate(any()) }
            verify(exactly = 0) { minLengthPasswordValidator.validate(any()) }
        }
    }

    @Test
    fun `reverts to default if fetching password policies fails`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { observePasswordPolicyValidators(null) } returns flow {
            error("Cannot fetch")
        }
        every { isCommonPasswordCheckEnabled(null) } returns false
        every { minLengthPasswordValidator.validate(any()) } returns validatorResult(isValid = false)

        // WHEN
        tested(passwordValidationType = PasswordValidationType.Main, "password", null).test {
            val result = awaitItem()

            // THEN
            assertEquals(1, result.results.size)
            assertNull(result.token)
            awaitComplete()

            verify(exactly = 1) { minLengthPasswordValidator.validate(any()) }
        }
    }

    @Test
    fun `custom policy is checked even if cannot load common passwords`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val customValidator = mockk<PasswordPolicyValidator> {
            every { this@mockk.validate(any()) } returns validatorResult(isOptional = true, isValid = false)
        }
        coEvery { observePasswordPolicyValidators(null) } returns flowOf(listOf(customValidator))
        coEvery { invalidPasswordProvider.init(any()) } throws Throwable("Cannot load common passwords")
        every { isCommonPasswordCheckEnabled(null) } returns true

        // WHEN
        tested(passwordValidationType = PasswordValidationType.Main, "password", null).test {
            val result = awaitItem()

            // THEN
            assertEquals(1, result.results.size)
            assertNotNull(result.token)
            awaitComplete()

            coVerify(exactly = 1) { invalidPasswordProvider.init(any()) }
            verify(exactly = 1) { customValidator.validate(any()) }
            verify(exactly = 0) { commonPasswordValidator.validate(any()) }
            verify(exactly = 0) { minLengthPasswordValidator.validate(any()) }
        }
    }

    @Test
    fun `default validators for secondary password`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        every { isCommonPasswordCheckEnabled(null) } returns false
        every { minLengthPasswordValidator.validate(any()) } returns validatorResult()

        // WHEN
        tested(passwordValidationType = PasswordValidationType.Secondary, "password", null).test {
            val result = awaitItem()

            // THEN
            assertEquals(1, result.results.size)
            assertNotNull(result.token)
            awaitComplete()

            verify(exactly = 1) { minLengthPasswordValidator.validate(any()) }
        }
    }

    private fun validatorResult(isOptional: Boolean = false, isValid: Boolean = true) = PasswordValidatorResult(
        errorMessage = "",
        hideIfValid = false,
        isOptional = isOptional,
        isValid = isValid,
        requirementMessage = ""
    )
}
package me.proton.core.passvalidator.presentation.report

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.passvalidator.domain.entity.PasswordValidationType
import me.proton.core.passvalidator.domain.entity.PasswordValidatorResult
import me.proton.core.passvalidator.domain.entity.PasswordValidatorToken
import me.proton.core.passvalidator.domain.usecase.ValidatePassword
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PasswordPolicyReportViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var validatePassword: ValidatePassword

    private lateinit var tested: PasswordPolicyReportViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = PasswordPolicyReportViewModel(validatePassword = validatePassword)
    }

    @Test
    fun `empty results`() = runTest {
        // GIVEN
        every { validatePassword(any(), any(), any()) } returns flowOf(
            ValidatePassword.Result(
                emptyList(),
                FakeToken()
            )
        )

        // WHEN
        tested.state.test {
            // THEN
            assertIs<PasswordPolicyReportState.Loading>(awaitItem())

            // WHEN
            tested.perform(
                PasswordPolicyReportAction.Validate(
                    passwordValidationType = PasswordValidationType.Main,
                    password = "password",
                    userId = null
                )
            )

            // THEN
            assertIs<PasswordPolicyReportState.Hidden>(awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `validation results`() = runTest {
        // GIVEN
        every { validatePassword(any(), any(), any()) } answers {
            flowOf(
                ValidatePassword.Result(emptyList(), null),
                ValidatePassword.Result(testValidatorResults, null)
            )
        }

        // WHEN
        tested.state.test {
            // THEN
            assertIs<PasswordPolicyReportState.Loading>(awaitItem())

            // WHEN
            tested.perform(
                PasswordPolicyReportAction.Validate(
                    passwordValidationType = PasswordValidationType.Main,
                    password = "password",
                    userId = null
                )
            )

            // THEN
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Hidden>(it)
                assertNull(it.token)
            }
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Idle>(it)
                assertEquals(
                    listOf(
                        PasswordPolicyReportMessage.Requirement("requirement1", success = true),
                        PasswordPolicyReportMessage.Requirement("requirement2", success = false),
                        PasswordPolicyReportMessage.Error("error5")
                    ),
                    it.messages
                )
                assertNull(it.token)
            }
        }
    }

    @Test
    fun `hint with error`() = runTest {
        // GIVEN
        every { validatePassword(any(), any(), any()) } answers {
            flowOf(
                ValidatePassword.Result(emptyList(), FakeToken()),
                ValidatePassword.Result(smallResults, FakeToken())
            )
        }

        // WHEN
        tested.state.test {
            // THEN
            assertIs<PasswordPolicyReportState.Loading>(awaitItem())

            // WHEN
            tested.perform(
                PasswordPolicyReportAction.Validate(
                    passwordValidationType = PasswordValidationType.Main,
                    password = "password",
                    userId = null
                )
            )

            // THEN
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Hidden>(it)
                assertNotNull(it.token)
            }
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Idle>(it)
                assertEquals(
                    listOf(PasswordPolicyReportMessage.Hint("error1", success = true)),
                    it.messages
                )
                assertNotNull(it.token)
            }
        }
    }

    @Test
    fun `runtime error`() = runTest {
        every { validatePassword(any(), any(), any()) } returns flow {
            error("unexpected error")
        }

        // WHEN
        tested.state.test {
            // THEN
            assertIs<PasswordPolicyReportState.Loading>(awaitItem())

            // WHEN
            tested.perform(
                PasswordPolicyReportAction.Validate(
                    passwordValidationType = PasswordValidationType.Main,
                    password = "password",
                    userId = null
                )
            )

            // THEN
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Hidden>(it)
                assertNull(it.token)
            }
        }
    }

    private val testValidatorResults = listOf(
        PasswordValidatorResult(
            errorMessage = "error1",
            requirementMessage = "requirement1",
            hideIfValid = false,
            isValid = true,
            isOptional = false
        ),
        PasswordValidatorResult(
            errorMessage = "error2",
            requirementMessage = "requirement2",
            hideIfValid = false,
            isValid = false,
            isOptional = false
        ),
        PasswordValidatorResult(
            errorMessage = "error3",
            requirementMessage = "requirement3",
            hideIfValid = true,
            isValid = true,
            isOptional = false
        ),
        PasswordValidatorResult(
            errorMessage = "error4",
            requirementMessage = "requirement4",
            hideIfValid = false,
            isValid = null,
            isOptional = false
        ),
        PasswordValidatorResult(
            errorMessage = "error5",
            requirementMessage = "requirement5",
            hideIfValid = true,
            isValid = false,
            isOptional = false
        ),
    )

    private val smallResults = listOf(
        PasswordValidatorResult(
            errorMessage = "error1",
            requirementMessage = "requirement1",
            hideIfValid = false,
            isValid = true,
            isOptional = false
        ),
        PasswordValidatorResult(
            errorMessage = "error2",
            requirementMessage = "requirement2",
            hideIfValid = true,
            isValid = true,
            isOptional = false
        )
    )
}

private class FakeToken : PasswordValidatorToken

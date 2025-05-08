package me.proton.core.passvalidator.presentation.report

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.passvalidator.domain.entity.PasswordValidatorResult
import me.proton.core.passvalidator.domain.usecase.ValidatePassword
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

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
        every { validatePassword(any(), any()) } returns flowOf(emptyList())

        // WHEN
        tested.state.test {
            // THEN
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Loading>(it)
                assertFalse(it.allPassed())
            }

            // WHEN
            tested.perform(PasswordPolicyReportAction.Validate(password = "password", userId = null))

            // THEN
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Hidden>(it)
                assertTrue(it.allPassed())
            }
            expectNoEvents()
        }
    }

    @Test
    fun `validation results`() = runTest {
        // GIVEN
        every { validatePassword(any(), any()) } answers {
            flowOf(emptyList(), testValidatorResults)
        }

        // WHEN
        tested.state.test {
            // THEN
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Loading>(it)
                assertFalse(it.allPassed())
            }

            // WHEN
            tested.perform(PasswordPolicyReportAction.Validate(password = "password", userId = null))

            // THEN
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Hidden>(it)
                assertTrue(it.allPassed())
            }
            awaitItem().let {
                assertIs<PasswordPolicyReportState.Idle>(it)
                assertFalse(it.allPassed())
                assertEquals(
                    listOf(
                        PasswordPolicyReportMessage.Requirement("requirement1", success = true),
                        PasswordPolicyReportMessage.Requirement("requirement2", success = false),
                        PasswordPolicyReportMessage.Error("error5")
                    ),
                    it.messages
                )
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
}

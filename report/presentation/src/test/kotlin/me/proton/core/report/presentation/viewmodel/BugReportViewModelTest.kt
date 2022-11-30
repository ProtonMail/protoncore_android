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

package me.proton.core.report.presentation.viewmodel

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import me.proton.core.report.domain.entity.BugReportValidationError
import me.proton.core.report.domain.usecase.SendBugReport
import me.proton.core.report.presentation.entity.BugReportFormState
import me.proton.core.report.presentation.entity.ExitSignal
import me.proton.core.report.presentation.entity.ReportFormData
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.flowTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class BugReportViewModelTest : CoroutinesTest by CoroutinesTest() {
    private lateinit var tested: BugReportViewModel
    private lateinit var sendBugReport: SendBugReport

    private val testReportFormData = ReportFormData(
        subject = "Subject",
        description = "Description",
    )
    private val testEmail = "email@test"
    private val testUsername = "testUsername"

    @Before
    fun setUp() {
        sendBugReport = mockk()
        tested = BugReportViewModel(sendBugReport)
    }

    @Test
    fun `send bug report successfully`() = runTest {
        val requestId = "123"
        val expectedResults = mockBugReportResults(
            SendBugReport.Result.Initialized(requestId),
            SendBugReport.Result.Enqueued(requestId),
            SendBugReport.Result.Blocked(requestId),
            SendBugReport.Result.Enqueued(requestId),
            SendBugReport.Result.Sent(requestId)
        )

        val formStateJob = flowTest(tested.bugReportFormState) {
            assertIs<BugReportFormState.Idle>(awaitItem())
            assertIs<BugReportFormState.Processing>(awaitItem())
            expectedResults.forEach { expected ->
                assertEquals(expected, assertIs<BugReportFormState.SendingResult>(awaitItem()).result)
            }
            expectNoEvents()
            cancel()
        }
        tested.trySendingBugReport(
            testReportFormData,
            email = testEmail,
            username = testUsername,
            country = "Switzerland",
            isp = "InternetServiceProvider"
        )
        formStateJob.join()
    }

    @Test
    fun `sending bug report failed`() = runTest {
        val requestId = "123"
        val expectedResults = mockBugReportResults(
            SendBugReport.Result.Initialized(requestId),
            SendBugReport.Result.Enqueued(requestId),
            SendBugReport.Result.Failed(requestId, "Failed")
        )

        val formStateJob = flowTest(tested.bugReportFormState) {
            assertIs<BugReportFormState.Idle>(awaitItem())
            assertIs<BugReportFormState.Processing>(awaitItem())
            expectedResults.forEach { expected ->
                assertEquals(expected, assertIs<BugReportFormState.SendingResult>(awaitItem()).result)
            }
            expectNoEvents()
            cancel()
        }
        tested.trySendingBugReport(
            testReportFormData.copy(),
            email = testEmail,
            username = testUsername,
            country = null,
            isp = null
        )
        formStateJob.join()
    }

    @Test
    fun `invalid form data`() = runTest {
        val formStateJob = flowTest(tested.bugReportFormState) {
            assertIs<BugReportFormState.Idle>(awaitItem())
            assertIs<BugReportFormState.Processing>(awaitItem())
            val formError = assertIs<BugReportFormState.FormError>(awaitItem())
            assertContentEquals(
                listOf(BugReportValidationError.DescriptionMissing),
                formError.errors
            )
            expectNoEvents()
            cancel()
        }
        val invalidFormData = testReportFormData.copy(description = "")
        tested.trySendingBugReport(
            invalidFormData,
            email = testEmail,
            username = testUsername,
            country = null,
            isp = null
        )
        formStateJob.join()
    }

    @Test
    fun `exit with form data`() = runTest {
        val exitSignalJob = flowTest(tested.exitSignal) {
            assertEquals(ExitSignal.ExitNow, awaitItem())
        }
        tested.tryExit()
        exitSignalJob.join()
    }

    @Test
    fun `exit with empty form data`() = runTest {
        val exitSignalJob = flowTest(tested.exitSignal) {
            assertEquals(ExitSignal.ExitNow, awaitItem())
        }
        tested.tryExit(ReportFormData("", ""))
        exitSignalJob.join()
    }

    @Test
    fun `exit with non-empty form data`() = runTest {
        val exitSignalJob = flowTest(tested.exitSignal) {
            assertEquals(ExitSignal.Ask, awaitItem())
        }
        tested.tryExit(ReportFormData("Subject", ""))
        exitSignalJob.join()
    }

    @Test
    fun `force exit with non-empty form data`() = runTest {
        val exitSignalJob = flowTest(tested.exitSignal) {
            assertEquals(ExitSignal.ExitNow, awaitItem())
        }
        tested.tryExit(ReportFormData("", "Description"), force = true)
        exitSignalJob.join()
    }

    @Test
    fun `revalidate subject`() = runTest {
        val formStateJob = flowTest(tested.bugReportFormState) {
            assertIs<BugReportFormState.Idle>(awaitItem())
            assertIs<BugReportFormState.Processing>(awaitItem())
            val formErrors = assertIs<BugReportFormState.FormError>(awaitItem())
            assertContentEquals(
                listOf(BugReportValidationError.SubjectMissing, BugReportValidationError.DescriptionMissing),
                formErrors.errors
            )
            val errorsAfterRevalidation = assertIs<BugReportFormState.FormError>(awaitItem())
            assertContentEquals(
                listOf(BugReportValidationError.DescriptionMissing),
                errorsAfterRevalidation.errors
            )
            expectNoEvents()
            cancel()
        }
        val invalidFormData = ReportFormData(subject = "", description = "")
        tested.trySendingBugReport(
            invalidFormData,
            email = testEmail,
            username = testUsername,
            country = null,
            isp = null
        )
        runCurrent()
        tested.revalidateSubject("Test subject")
        formStateJob.join()
    }

    @Test
    fun `revalidate description`() = runTest {
        val formStateJob = flowTest(tested.bugReportFormState) {
            assertIs<BugReportFormState.Idle>(awaitItem())
            assertIs<BugReportFormState.Processing>(awaitItem())
            val formErrors = assertIs<BugReportFormState.FormError>(awaitItem())
            assertContentEquals(
                listOf(BugReportValidationError.SubjectMissing, BugReportValidationError.DescriptionMissing),
                formErrors.errors
            )
            val errorsAfterRevalidation = assertIs<BugReportFormState.FormError>(awaitItem())
            assertContentEquals(
                listOf(BugReportValidationError.SubjectMissing),
                errorsAfterRevalidation.errors
            )
            expectNoEvents()
            cancel()
        }
        val invalidFormData = ReportFormData(subject = "", description = "")
        tested.trySendingBugReport(
            invalidFormData,
            email = testEmail,
            username = testUsername,
            country = null,
            isp = null
        )
        runCurrent()
        tested.revalidateDescription("Test description")
        formStateJob.join()
    }

    private fun mockBugReportResults(vararg results: SendBugReport.Result): List<SendBugReport.Result> {
        every { sendBugReport.invoke(any(), any()) } returns flow {
            results.forEach { emit(it) }
            yield()
        }
        return results.toList()
    }
}

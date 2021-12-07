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

package me.proton.core.reports.data.work

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.MockKStubScope
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.reports.data.testBugReport
import me.proton.core.reports.data.testBugReportExtra
import me.proton.core.reports.data.testBugReportMeta
import me.proton.core.reports.data.testUserId
import me.proton.core.reports.domain.entity.BugReportExtra
import me.proton.core.reports.domain.repository.ReportsRepository
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.test.assertFailsWith

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
internal class BugReportWorkerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val reportsRepository: ReportsRepository = mockk()

    @Inject
    internal lateinit var hiltWorkerFactory: HiltWorkerFactory

    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun reportsSuccessfullySent() {
        val result = mockAndRun { returns(Unit) }
        assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Success()))
    }

    @Test
    fun reportsWithNoExtraSuccessfullySent() {
        val result = mockAndRun(extra = null) { returns(Unit) }
        assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Success()))
    }

    @Test
    fun reportsWithNoInputData() {
        coJustRun { reportsRepository.sendReport(any(), any(), any(), any()) }
        val worker = TestListenableWorkerBuilder<BugReportWorker>(context, Data.EMPTY)
            .setWorkerFactory(hiltWorkerFactory)
            .build()
        assertFailsWith<IllegalArgumentException> {
            runBlocking { worker.doWork() }
        }
    }

    @Test
    fun reportsWithRecoverableError() {
        val apiException = ApiException(ApiResult.Error.Http(500, "Server error"))
        val result = mockAndRun { throws(apiException) }
        assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Retry()))
    }

    @Test
    fun reportsWithUnrecoverableError() {
        val protonData = ApiResult.Error.ProtonData(0, "Invalid request")
        val apiException = ApiException(ApiResult.Error.Http(400, "Bad request", protonData))
        val result = mockAndRun { throws(apiException) }
        val outputData = workDataOf(BugReportWorker.OUTPUT_ERROR_MESSAGE to "Invalid request")
        assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Failure(outputData)))
    }

    @Test
    fun reportsWithGenericError() {
        val result = mockAndRun { throws(Throwable("Error")) }
        val expectedOutputData = workDataOf(BugReportWorker.OUTPUT_ERROR_MESSAGE to "Error")
        assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Failure(expectedOutputData)))
    }

    private fun mockAndRun(
        extra: BugReportExtra? = testBugReportExtra,
        sendReportAnswer: MockKStubScope<Unit, Unit>.() -> Unit
    ): ListenableWorker.Result {
        coEvery {
            reportsRepository.sendReport(
                testUserId,
                testBugReport,
                testBugReportMeta,
                extra
            )
        }.apply { sendReportAnswer.invoke(this) }

        val inputData = BugReportWorker.makeData(testUserId, testBugReport, testBugReportMeta, extra)
        val worker = TestListenableWorkerBuilder<BugReportWorker>(context, inputData)
            .setWorkerFactory(hiltWorkerFactory)
            .build()
        return runBlocking { worker.doWork() }
    }
}

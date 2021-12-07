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

package me.proton.core.reports.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import app.cash.turbine.test
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.yield
import me.proton.core.reports.data.work.BugReportWorker
import me.proton.core.reports.domain.usecase.SendBugReport
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class SendBugReportImplTest : CoroutinesTest {
    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var tested: SendBugReportImpl
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        workManager = mockk {
            every { enqueue(any<WorkRequest>()) } returns mockk()
        }
        tested = SendBugReportImpl({ testBugReportMeta }, workManager)
    }

    @Test
    fun `sending bug report successful`() = coroutinesTest {
        mockWorkInfoStates(
            mockedWorkInfo(WorkInfo.State.ENQUEUED),
            mockedWorkInfo(WorkInfo.State.RUNNING),
            mockedWorkInfo(WorkInfo.State.SUCCEEDED)
        )

        tested.invoke(testUserId, testBugReport, testBugReportExtra).test {
            assertIs<SendBugReport.Result.Initialized>(awaitItem())
            assertIs<SendBugReport.Result.Enqueued>(awaitItem())
            assertIs<SendBugReport.Result.Sent>(awaitItem())
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun `sending bug report blocked then failed`() = coroutinesTest {
        mockWorkInfoStates(
            mockedWorkInfo(WorkInfo.State.ENQUEUED),
            mockedWorkInfo(WorkInfo.State.BLOCKED),
            mockedWorkInfo(WorkInfo.State.RUNNING),
            mockedWorkInfo(WorkInfo.State.FAILED).apply {
                every { outputData } returns workDataOf(BugReportWorker.OUTPUT_ERROR_MESSAGE to "Failed")
            }
        )

        tested.invoke(testUserId, testBugReport, extra = null).test {
            assertIs<SendBugReport.Result.Initialized>(awaitItem())
            assertIs<SendBugReport.Result.Enqueued>(awaitItem())
            assertIs<SendBugReport.Result.Blocked>(awaitItem())
            val failedResult = assertIs<SendBugReport.Result.Failed>(awaitItem())
            assertEquals("Failed", failedResult.message)
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun `sending bug report cancelled`() = coroutinesTest {
        val uuid = UUID.randomUUID()
        every { workManager.cancelWorkById(uuid) } returns makeSuccessfulOperation()
        tested.cancel(uuid.toString())
        verify { workManager.cancelWorkById(uuid) }
    }

    private fun mockWorkInfoStates(vararg workInfo: WorkInfo) {
        every { workManager.getWorkInfoByIdLiveData(any()) } returns liveData<WorkInfo>(dispatchers.Main) {
            workInfo.forEach {
                emit(it)
                yield()
            }
        }
    }

    private fun mockedWorkInfo(workInfoState: WorkInfo.State): WorkInfo =
        mockk { every { state } returns workInfoState }

    private fun makeSuccessfulOperation(): Operation {
        return object : Operation {
            override fun getState(): LiveData<Operation.State> {
                return MutableLiveData(Operation.SUCCESS)
            }

            override fun getResult(): ListenableFuture<Operation.State.SUCCESS> {
                @Suppress("UnstableApiUsage")
                return Futures.immediateFuture(Operation.SUCCESS)
            }
        }
    }
}

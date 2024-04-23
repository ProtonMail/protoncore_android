/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.push.data.remote.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.MockKAnnotations
import io.mockk.MockKStubScope
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.push.domain.usecase.DeletePushRemote
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertFailsWith

@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
internal class DeletePushWorkerTest {
    private lateinit var context: Context

    private val testPushId = PushId("test-push-id")
    private val testUserId = UserId("test-user-id")
    private val testPushType = PushObjectType.Messages

    @MockK
    private lateinit var pushRepository: PushRepository

    @MockK
    private lateinit var deletePushRemote: DeletePushRemote

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `push successfully deleted`() {
        val result = mockAndRun { returns(Unit) }
        assertEquals(ListenableWorker.Result.Success(), result)
    }

    @Test
    fun `recoverable error`() {
        val apiException = ApiException(ApiResult.Error.Http(500, "Server error"))
        val result = mockAndRun { throws(apiException) }
        assertEquals(ListenableWorker.Result.Retry(), result)
    }

    @Test
    fun `unrecoverable error`() {
        val apiException = ApiException(ApiResult.Error.Http(400, "Bad request"))
        val result = mockAndRun { throws(apiException) }
        assertEquals(ListenableWorker.Result.Failure(), result)
    }

    @Test
    fun `missing input data`() {
        val worker = TestListenableWorkerBuilder<DeletePushWorker>(context, Data.EMPTY)
            .setWorkerFactory(makeWorkerFactory())
            .build()
        assertFailsWith<IllegalArgumentException> {
            runBlocking { worker.doWork() }
        }
    }

    private fun mockAndRun(
        deletePushAnswer: MockKStubScope<Unit, Unit>.() -> Unit
    ): ListenableWorker.Result {
        coEvery { deletePushRemote.invoke(any(),any()) }.apply(deletePushAnswer)
        coEvery { pushRepository.markAsStale(any(), any()) } returns Unit

        val inputData = DeletePushWorker.makeInputData(testUserId, testPushId, testPushType.value)
        val worker = TestListenableWorkerBuilder<DeletePushWorker>(context, inputData)
            .setWorkerFactory(makeWorkerFactory())
            .build()
        return runBlocking { worker.doWork() }
    }

    private fun makeWorkerFactory() = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker = DeletePushWorker(appContext, workerParameters, pushRepository, deletePushRemote)
    }
}

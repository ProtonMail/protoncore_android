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
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.MockKStubScope
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManager
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.remote.PushRemoteDataSource
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.push.domain.usecase.DeletePushRemote
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertFailsWith

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
internal class DeletePushWorkerTest {
    private lateinit var context: Context

    private val testPushId = PushId("test-push-id")
    private val testUserId = UserId("test-user-id")
    private val testPushType = PushObjectType.Messages

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val pushRepository: PushRepository = mockk()

    @BindValue
    @JvmField
    internal val deletePushRemote: DeletePushRemote = mockk()

    @Inject
    internal lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Before
    fun setUp() {
        hiltRule.inject()
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
            .setWorkerFactory(hiltWorkerFactory)
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
            .setWorkerFactory(hiltWorkerFactory)
            .build()
        return runBlocking { worker.doWork() }
    }
}

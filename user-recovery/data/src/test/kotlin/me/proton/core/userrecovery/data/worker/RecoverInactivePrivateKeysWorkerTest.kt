/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.userrecovery.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.userrecovery.data.usecase.RecoverInactivePrivateKeys
import me.proton.core.userrecovery.data.worker.RecoverInactivePrivateKeysWorker.Companion.getWorkData
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
class RecoverInactivePrivateKeysWorkerTest {
    @MockK
    private lateinit var recoverInactivePrivateKeys: RecoverInactivePrivateKeys

    private lateinit var context: Context

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `recover private keys success`() = runTest {
        // GIVEN
        val testUserId = UserId("test_user_id")
        val worker = getWorker(testUserId)
        coJustRun { recoverInactivePrivateKeys(testUserId) }

        // WHEN
        val result = worker.doWork()

        // THEN
        assertIs<ListenableWorker.Result.Success>(result)
    }

    @Test
    fun `recover private keys API exception`() = runTest {
        // GIVEN
        val testUserId = UserId("test_user_id")
        val worker = getWorker(testUserId)
        coEvery { recoverInactivePrivateKeys(testUserId) } throws
                ApiException(ApiResult.Error.Timeout(isConnectedToNetwork = true))

        // WHEN
        val result = worker.doWork()

        // THEN
        assertIs<ListenableWorker.Result.Retry>(result)
    }

    @Test
    fun `recover private keys error`() = runTest {
        // GIVEN
        val testUserId = UserId("test_user_id")
        val worker = getWorker(testUserId)
        coEvery { recoverInactivePrivateKeys(testUserId) } throws Throwable("Unexpected")

        // WHEN
        val result = worker.doWork()

        // THEN
        assertIs<ListenableWorker.Result.Failure>(result)
    }

    private fun getWorker(userId: UserId): RecoverInactivePrivateKeysWorker =
        TestListenableWorkerBuilder<RecoverInactivePrivateKeysWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker =
                    RecoverInactivePrivateKeysWorker(context, workerParameters, recoverInactivePrivateKeys)
            })
            .setInputData(getWorkData(userId))
            .build()

}
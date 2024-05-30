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
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.network.domain.ResponseCodes.NOT_NULL
import me.proton.core.user.domain.usecase.GetUser
import me.proton.core.userrecovery.domain.usecase.SetRecoverySecretRemote
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class SetRecoverySecretWorkerTest {

    private lateinit var context: Context
    private lateinit var setRecoverySecretRemote: SetRecoverySecretRemote

    @MockK(relaxed = true)
    private lateinit var getUser: GetUser

    private val userId = UserId("user-id")

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        context = ApplicationProvider.getApplicationContext()
        setRecoverySecretRemote = mockk(relaxUnitFun = true)
    }

    @Test
    fun success() = runTest {
        coEvery { setRecoverySecretRemote(userId) } returns Unit

        val result = makeWorker(userId).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun successAfterUpgrade() = runTest {
        coEvery { setRecoverySecretRemote(userId) } throws ApiException(ApiResult.Error.Http(HTTP_UNPROCESSABLE, "Recovery secret already set", ApiResult.Error.ProtonData(NOT_NULL, "test error")))

        val result = makeWorker(userId).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun retry() = runTest {
        coEvery { setRecoverySecretRemote(userId) } throws ApiException(ApiResult.Error.NoInternet())

        val result = makeWorker(userId).doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun failure() = runTest {
        coEvery { setRecoverySecretRemote(userId) } throws IllegalStateException()

        val result = makeWorker(userId).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
    }

    private fun makeWorker(userId: UserId): SetRecoverySecretWorker =
        TestListenableWorkerBuilder<SetRecoverySecretWorker>(context)
            // hilt is not working for this test
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = SetRecoverySecretWorker(appContext, workerParameters, setRecoverySecretRemote, getUser)

            })
            .setInputData(SetRecoverySecretWorker.getWorkData(userId))
            .build()
}

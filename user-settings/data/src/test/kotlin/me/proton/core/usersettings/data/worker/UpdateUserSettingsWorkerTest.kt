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

package me.proton.core.usersettings.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.usersettings.data.extension.toUserSettingsPropertySerializable
import me.proton.core.usersettings.domain.entity.UserSettingsProperty
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import me.proton.core.usersettings.domain.usecase.UpdateUserSettingsRemote
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class UpdateUserSettingsWorkerTest {

    private lateinit var context: Context
    private lateinit var repository: UserSettingsRepository
    private lateinit var updateUserSettingsRemote: UpdateUserSettingsRemote

    private val userId = UserId("user-id")
    private val property = UserSettingsProperty.CrashReports(true)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = mockk(relaxUnitFun = true)
        updateUserSettingsRemote = mockk(relaxUnitFun = true)
    }

    @Test
    fun success() = runTest {
        coEvery { updateUserSettingsRemote(userId, property) } returns Unit

        val result = makeWorker(userId, property).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { repository wasNot called }
    }

    @Test
    fun retry() = runTest {
        coEvery { updateUserSettingsRemote(userId, property) } throws ApiException(ApiResult.Error.NoInternet())

        val result = makeWorker(userId, property).doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify { repository wasNot called }
    }

    @Test
    fun failure() = runTest {
        coEvery { updateUserSettingsRemote(userId, property) } throws IllegalStateException()

        val result = makeWorker(userId, property).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        coVerify { repository.markAsStale(userId) }
    }

    private fun makeWorker(userId: UserId, property: UserSettingsProperty): UpdateUserSettingsWorker =
        TestListenableWorkerBuilder<UpdateUserSettingsWorker>(context)
            // hilt is not working for this test
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = UpdateUserSettingsWorker(appContext, workerParameters, updateUserSettingsRemote, repository)

            })
            .setInputData(UpdateUserSettingsWorker.getWorkData(userId, property.toUserSettingsPropertySerializable()))
            .build()

}

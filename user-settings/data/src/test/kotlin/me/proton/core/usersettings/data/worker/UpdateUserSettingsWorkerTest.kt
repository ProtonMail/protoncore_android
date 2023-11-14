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
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.test.assertEquals


@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class UpdateUserSettingsWorkerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var hiltWorkerFactory: HiltWorkerFactory

    @BindValue
    internal lateinit var updateUserSettingsRemote: UpdateUserSettingsRemote

    @BindValue
    internal lateinit var repository: UserSettingsRepository

    private lateinit var context: Context

    private val userId = UserId("user-id")
    private val property = UserSettingsProperty.CrashReports(true)

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        updateUserSettingsRemote = mockk(relaxUnitFun = true)
        repository = mockk(relaxUnitFun = true)
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
            .setWorkerFactory(hiltWorkerFactory)
            .setInputData(UpdateUserSettingsWorker.getWorkData(userId, property.toUserSettingsPropertySerializable()))
            .build()

}

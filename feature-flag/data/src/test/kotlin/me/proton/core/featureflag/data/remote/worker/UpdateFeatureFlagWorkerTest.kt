/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.featureflag.data.remote.worker

import android.content.Context
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import me.proton.core.featureflag.data.testdata.UserIdTestData
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRemoteDataSource
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateFeatureFlagWorkerTest : CoroutinesTest by UnconfinedCoroutinesTest() {

    private val userId = UserIdTestData.userId
    private val featureId = FeatureId("feature-flag-mail")
    private val featureFlagValue = true

    private val context = mockk<Context>()
    private val parameters = mockk<WorkerParameters> {
        every { inputData.getString(UpdateFeatureFlagWorker.INPUT_USER_ID) } returns userId.id
        every { inputData.getString(UpdateFeatureFlagWorker.INPUT_FEATURE_ID) } returns featureId.id
        every { inputData.getBoolean(UpdateFeatureFlagWorker.INPUT_FEATURE_VALUE, false) } returns featureFlagValue
        every { this@mockk.taskExecutor } returns mockk(relaxed = true)
    }
    private val remoteDataSource: FeatureFlagRemoteDataSource = mockk()
    private val localDataSource: FeatureFlagLocalDataSource = mockk(relaxUnitFun = true)

    private val worker = UpdateFeatureFlagWorker(
        context,
        parameters,
        remoteDataSource,
        localDataSource
    )

    @Test
    fun `get request builds a request with the required data`() = runTest {
        // given
        val userId = UserIdTestData.userId
        val featureId = FeatureId("feature-flag-mail")
        val featureFlagValue = true

        // when
        val actual = UpdateFeatureFlagWorker.getRequest(userId, featureId, featureFlagValue)

        // then
        val actualUserId = actual.workSpec.input.getString(UpdateFeatureFlagWorker.INPUT_USER_ID)
        val actualFeatureId = actual.workSpec.input.getString(UpdateFeatureFlagWorker.INPUT_FEATURE_ID)
        val actualFeatureValue = actual.workSpec.input.getBoolean(UpdateFeatureFlagWorker.INPUT_FEATURE_VALUE, false)
        assertEquals(userId.id, actualUserId)
        assertEquals(featureId.id, actualFeatureId)
        assertEquals(featureFlagValue, actualFeatureValue)
    }


    @Test
    fun `get request builds a request with connected network constraint`() = runTest {
        // when
        val actual = UpdateFeatureFlagWorker.getRequest(userId, featureId, featureFlagValue)

        // then
        val networkConstraint = actual.workSpec.constraints.requiredNetworkType
        assertEquals(NetworkType.CONNECTED, networkConstraint)
    }

    @Test
    fun `worker returns success when API calls succeeds`() = runTest {
        // given
        coEvery { remoteDataSource.update(userId, featureId, featureFlagValue) } returns mockk()

        // when
        val actual = worker.doWork()

        // then
        assertEquals(androidx.work.ListenableWorker.Result.success(), actual)
    }

    @Test
    fun `worker returns retry when API calls fails with retryable exception`() = runTest {
        // given
        coEvery {
            remoteDataSource.update(userId, featureId, featureFlagValue)
        } throws ApiException(ApiResult.Error.NoInternet())

        // when
        val actual = worker.doWork()

        // then
        assertEquals(androidx.work.ListenableWorker.Result.retry(), actual)
    }

    @Test
    fun `worker returns failure when API calls fails with non retryable exception`() = runTest {
        // given
        coEvery {
            remoteDataSource.update(userId, featureId, featureFlagValue)
        } throws ApiException(ApiResult.Error.Parse(SerializationException()))

        // when
        val actual = worker.doWork()

        // then
        coVerify { localDataSource.updateValue(userId, featureId, featureFlagValue.not()) }
        assertEquals(androidx.work.ListenableWorker.Result.failure(), actual)
    }
}

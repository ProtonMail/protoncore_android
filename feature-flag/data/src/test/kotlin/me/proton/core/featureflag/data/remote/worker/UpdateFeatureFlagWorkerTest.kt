/*
 * Copyright (c) 2022 Proton AG
 *
 * This file is part of Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see https://www.gnu.org/licenses/.
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
import me.proton.core.featureflag.data.remote.FeaturesApi
import me.proton.core.featureflag.data.remote.request.PutFeatureFlagBody
import me.proton.core.featureflag.data.remote.response.PutFeatureResponse
import me.proton.core.featureflag.data.testdata.UserIdTestData
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.UnknownHostException

class UpdateFeatureFlagWorkerTest : CoroutinesTest by UnconfinedCoroutinesTest() {

    private val userId = UserIdTestData.userId
    private val featureId = FeatureId("feature-flag-mail")
    private val featureFlagValue = true
    private val nonRetryableException = SerializationException()
    private val retryableException = UnknownHostException()

    private val context = mockk<Context>()
    private val parameters = mockk<WorkerParameters> {
        every { inputData.getString(UpdateFeatureFlagWorker.INPUT_USER_ID) } returns userId.id
        every { inputData.getString(UpdateFeatureFlagWorker.INPUT_FEATURE_ID) } returns featureId.id
        every { inputData.getBoolean(UpdateFeatureFlagWorker.INPUT_FEATURE_VALUE, false) } returns featureFlagValue
        every { this@mockk.taskExecutor } returns mockk(relaxed = true)
    }
    private val featuresApi: FeaturesApi = mockk()
    private val apiProvider: ApiProvider = mockk {
        coEvery { get<FeaturesApi>(userId).invoke<PutFeatureResponse>(block = any()) } coAnswers {
            val block = secondArg<suspend FeaturesApi.() -> PutFeatureResponse>()
            try {
                ApiResult.Success(block(featuresApi))
            } catch (e: Exception) {
                when (e) {
                    nonRetryableException -> ApiResult.Error.Parse(e)
                    retryableException -> ApiResult.Error.Connection()
                    else -> throw e
                }
            }
        }
    }
    private val localDataSource: FeatureFlagLocalDataSource = mockk(relaxUnitFun = true)

    private val worker = UpdateFeatureFlagWorker(
        context,
        parameters,
        apiProvider,
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
        coEvery { featuresApi.putFeatureFlag(featureId.id, PutFeatureFlagBody(featureFlagValue)) } returns mockk()

        // when
        val actual = worker.doWork()

        // then
        assertEquals(androidx.work.ListenableWorker.Result.success(), actual)
    }

    @Test
    fun `worker returns retry when API calls fails with retryable exception`() = runTest {
        // given
        coEvery {
            featuresApi.putFeatureFlag(
                featureId.id,
                PutFeatureFlagBody(featureFlagValue)
            )
        } throws retryableException

        // when
        val actual = worker.doWork()

        // then
        assertEquals(androidx.work.ListenableWorker.Result.retry(), actual)
    }

    @Test
    fun `worker returns failure when API calls fails with non retryable exception`() = runTest {
        // given
        coEvery {
            featuresApi.putFeatureFlag(
                featureId.id,
                PutFeatureFlagBody(featureFlagValue)
            )
        } throws nonRetryableException

        // when
        val actual = worker.doWork()

        // then
        assertEquals(androidx.work.ListenableWorker.Result.failure(), actual)
    }

    @Test
    fun `worker rollbacks local feature flag value when API calls fails with non retryable exception`() = runTest {
        // given
        coEvery {
            featuresApi.putFeatureFlag(
                featureId.id,
                PutFeatureFlagBody(featureFlagValue)
            )
        } throws nonRetryableException

        // when
        worker.doWork()

        // then
        val expected = FeatureFlag(userId, featureId, Scope.User, false, featureFlagValue.not())
        coVerify { localDataSource.upsert(listOf(expected)) }
    }
}
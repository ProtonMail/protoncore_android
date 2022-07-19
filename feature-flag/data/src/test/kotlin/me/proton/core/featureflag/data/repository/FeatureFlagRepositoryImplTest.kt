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

package me.proton.core.featureflag.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.cash.turbine.test
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.db.FeatureFlagDao
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.data.entity.FeatureFlagEntity
import me.proton.core.featureflag.data.local.FeatureFlagLocalDataSourceImpl
import me.proton.core.featureflag.data.local.orGlobal
import me.proton.core.featureflag.data.local.withGlobal
import me.proton.core.featureflag.data.remote.FeatureFlagRemoteDataSourceImpl
import me.proton.core.featureflag.data.remote.FeaturesApi
import me.proton.core.featureflag.data.remote.response.GetFeaturesResponse
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData
import me.proton.core.featureflag.data.testdata.SessionIdTestData
import me.proton.core.featureflag.data.testdata.UserIdTestData.userId
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

class FeatureFlagRepositoryImplTest {

    private val featureFlagDao = mockk<FeatureFlagDao> {
        coEvery { this@mockk.insertOrUpdate(any<FeatureFlagEntity>()) } just Runs
        coEvery { this@mockk.insertOrUpdate(*anyVararg<FeatureFlagEntity>()) } just Runs
    }
    private val database = mockk<FeatureFlagDatabase> {
        every { this@mockk.featureFlagDao() } returns featureFlagDao
    }
    private val sessionProvider = mockk<SessionProvider> {
        coEvery { this@mockk.getSessionId(userId) } returns SessionIdTestData.sessionId
    }
    private val featuresApi = mockk<FeaturesApi> {
        coEvery { this@mockk.getFeatureFlags(any()) } returns GetFeaturesResponse(
            1000,
            listOf(FeatureFlagTestData.enabledFeatureApiResponse)
        )
    }
    private val apiManagerFactory = mockk<ApiManagerFactory> {
        every { this@mockk.create(any(), interfaceClass = FeaturesApi::class) } returns TestApiManager(featuresApi)
    }
    private val workManager = mockk<WorkManager>(relaxed = true)

    private lateinit var repository: FeatureFlagRepository

    @Before
    fun setUp() {
        repository = FeatureFlagRepositoryImpl(
            FeatureFlagLocalDataSourceImpl(database),
            FeatureFlagRemoteDataSourceImpl(ApiProvider(apiManagerFactory, sessionProvider)),
            workManager,
        )
    }

    @Test
    fun featureFlagIsReturnedFromDbWhenAvailable() = runBlockingTest {
        // Given
        coEvery {
            featureFlagDao.observe(
                userId.withGlobal(),
                listOf(FeatureFlagTestData.featureId.id)
            )
        } returns flowOf(listOf(FeatureFlagTestData.enabledFeatureEntity))

        // When
        val actual = repository.get(userId, FeatureFlagTestData.featureId)

        // Then
        val expected = FeatureFlag(
            featureId = FeatureFlagTestData.featureId,
            value = true,
            userId = userId,
            isGlobal = false,
            defaultValue = true
        )
        assertEquals(expected, actual)
        verify { featuresApi wasNot Called }
    }

    @Test
    fun getUserSpecificFeatureFlagIsReturnedFromDbWhenGlobalIsAlsoAvailable() = runBlockingTest {
        // Given
        val nullUserId: UserId? = null
        coEvery {
            featureFlagDao.observe(
                userId.withGlobal(),
                listOf(FeatureFlagTestData.featureId.id)
            )
        } returns flowOf(
            listOf(
                FeatureFlagTestData.enabledFeatureEntity.copy(
                    userId = userId,
                    isGlobal = false,
                    value = false
                ),
                FeatureFlagTestData.enabledFeatureEntity.copy(
                    userId = nullUserId.orGlobal(),
                    isGlobal = true,
                    value = true
                )
            )
        )

        // When
        val actual = repository.get(userId, FeatureFlagTestData.featureId)

        // Then
        val expected = FeatureFlag(
            featureId = FeatureFlagTestData.featureId,
            value = false,
            userId = userId,
            isGlobal = false,
            defaultValue = true
        )
        assertEquals(expected, actual)
        verify { featuresApi wasNot Called }
    }

    @Test
    fun observeUserSpecificFeatureFlagIsReturnedFromDbWhenGlobalIsAlsoAvailable() = runBlockingTest {
        // Given
        val nullUserId: UserId? = null
        coEvery {
            featureFlagDao.observe(
                userId.withGlobal(),
                listOf(FeatureFlagTestData.featureId.id)
            )
        } returns flowOf(
            listOf(
                FeatureFlagTestData.enabledFeatureEntity.copy(
                    userId = userId,
                    isGlobal = false,
                    value = false
                ),
                FeatureFlagTestData.enabledFeatureEntity.copy(
                    userId = nullUserId.orGlobal(),
                    isGlobal = true,
                    value = true
                )
            )
        )

        // When
        repository.observe(userId, FeatureFlagTestData.featureId).test {
            // Then
            assertEquals(
                expected = FeatureFlag(
                    featureId = FeatureFlagTestData.featureId,
                    value = false,
                    userId = userId,
                    isGlobal = false,
                    defaultValue = true
                ),
                actual = awaitItem(),
            )
        }
        verify { featuresApi wasNot Called }
    }

    @Test
    fun featureFlagValueIsObservedInDb() = runBlockingTest {
        // Given
        val mutableDbFlow = MutableStateFlow(listOf(FeatureFlagTestData.enabledFeatureEntity))
        coEvery {
            featureFlagDao.observe(
                userId.withGlobal(),
                listOf(FeatureFlagTestData.featureId.id)
            )
        } returns mutableDbFlow

        // When
        repository.observe(userId, FeatureFlagTestData.featureId).test {
            // Then
            assertEquals(
                expected = FeatureFlag(
                    featureId = FeatureFlagTestData.featureId,
                    value = true,
                    userId = userId,
                    isGlobal = false,
                    defaultValue = true
                ),
                actual = awaitItem(),
            )

            // simulate this feature flag's value changes in the DB
            val disabledFeatures = listOf(FeatureFlagTestData.enabledFeatureEntity.copy(value = false))
            mutableDbFlow.emit(disabledFeatures)
            assertEquals(
                expected = FeatureFlag(
                    featureId = FeatureFlagTestData.featureId,
                    value = false,
                    userId = userId,
                    isGlobal = false,
                    defaultValue = true
                ),
                actual = awaitItem()
            )
        }
        // API should not be called when there are values in DB (update is done through event loop)
        verify { featuresApi wasNot Called }
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun featureFlagValueIsFetchedFromApiAndObservedInDbWhenNotAlreadyAvailableLocally() = runBlocking {
        // Given
        val mutableDbFlow = MutableStateFlow<List<FeatureFlagEntity>>(emptyList())
        coEvery {
            featureFlagDao.observe(
                userId.withGlobal(),
                listOf(FeatureFlagTestData.featureId.id)
            )
        } returns mutableDbFlow

        // When
        repository.observe(userId, FeatureFlagTestData.featureId).test(timeout = 2000.milliseconds) {
            // First item is emitted from DB is null
            assertNull(awaitItem())

            // enabledFeatureFlagEntity is the corresponding entity that the mocked API response
            coVerify(timeout = 500) { featureFlagDao.insertOrUpdate(FeatureFlagTestData.enabledFeatureEntity) }

            // Inserting the API response into DB causes it to be emitted
            mutableDbFlow.emit(listOf(FeatureFlagTestData.enabledFeatureEntity))

            val expectedFlag = FeatureFlag(
                featureId = FeatureFlagTestData.featureId,
                value = true,
                userId = userId,
                isGlobal = false,
                defaultValue = true
            )
            assertEquals(expectedFlag, awaitItem())
        }
    }

    @Test
    fun prefetchFeatureFlagsEnqueueFetchWorker() = runBlockingTest {
        val featureIdsString = "${FeatureFlagTestData.featureId.id},${FeatureFlagTestData.featureId1.id}"
        coEvery { featuresApi.getFeatureFlags(featureIdsString) } returns GetFeaturesResponse(
            1000,
            listOf(FeatureFlagTestData.enabledFeatureApiResponse, FeatureFlagTestData.disabledFeatureApiResponse)
        )

        val featureIds = listOf(FeatureFlagTestData.featureId, FeatureFlagTestData.featureId1)
        repository.prefetch(userId, featureIds)

        val name = "${FeatureFlagRepositoryImpl::class.simpleName}-prefetch-$userId"
        verify { workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, any<OneTimeWorkRequest>()) }
    }
}

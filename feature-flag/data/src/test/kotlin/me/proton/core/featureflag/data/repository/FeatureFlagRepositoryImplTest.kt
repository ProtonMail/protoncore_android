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
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData.disabledFeature
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData.disabledFeatureApiResponse
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData.disabledFeatureEntity
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData.enabledFeature
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData.enabledFeatureApiResponse
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData.enabledFeatureEntity
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData.featureId
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData.featureId1
import me.proton.core.featureflag.data.testdata.SessionIdTestData
import me.proton.core.featureflag.data.testdata.UserIdTestData.userId
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRemoteDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import me.proton.core.test.kotlin.flowTest
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class FeatureFlagRepositoryImplTest : CoroutinesTest by UnconfinedCoroutinesTest() {

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
            resultCode = 1000,
            features = listOf(enabledFeatureApiResponse)
        )
    }
    private val apiManagerFactory = mockk<ApiManagerFactory> {
        every { this@mockk.create(any(), interfaceClass = FeaturesApi::class) } returns TestApiManager(featuresApi)
    }
    private val workManager = mockk<WorkManager>(relaxed = true)

    private lateinit var apiProvider: ApiProvider
    private lateinit var local: FeatureFlagLocalDataSource
    private lateinit var remote: FeatureFlagRemoteDataSource
    private lateinit var repository: FeatureFlagRepository

    @Before
    fun setUp() {
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, coroutinesRule.dispatchers)
        local = spyk(FeatureFlagLocalDataSourceImpl(database))
        remote = spyk(FeatureFlagRemoteDataSourceImpl(apiProvider))
        repository = FeatureFlagRepositoryImpl(
            local,
            remote,
            workManager,
            TestCoroutineScopeProvider(coroutinesRule.dispatchers)
        )
    }

    @Test
    fun featureFlagIsReturnedFromDbWhenAvailable() = coroutinesTest {
        // Given
        val dbFlow = flowOf(listOf(enabledFeatureEntity))
        coEvery { featureFlagDao.observe(userId.withGlobal(), listOf(featureId.id)) } returns dbFlow

        // When
        val actual = repository.get(userId, featureId)

        // Then
        val expected = enabledFeature
        assertEquals(expected, actual)
        verify { featuresApi wasNot Called }
    }

    @Test
    fun getUserSpecificFeatureFlagIsReturnedFromDbWhenGlobalIsAlsoAvailable() = coroutinesTest {
        // Given
        val nullUserId: UserId? = null
        val dbFlow = flowOf(
            listOf(
                enabledFeatureEntity.copy(userId = userId, scope = Scope.User, value = false),
                enabledFeatureEntity.copy(userId = nullUserId.orGlobal(), scope = Scope.Global, value = true)
            )
        )
        coEvery { featureFlagDao.observe(userId.withGlobal(), listOf(featureId.id)) } returns dbFlow

        // When
        val actual = repository.get(userId, featureId)

        // Then
        val expected = FeatureFlag(
            featureId = featureId,
            value = false,
            userId = userId,
            scope = Scope.User,
            defaultValue = true
        )
        assertEquals(expected, actual)
        verify { featuresApi wasNot Called }
    }

    @Test
    fun observeUserSpecificFeatureFlagIsReturnedFromDbWhenGlobalIsAlsoAvailable() = coroutinesTest {
        // Given
        val nullUserId: UserId? = null
        val dbFlow = flowOf(
            listOf(
                enabledFeatureEntity.copy(userId = userId, scope = Scope.User, value = false),
                enabledFeatureEntity.copy(userId = nullUserId.orGlobal(), scope = Scope.Global, value = true)
            )
        )
        coEvery { featureFlagDao.observe(userId.withGlobal(), listOf(featureId.id)) } returns dbFlow

        // When
        repository.observe(userId, featureId).test {
            // Then
            assertEquals(
                expected = FeatureFlag(
                    featureId = featureId,
                    value = false,
                    userId = userId,
                    scope = Scope.User,
                    defaultValue = true
                ),
                actual = awaitItem(),
            )
        }
        verify { featuresApi wasNot Called }
    }

    @Test
    fun featureFlagValueIsObservedInDb() = coroutinesTest {
        // Given
        val mutableDbFlow = MutableStateFlow(listOf(enabledFeatureEntity))
        coEvery { featureFlagDao.observe(userId.withGlobal(), listOf(featureId.id)) } returns mutableDbFlow

        // When
        repository.observe(userId, featureId).test {
            // Then
            assertEquals(
                expected = enabledFeature,
                actual = awaitItem(),
            )

            // simulate this feature flag's value changes in the DB
            val disabledFeatures = listOf(enabledFeatureEntity.copy(value = false))
            mutableDbFlow.emit(disabledFeatures)

            assertEquals(
                expected = enabledFeature.copy(value = false),
                actual = awaitItem()
            )
        }
        // API should not be called when there are values in DB (update is done through event loop)
        verify { featuresApi wasNot Called }
    }

    @Test
    fun featureFlagValueIsFetchedFromApiAndObservedInDbWhenNotAlreadyAvailableLocally() = coroutinesTest {
        // Given
        val mutableDbFlow = MutableStateFlow<List<FeatureFlagEntity>>(emptyList())
        coEvery { featureFlagDao.observe(userId.withGlobal(), listOf(featureId.id)) } returns mutableDbFlow

        // When
        flowTest(repository.observe(userId, featureId)) {
            // Then
            // First item is emitted from DB is null
            assertNull(awaitItem())

            // enabledFeatureFlagEntity is the corresponding entity that the mocked API response
            coVerify { featureFlagDao.insertOrUpdate(enabledFeatureEntity) }

            // Inserting the API response into DB causes it to be emitted
            mutableDbFlow.emit(listOf(enabledFeatureEntity))

            val expected = enabledFeature
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun featureFlagValuesAreFetchedFromApiWhenNotAllAvailableLocally() = coroutinesTest {
        // Given
        val mutableDbFlow = MutableStateFlow(listOf(enabledFeatureEntity))
        coEvery { featureFlagDao.observe(userId.withGlobal(), any()) } returns mutableDbFlow
        coEvery { featuresApi.getFeatureFlags(any()) } returns GetFeaturesResponse(
            resultCode = 1000,
            features = listOf(enabledFeatureApiResponse, disabledFeatureApiResponse)
        )

        // When
        val featureIds = setOf(featureId, featureId1)
        repository.observe(userId, featureIds).test {
            // Then
            // First item is emitted from DB is empty.
            assert(awaitItem().isEmpty())

            // Corresponding entities that the mocked API response.
            coVerify { featureFlagDao.insertOrUpdate(enabledFeatureEntity, disabledFeatureEntity) }

            // Inserting the API response into DB causes it to be emitted.
            mutableDbFlow.emit(listOf(enabledFeatureEntity, disabledFeatureEntity))

            val expected = listOf(enabledFeature, disabledFeature)
            assertEquals(expected, awaitItem())
        }
        coVerify(Ordering.ORDERED) {
            remote.get(userId, featureIds)
            local.upsert(any())
        }
    }

    @Test
    fun upsertUnknownRequestedFlagsInDbAsUnknown() = coroutinesTest {
        // Given
        val nullUserId: UserId? = null
        val unknownId = FeatureId("unknown")
        val unknownFlagEntity = FeatureFlagEntity(
            userId = nullUserId.orGlobal(), // Unknown are global by default.
            featureId = unknownId.id,
            scope = Scope.Unknown, // Unknown.
            defaultValue = false, // Unknown default is false.
            value = false // Unknown default is false.
        )

        val mutableDbFlow = MutableStateFlow(emptyList<FeatureFlagEntity>())
        coEvery { featureFlagDao.observe(userId.withGlobal(), any()) } returns mutableDbFlow

        // When
        val featureIds = setOf(featureId, unknownId)
        repository.observe(userId, featureIds).test {
            // First item is emitted from DB is empty.
            assert(awaitItem().isEmpty())

            // Corresponding entities that the mocked API response.
            coVerify {
                featureFlagDao.insertOrUpdate(
                    enabledFeatureEntity,
                    unknownFlagEntity
                )
            }

            // Inserting the API response into DB causes it to be emitted.
            mutableDbFlow.emit(listOf(enabledFeatureEntity, unknownFlagEntity))

            // Unknown flag is filtered out.
            val expected = listOf(enabledFeature)
            assertEquals(expected, awaitItem())
        }
        coVerify(Ordering.ORDERED) {
            remote.get(userId, featureIds)
            local.upsert(any())
        }
    }

    @Test
    fun prefetchFeatureFlagsEnqueueFetchWorker() = coroutinesTest {
        val featureIdsString = "${featureId.id},${featureId1.id}"
        coEvery { featuresApi.getFeatureFlags(featureIdsString) } returns GetFeaturesResponse(
            resultCode = 1000,
            features = listOf(enabledFeatureApiResponse, disabledFeatureApiResponse)
        )

        val featureIds = setOf(featureId, featureId1)
        repository.prefetch(userId, featureIds)

        val name = "${FeatureFlagRepositoryImpl::class.simpleName}-prefetch-$userId"
        verify { workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, any<OneTimeWorkRequest>()) }
    }
}

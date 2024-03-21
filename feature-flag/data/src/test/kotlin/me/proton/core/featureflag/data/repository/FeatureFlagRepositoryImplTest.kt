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
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CancellationException
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
import me.proton.core.featureflag.data.remote.response.GetUnleashTogglesResponse
import me.proton.core.featureflag.data.remote.worker.FeatureFlagWorkerManager
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData
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
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.FeatureFlagAwaitTotal
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import me.proton.core.test.kotlin.flowTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FeatureFlagRepositoryImplTest : CoroutinesTest by UnconfinedCoroutinesTest() {

    private val featureFlagDao = mockk<FeatureFlagDao> {
        coEvery { this@mockk.getAll(any<Scope>()) } returns emptyList<FeatureFlagEntity>()
        coEvery { this@mockk.insertOrUpdate(any<FeatureFlagEntity>()) } just Runs
        coEvery { this@mockk.insertOrUpdate(*anyVararg<FeatureFlagEntity>()) } just Runs
        coEvery { this@mockk.deleteAll(any<UserId>(), any<Scope>()) } just Runs
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
        every {
            this@mockk.create(
                any(),
                interfaceClass = FeaturesApi::class
            )
        } returns TestApiManager(featuresApi)
    }
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val workerManager = mockk<FeatureFlagWorkerManager>(relaxed = true)

    private val observabilityManager = mockk<ObservabilityManager>(relaxed = true)

    private lateinit var apiProvider: ApiProvider
    private lateinit var featureFlagContextProvider: TestFeatureFlagContextProvider
    private lateinit var local: FeatureFlagLocalDataSource
    private lateinit var remote: FeatureFlagRemoteDataSource
    private lateinit var repository: FeatureFlagRepository

    @Before
    fun setUp() {
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, coroutinesRule.dispatchers)
        featureFlagContextProvider = TestFeatureFlagContextProvider()
        local = spyk(FeatureFlagLocalDataSourceImpl(database))
        remote = spyk(FeatureFlagRemoteDataSourceImpl(apiProvider, workManager, Optional.of(featureFlagContextProvider)))
        repository = FeatureFlagRepositoryImpl(
            localDataSource = local,
            remoteDataSource = remote,
            workerManager = workerManager,
            observabilityManager = observabilityManager,
            scopeProvider = TestCoroutineScopeProvider(coroutinesRule.dispatchers)
        )

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { database.inTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
    }

    @After
    fun clean() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
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
                enabledFeatureEntity.copy(
                    userId = nullUserId.orGlobal(),
                    scope = Scope.Global,
                    value = true
                )
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
                enabledFeatureEntity.copy(
                    userId = nullUserId.orGlobal(),
                    scope = Scope.Global,
                    value = true
                )
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
        coEvery {
            featureFlagDao.observe(
                userId.withGlobal(),
                listOf(featureId.id)
            )
        } returns mutableDbFlow

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
    fun featureFlagValueIsFetchedFromApiAndObservedInDbWhenNotAlreadyAvailableLocally() =
        coroutinesTest {
            // Given
            val mutableDbFlow = MutableStateFlow<List<FeatureFlagEntity>>(emptyList())
            coEvery {
                featureFlagDao.observe(
                    userId.withGlobal(),
                    listOf(featureId.id)
                )
            } returns mutableDbFlow

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
        coEvery { featureFlagDao.observe(userId.withGlobal(), any<List<String>>()) } returns mutableDbFlow
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
        coEvery { featureFlagDao.observe(userId.withGlobal(), any<List<String>>()) } returns mutableDbFlow

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
    fun prefetchFeatureFlagsCallsRemoteDataSource() = coroutinesTest {
        val featureIds = setOf(featureId, featureId1)
        repository.prefetch(userId, featureIds)

        coVerify { remote.prefetch(userId, featureIds) }
    }

    @Test
    fun updateUpsertTheGivenFeatureFlagLocallyAndEnqueuesRemoteCall() = coroutinesTest {
        // Given
        val featureFlag = FeatureFlagTestData.disabledFeature

        // When
        repository.update(featureFlag)

        // Then
        coVerify { local.upsert(listOf(featureFlag)) }
        coVerify { remote.update(featureFlag) }
    }

    @Test
    fun getValueReturnTrue() = coroutinesTest {
        // Given
        val list = listOf(
            FeatureFlag(
                userId = userId,
                featureId = featureId,
                scope = Scope.Unleash,
                defaultValue = false,
                value = true
            )
        )
        coEvery { remote.getAll(any()) } returns list
        coEvery { local.getAll(Scope.Unleash) } returns list

        // When
        repository.getAll(userId)

        // Then
        coVerify { remote.getAll(userId) }
        coVerify { local.replaceAll(userId, Scope.Unleash, list) }
        coVerify { local.getAll(Scope.Unleash) }

        val result = repository.getValue(userId, featureId)
        assertNotNull(result)
        assertTrue(result)
    }

    @Test
    fun getValueReturnFalse() = coroutinesTest {
        // Given
        val list = listOf(
            FeatureFlag(
                userId = userId,
                featureId = featureId,
                scope = Scope.Unleash,
                defaultValue = false,
                value = false
            )
        )
        coEvery { remote.getAll(any()) } returns list
        coEvery { local.getAll(Scope.Unleash) } returns list

        // When
        repository.getAll(userId)

        // Then
        coVerify { remote.getAll(userId) }
        coVerify { local.replaceAll(userId, Scope.Unleash, list) }
        coVerify { local.getAll(Scope.Unleash) }

        val result = repository.getValue(userId, featureId)
        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun getValueReturnNull() = coroutinesTest {
        // Given
        val list = emptyList<FeatureFlag>()
        coEvery { remote.getAll(any()) } returns list
        coEvery { local.getAll(Scope.Unleash) } returns list

        // When
        repository.getAll(userId)

        // Then
        coVerify { remote.getAll(userId) }
        coVerify { local.replaceAll(userId, Scope.Unleash, list) }
        coVerify { local.getAll(Scope.Unleash) }

        val result = repository.getValue(userId, featureId)
        assertNull(result)
    }

    @Test
    fun getValueReturnTrueForUser1FalseForUser2() = coroutinesTest {
        // Given
        val userId1 = UserId("1")
        val userId2 = UserId("2")
        val featureId = FeatureId("Test")
        val list1 = listOf(
            FeatureFlag(
                userId = userId1,
                featureId = featureId,
                scope = Scope.Unleash,
                defaultValue = false,
                value = true
            )
        )
        val list2 = listOf(
            FeatureFlag(
                userId = userId2,
                featureId = featureId,
                scope = Scope.Unleash,
                defaultValue = false,
                value = false
            )
        )
        coEvery { remote.getAll(userId1) } returns list1
        coEvery { remote.getAll(userId2) } returns list2
        coEvery { local.getAll(Scope.Unleash) } returns list1 + list2

        // When
        repository.getAll(userId1)
        repository.getAll(userId2)

        // Then
        val result1 = repository.getValue(userId1, featureId)
        assertNotNull(result1)
        assertTrue(result1)

        val result2 = repository.getValue(userId2, featureId)
        assertNotNull(result2)
        assertFalse(result2)
    }

    @Test
    fun getValueReturnTrueThenNull() = coroutinesTest {
        // Given
        val featureId = FeatureId("Test")
        val list = listOf(
            FeatureFlag(
                userId = userId,
                featureId = featureId,
                scope = Scope.Unleash,
                defaultValue = false,
                value = true
            )
        )
        coEvery { remote.getAll(userId) } returns list
        coEvery { local.getAll(Scope.Unleash) } returns list

        // When
        repository.getAll(userId)

        // Then
        val result1 = repository.getValue(userId, featureId)
        assertNotNull(result1)
        assertTrue(result1)

        // Given
        coEvery { remote.getAll(userId) } returns emptyList()
        coEvery { local.getAll(Scope.Unleash) } returns emptyList()

        // When
        repository.getAll(userId)

        // Then
        val result2 = repository.getValue(userId, featureId)
        assertNull(result2)
    }

    @Test
    fun refreshAllOneTimeFeatureFlagsEnqueueWorker() = coroutinesTest {
        // When
        repository.refreshAllOneTime(userId)

        // Then
        coVerify { workerManager.enqueueOneTime(userId) }
    }

    @Test
    fun refreshAllPeriodicFeatureFlagsEnqueueWorker() = coroutinesTest {
        // When
        repository.refreshAllPeriodic(userId, true)

        // Then
        coVerify { workerManager.enqueuePeriodic(userId, true) }
    }

    @Test
    fun awaitNotEmptyScopeSuccess() = coroutinesTest {
        // Given
        val list = listOf(FeatureFlagEntity(userId, "Feature", Scope.Unleash, defaultValue = false, value = true))
        coEvery { featureFlagDao.observe(userId.withGlobal(), Scope.Unleash) } returns flowOf(list)

        // When
        repository.awaitNotEmptyScope(userId = userId, Scope.Unleash)

        // Then
        coVerify { observabilityManager.enqueue(FeatureFlagAwaitTotal.Success, any()) }
    }

    @Test
    fun awaitNotEmptyScopeFailure() = coroutinesTest {
        // Given
        coEvery { featureFlagDao.observe(userId.withGlobal(), Scope.Unleash) } throws CancellationException()

        // When
        assertFailsWith<CancellationException> {
            repository.awaitNotEmptyScope(userId = userId, Scope.Unleash)
        }
        // Then
        coVerify { observabilityManager.enqueue(FeatureFlagAwaitTotal.Failure, any()) }
    }

    @Test
    fun contextPropertiesIncludedInApiRequest() = coroutinesTest {
        // Given
        val properties = mapOf("customProperty" to "propertyValue")
        featureFlagContextProvider.properties = properties
        val emptyUnleashResponse = GetUnleashTogglesResponse(ResponseCodes.OK, emptyList())
        coEvery { featuresApi.getUnleashToggles(any()) } returns emptyUnleashResponse

        // When
        repository.getAll()

        // Then
        coVerify { featuresApi.getUnleashToggles(properties) }
    }
}

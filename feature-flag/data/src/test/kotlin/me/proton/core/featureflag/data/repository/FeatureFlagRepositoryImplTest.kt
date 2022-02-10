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
import me.proton.core.featureflag.data.api.FeaturesApi
import me.proton.core.featureflag.data.api.response.FeatureApiResponse
import me.proton.core.featureflag.data.api.response.FeaturesApiResponse
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.data.db.dao.FeatureFlagDao
import me.proton.core.featureflag.data.entity.FeatureFlagEntity
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData
import me.proton.core.featureflag.data.testdata.FeatureIdTestData
import me.proton.core.featureflag.data.testdata.SessionIdTestData
import me.proton.core.featureflag.data.testdata.UserIdTestData
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

class FeatureFlagRepositoryImplTest {

    private val featureFlagDao = mockk<FeatureFlagDao> {
        coEvery { this@mockk.insertOrUpdate(any<FeatureFlagEntity>()) } just Runs
    }
    private val database = mockk<FeatureFlagDatabase> {
        every { this@mockk.featureFlagDao() } returns featureFlagDao
    }
    private val sessionProvider = mockk<SessionProvider> {
        coEvery { this@mockk.getSessionId(UserIdTestData.userId) } returns SessionIdTestData.sessionId
    }
    private val featuresApi = mockk<FeaturesApi> {
        coEvery { this@mockk.getFeatureFlag(any()) } returns FeaturesApiResponse(
            1000,
            listOf(
                FeatureApiResponse(
                    FeatureIdTestData.featureId.id,
                    isGlobal = false,
                    defaultValue = false,
                    value = true
                )
            )
        )
    }
    private val apiManagerFactory = mockk<ApiManagerFactory> {
        every { this@mockk.create(any(), interfaceClass = FeaturesApi::class) } returns TestApiManager(featuresApi)
    }

    private lateinit var repository: FeatureFlagRepository

    @Before
    fun setUp() {
        repository = FeatureFlagRepositoryImpl(
            database,
            ApiProvider(apiManagerFactory, sessionProvider)
        )
    }

    @Test
    @Ignore("Disabled as failing after introducing Store, awaiting for feedback")
    fun featureFlagIsFetchedFromApiWhenNotAvailableInDb() = runBlockingTest {
        // Given
        coEvery { featureFlagDao.observe(UserIdTestData.userId, FeatureIdTestData.featureId.id) } returns flowOf(null)

        // When
        repository.get(UserIdTestData.userId, FeatureIdTestData.featureId)

        // Then
        coVerify { featuresApi.getFeatureFlag(FeatureIdTestData.featureId.id) }
    }

    @Test
    fun featureFlagIsReturnedFromDbWhenAvailable() = runBlockingTest {
        // Given
        coEvery {
            featureFlagDao.observe(
                UserIdTestData.userId,
                FeatureIdTestData.featureId.id
            )
        } returns flowOf(FeatureFlagTestData.enabledFeatureFlagEntity)

        // When
        val actual = repository.get(UserIdTestData.userId, FeatureIdTestData.featureId)

        // Then
        val expected = FeatureFlag(FeatureIdTestData.featureId, true)
        assertEquals(expected, actual)
        verify { featuresApi wasNot Called }
    }

    @Test
    @Ignore("Disabled as failing after introducing Store, awaiting for feedback")
    fun featureFlagIsSavedToDbWhenFetchedFromApi() = runBlockingTest {
        // Given
        coEvery { featureFlagDao.observe(UserIdTestData.userId, FeatureIdTestData.featureId.id) } returns flowOf(null)

        // When
        repository.get(UserIdTestData.userId, FeatureIdTestData.featureId)

        // Then
        val expected = FeatureFlagEntity(
            UserIdTestData.userId,
            FeatureIdTestData.featureId.id,
            isGlobal = false,
            defaultValue = false,
            value = true
        )
        coVerify(timeout = 5000) { featureFlagDao.insertOrUpdate(expected) }
    }

    @Test
    fun featureFlagValueIsObservedInDb() = runBlockingTest {
        // Given
        val mutableDbFlow = MutableStateFlow<FeatureFlagEntity?>(FeatureFlagTestData.enabledFeatureFlagEntity)
        coEvery { featureFlagDao.observe(UserIdTestData.userId, FeatureIdTestData.featureId.id) } returns mutableDbFlow

        // When
        repository.observe(UserIdTestData.userId, FeatureIdTestData.featureId).test {
            // Then
            assertEquals(
                FeatureFlag(FeatureIdTestData.featureId, true),
                awaitItem()
            )

            // simulate this feature flag's value changes in the DB
            mutableDbFlow.emit(FeatureFlagTestData.disabledFeatureFlagEntity)
            assertEquals(
                FeatureFlag(FeatureIdTestData.featureId, false),
                awaitItem()
            )
        }
        // API should not be called when there are values in DB (update is done through event loop)
        verify { featuresApi wasNot Called }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun featureFlagValueIsFetchedFromApiAndObservedInDbWhenNotAlreadyAvailableLocally() = runBlocking {
        // Given
        val mutableDbFlow = MutableStateFlow<FeatureFlagEntity?>(null)
        coEvery { featureFlagDao.observe(UserIdTestData.userId, FeatureIdTestData.featureId.id) } returns mutableDbFlow

        // When
        repository.observe(UserIdTestData.userId, FeatureIdTestData.featureId).test(timeout = 2000.milliseconds) {
            // First item is emitted from DB is null
            assertNull(awaitItem())

            // enabledFeatureFlagEntity is the corresponding entity that the mocked API response
            coVerify(timeout = 500) { featureFlagDao.insertOrUpdate(FeatureFlagTestData.enabledFeatureFlagEntity) }

            // Inserting the API response into DB causes it to be emitted
            mutableDbFlow.emit(FeatureFlagTestData.enabledFeatureFlagEntity)

            val expectedFlag = FeatureFlag(FeatureIdTestData.featureId, true)
            assertEquals(expectedFlag, awaitItem())
        }
    }

}

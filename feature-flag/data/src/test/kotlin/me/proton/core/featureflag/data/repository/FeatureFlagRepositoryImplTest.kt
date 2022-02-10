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
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.featureflag.data.api.fake.MockFeaturesApiProvider
import me.proton.core.featureflag.data.api.response.FeatureApiResponse
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.data.db.dao.FeatureFlagDao
import me.proton.core.featureflag.data.entity.FeatureFlagEntity
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData
import me.proton.core.featureflag.data.testdata.FeatureIdTestData
import me.proton.core.featureflag.data.testdata.UserIdTestData
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FeatureFlagRepositoryImplTest {

    private val featureFlagDao = mockk<FeatureFlagDao> {
        coEvery { this@mockk.insertOrUpdate(any<FeatureFlagEntity>()) } just Runs
    }
    private val database = mockk<FeatureFlagDatabase> {
        every { this@mockk.featureFlagDao() } returns featureFlagDao
    }

    private val featureApiResponse = FeatureApiResponse(
        FeatureIdTestData.featureId.id,
        isGlobal = false,
        defaultValue = false,
        value = true
    )
    private val fakeApiProvider = MockFeaturesApiProvider(featureApiResponse)

    private lateinit var repository: FeatureFlagRepository

    @Before
    fun setUp() {
        repository = FeatureFlagRepositoryImpl(
            database,
            fakeApiProvider.mockedApiProvider()
        )
    }

    @Test
    fun featureFlagIsFetchedFromApiWhenNotAvailableInDb() = runBlockingTest {
        // Given
        coEvery { featureFlagDao.get(UserIdTestData.userId, FeatureIdTestData.featureId.id) } returns null

        // When
        val actual = repository.get(UserIdTestData.userId, FeatureIdTestData.featureId)

        // Then
        val expected = DataResult.Success(ResponseSource.Remote, FeatureFlag(FeatureIdTestData.featureId, true))
        assertEquals(expected, actual)
    }

    @Test
    fun featureFlagIsReturnedFromDbWhenAvailable() = runBlockingTest {
        // Given
        coEvery {
            featureFlagDao.get(
                UserIdTestData.userId,
                FeatureIdTestData.featureId.id
            )
        } returns FeatureFlagTestData.enabledFeatureFlagEntity

        // When
        val actual = repository.get(UserIdTestData.userId, FeatureIdTestData.featureId)

        // Then
        val expected = DataResult.Success(ResponseSource.Local, FeatureFlag(FeatureIdTestData.featureId, true))
        assertEquals(expected, actual)
        // API should not be called when there are values in DB (update is done through event loop)
        fakeApiProvider.verifyApiProviderNotCalled()
    }

    @Test
    fun featureFlagIsSavedToDbWhenFetchedFromApi() = runBlockingTest {
        // Given
        coEvery { featureFlagDao.get(UserIdTestData.userId, FeatureIdTestData.featureId.id) } returns null

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
        coVerify { featureFlagDao.insertOrUpdate(expected) }
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
                DataResult.Success(ResponseSource.Local, FeatureFlag(FeatureIdTestData.featureId, true)),
                awaitItem()
            )

            // simulate this feature flag's value changes in the DB
            mutableDbFlow.emit(FeatureFlagTestData.disabledFeatureFlagEntity)
            assertEquals(
                DataResult.Success(ResponseSource.Local, FeatureFlag(FeatureIdTestData.featureId, false)),
                awaitItem()
            )
        }
        // API should not be called when there are values in DB (update is done through event loop)
        fakeApiProvider.verifyApiProviderNotCalled()
    }

    @Test
    fun featureFlagValueIsFetchedFromApiAndObservedInDbWhenNotAlreadyAvailableLocally() = runBlockingTest {
        // Given
        val mutableDbFlow = MutableStateFlow<FeatureFlagEntity?>(null)
        coEvery { featureFlagDao.observe(UserIdTestData.userId, FeatureIdTestData.featureId.id) } returns mutableDbFlow

        // When
        repository.observe(UserIdTestData.userId, FeatureIdTestData.featureId).test {
            // Then
            val expectedFlag = FeatureFlag(FeatureIdTestData.featureId, true)
            assertEquals(DataResult.Success(ResponseSource.Remote, expectedFlag), awaitItem())

            // enabledFeatureFlagEntity is the corresponding entity that the mocked API response
            coVerify { featureFlagDao.insertOrUpdate(FeatureFlagTestData.enabledFeatureFlagEntity) }
            // Inserting the API response into DB causes it to be emitted
            mutableDbFlow.emit(FeatureFlagTestData.enabledFeatureFlagEntity)

            val expected = DataResult.Success(ResponseSource.Local, expectedFlag)
            assertEquals(expected, awaitItem())
        }
    }

}

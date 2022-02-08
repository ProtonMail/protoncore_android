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

package me.proton.core.featureflags.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.featureflags.data.api.fake.MockFeaturesApiProvider
import me.proton.core.featureflags.data.api.response.FeatureApiResponse
import me.proton.core.featureflags.data.db.FeatureFlagDatabase
import me.proton.core.featureflags.data.db.dao.FeatureFlagDao
import me.proton.core.featureflags.data.entity.FeatureFlagEntity
import me.proton.core.featureflags.data.testdata.FeatureIdTestData
import me.proton.core.featureflags.data.testdata.UserIdTestData
import me.proton.core.featureflags.domain.entity.FeatureFlag
import me.proton.core.featureflags.domain.repository.FeatureFlagsRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FeatureFlagsRepositoryImplTest {

    private val featureFlagDao = mockk<FeatureFlagDao>()
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

    private lateinit var repository: FeatureFlagsRepository

    @Before
    fun setUp() {
        repository = FeatureFlagsRepositoryImpl(
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
        coEvery { featureFlagDao.get(UserIdTestData.userId, FeatureIdTestData.featureId.id) } returns FeatureFlagEntity(
            UserIdTestData.userId,
            FeatureIdTestData.featureId.id,
            isGlobal = false,
            defaultValue = false,
            value = true
        )

        // When
        val actual = repository.get(UserIdTestData.userId, FeatureIdTestData.featureId)

        // Then
        val expected = DataResult.Success(ResponseSource.Local, FeatureFlag(FeatureIdTestData.featureId, true))
        assertEquals(expected, actual)
        fakeApiProvider.verifyApiProviderNotCalled()
    }
}

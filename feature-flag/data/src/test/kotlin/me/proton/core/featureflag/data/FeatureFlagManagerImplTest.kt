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

package me.proton.core.featureflag.data

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData.featureId
import me.proton.core.featureflag.data.testdata.UserIdTestData
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FeatureFlagManagerImplTest {

    private val repository = mockk<FeatureFlagRepository>()

    private lateinit var manager: FeatureFlagManager

    private fun newFlag(featureId: FeatureId, value: Boolean) = FeatureFlag(
        userId = null,
        featureId = featureId,
        scope = Scope.Global,
        defaultValue = value,
        value = value,
    )

    @Before
    fun setUp() {
        manager = FeatureFlagManagerImpl(repository)
    }

    @Test
    fun observeReturnsValueFromRepository() = runTest {
        // Given
        val repositoryFeatureFlag = newFlag(featureId, true)
        every { repository.observe(any(), any<FeatureId>(), false) } returns flowOf(repositoryFeatureFlag)

        // When
        val actual = manager.observe(UserIdTestData.userId, featureId).first()

        // Then
        verify { repository.observe(UserIdTestData.userId, featureId, false) }
        assertEquals(repositoryFeatureFlag, actual)
    }

    @Test
    fun getReturnsValueFromRepository() = runTest {
        // Given
        val repositoryFeatureFlag = newFlag(featureId, true)
        coEvery { repository.get(any(), any<FeatureId>()) } returns repositoryFeatureFlag

        // When
        val actual = manager.get(UserIdTestData.userId, featureId)

        // Then
        coVerify { repository.get(UserIdTestData.userId, featureId) }
        assertEquals(repositoryFeatureFlag, actual)
    }

    @Test
    fun prefetchCallsRepository() = runTest {
        // Given
        coEvery { repository.prefetch(any(), any()) } just Runs

        // When
        val featureIds = setOf(featureId, FeatureFlagTestData.featureId1)
        manager.prefetch(UserIdTestData.userId, featureIds)

        // Then
        coVerify { repository.prefetch(UserIdTestData.userId, featureIds) }
    }

    @Test
    fun updateFeatureCallsRepository() = runTest {
        // Given
        val featureFlag = FeatureFlagTestData.enabledFeature
        coEvery { repository.update(any()) } just Runs

        // When
        manager.update(featureFlag)

        // Then
        coVerify { repository.update(featureFlag) }
    }
}

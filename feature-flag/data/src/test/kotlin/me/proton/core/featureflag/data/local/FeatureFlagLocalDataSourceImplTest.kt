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

package me.proton.core.featureflag.data.local

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.db.FeatureFlagDao
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.domain.entity.FeatureId
import org.junit.Test

class FeatureFlagLocalDataSourceImplTest {

    private val dao = mockk<FeatureFlagDao>(relaxUnitFun = true)
    private val database = mockk<FeatureFlagDatabase> {
        every { featureFlagDao() } returns dao
    }

    private val localDataSource = FeatureFlagLocalDataSourceImpl(database)

    @Test
    fun `update value calls dao with user id raw feature id and value`() = runTest {
        val userId = UserId("test-user-id")
        val rawFeatureId = "raw-feature-id"
        val featureId = FeatureId(rawFeatureId)
        val value = true

        localDataSource.updateValue(userId, featureId, value)

        verify { dao.updateValue(userId, rawFeatureId, value) }
    }

    @Test
    fun `update value calls dao with global user id when given user id is null`() = runTest {
        val globalUserId = UserId("global")
        val rawFeatureId = "raw-feature-id"
        val value = false

        localDataSource.updateValue(null, FeatureId(rawFeatureId), value)

        verify { dao.updateValue(globalUserId, rawFeatureId, value) }
    }
}
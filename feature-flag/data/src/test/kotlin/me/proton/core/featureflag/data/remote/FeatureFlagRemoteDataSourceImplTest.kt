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

package me.proton.core.featureflag.data.remote

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.featureflag.data.remote.worker.UpdateFeatureFlagWorker
import me.proton.core.featureflag.data.testdata.FeatureFlagTestData
import me.proton.core.network.data.ApiProvider
import org.junit.Test
import kotlin.test.assertEquals

class FeatureFlagRemoteDataSourceImplTest {

    private val workManager: WorkManager = mockk {
        coEvery { enqueue(any<WorkRequest>()) } returns mockk()
    }
    private val apiProvider: ApiProvider = mockk()

    private val remoteDataSource = FeatureFlagRemoteDataSourceImpl(apiProvider, workManager)

    @Test
    fun `update enqueues worker to update on remote`() = runTest {
        // given
        val featureFlag = FeatureFlagTestData.disabledFeature

        // when
        remoteDataSource.update(featureFlag)

        // then
        val requestSlot = slot<OneTimeWorkRequest>()
        verify { workManager.enqueue(capture(requestSlot)) }
        val workSpec = requestSlot.captured.workSpec
        assertEquals(UpdateFeatureFlagWorker::class.qualifiedName, workSpec.workerClassName)
    }
}
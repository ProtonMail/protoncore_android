/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.featureflag.data.remote.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.featureflag.data.testdata.UserIdTestData
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.FeatureFlagRefreshRequestTotal
import org.junit.Test

class FeatureFlagWorkerManagerTest {

    private val userId = UserIdTestData.userId

    private val context = mockk<Context>(relaxed = true) {
        every { resources } returns mockk {
            every { getInteger(any()) } returns 900 // minimum interval
        }
    }
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val observabilityManager = mockk<ObservabilityManager>(relaxed = true)

    private fun mockManager() = FeatureFlagWorkerManager(context, workManager, observabilityManager)

    @Test
    fun enqueueOneTime() = runTest {
        // WHEN
        mockManager().enqueueOneTime(userId)

        // THEN
        verify {
            workManager.enqueueUniqueWork(
                FetchUnleashTogglesWorker.getOneTimeUniqueWorkName(userId),
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }

        verify { observabilityManager.enqueue(FeatureFlagRefreshRequestTotal.Onetime, any()) }
    }

    @Test
    fun enqueuePeriodicNotImmediately() = runTest {
        // WHEN
        mockManager().enqueuePeriodic(userId, false)

        // THEN
        verify {
            workManager.enqueueUniquePeriodicWork(
                FetchUnleashTogglesWorker.getPeriodicUniqueWorkName(userId),
                ExistingPeriodicWorkPolicy.KEEP,
                any<PeriodicWorkRequest>()
            )
        }

        verify { observabilityManager.enqueue(FeatureFlagRefreshRequestTotal.Periodic, any()) }
    }

    @Test
    fun enqueuePeriodicImmediately() = runTest {
        // WHEN
        mockManager().enqueuePeriodic(userId, true)

        // THEN
        verify {
            workManager.enqueueUniquePeriodicWork(
                FetchUnleashTogglesWorker.getPeriodicUniqueWorkName(userId),
                ExistingPeriodicWorkPolicy.REPLACE,
                any<PeriodicWorkRequest>()
            )
        }

        verify { observabilityManager.enqueue(FeatureFlagRefreshRequestTotal.Periodic, any()) }
    }
}

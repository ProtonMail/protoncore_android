/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.userrecovery.data.worker

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test

class UserRecoveryWorkerManagerImplTest {

    private val testUserId = "test-user-id"

    private val workManager = mockk<WorkManager>(relaxed = true)

    private lateinit var tested: UserRecoveryWorkerManagerImpl

    @Before
    fun beforeEveryTest() {
        tested = UserRecoveryWorkerManagerImpl(workManager)
    }

    @Test
    fun setRecoverySecret() = runTest {
        // WHEN
        tested.enqueueSetRecoverySecret(UserId(testUserId))
        // THEN
        verify {
            workManager.enqueueUniqueWork(
                "setRecoverySecretWork-test-user-id",
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }
}

/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.mailsettings.data.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.data.worker.SettingsProperty.DisplayName
import me.proton.core.util.kotlin.deserialize
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateSettingsWorkerTest {

    private val context = mockk<Context>()

    private val parameters = mockk<WorkerParameters> {
        every { this@mockk.taskExecutor } returns mockk(relaxed = true)
    }

    private val workManager = mockk<WorkManager> {
        coEvery {
            this@mockk.enqueueUniqueWork(
                any<String>(),
                any<ExistingWorkPolicy>(),
                any<OneTimeWorkRequest>()
            )
        } returns mockk()
        every { this@mockk.getWorkInfoById(any()) } returns mockk()
    }

    private val worker = UpdateSettingsWorker(
        context,
        parameters
    )

    @Test
    fun workerEnqueuerCreatesOneTimeRequestWorkerWhichIsUniqueByUserAndSettingProperty() {
        // WHEN
        val userId = UserId("userId")
        UpdateSettingsWorker.Enqueuer(workManager).enqueue(userId, DisplayName("Updated name"))

        // THEN
        val requestSlot = slot<OneTimeWorkRequest>()
        verify {
            workManager.enqueueUniqueWork(
                "updateSettingsWork-userId-DisplayName",
                ExistingWorkPolicy.REPLACE,
                capture(requestSlot)
            )
        }
        val workSpec = requestSlot.captured.workSpec
        val inputData = workSpec.input
        val actualUserId = inputData.getString("keyUserId")
        val settingsPropertySerialized = inputData.getString("keySettingsPropertySerialized")
        val actualSettingsProperty = settingsPropertySerialized?.deserialize<SettingsProperty>()
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
        assertEquals("userId", actualUserId)
        assertEquals(DisplayName("Updated name"), actualSettingsProperty)
    }
}

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

package me.proton.core.eventmanager.data.work

import android.content.Context
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.utils.futures.SettableFuture
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.presentation.app.AppLifecycleProvider
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EventWorkerManagerImplTest {

    @get:Rule
    val mockKRule = MockKRule(this)

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var workManager: WorkManager

    @MockK
    lateinit var appLifecycleProvider: AppLifecycleProvider

    @InjectMockKs
    lateinit var manager: EventWorkerManagerImpl

    @Test
    fun `given empty work infos when isRunning then returns false`() = runTest {
        val config = EventManagerConfig.Core(UserId("user-id"))
        val future = SettableFuture.create<MutableList<WorkInfo>>()
        every { workManager.getWorkInfosForUniqueWork(config.toString()) } returns future

        future.set(mutableListOf())

        assertFalse(manager.isRunning(config))
    }

    @Test
    fun `given work infos with first running when isRunning then returns true`() = runTest {
        val config = EventManagerConfig.Core(UserId("user-id"))
        val future = SettableFuture.create<MutableList<WorkInfo>>()
        every { workManager.getWorkInfosForUniqueWork(config.toString()) } returns future

        future.set(
            mutableListOf(
                createWorkInfo(WorkInfo.State.RUNNING),
                createWorkInfo(WorkInfo.State.CANCELLED),
            )
        )

        assertTrue(manager.isRunning(config))
    }

    @Test
    fun `given work infos with first not running when isRunning then returns false`() = runTest {
        val config = EventManagerConfig.Core(UserId("user-id"))
        val future = SettableFuture.create<MutableList<WorkInfo>>()
        every { workManager.getWorkInfosForUniqueWork(config.toString()) } returns future

        future.set(
            mutableListOf(
                createWorkInfo(WorkInfo.State.CANCELLED),
                createWorkInfo(WorkInfo.State.RUNNING),
            )
        )

        assertFalse(manager.isRunning(config))
    }

    private fun createWorkInfo(state: WorkInfo.State) = WorkInfo(
        UUID.randomUUID(),
        state,
        Data.EMPTY,
        emptyList(),
        Data.EMPTY,
        0
    )
}
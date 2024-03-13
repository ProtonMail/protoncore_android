/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.telemetry.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.repository.TelemetryRepository
import me.proton.core.telemetry.domain.usecase.IsTelemetryEnabled
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import kotlin.test.BeforeTest
import kotlin.test.Test

class TelemetryManagerTest {
    private lateinit var isTelemetryEnabled: IsTelemetryEnabled
    private lateinit var telemetryRepository: TelemetryRepository
    private lateinit var scopeProvider: CoroutineScopeProvider
    private lateinit var workerManager: TelemetryWorkerManager
    private lateinit var tested: TelemetryManager

    private val userId = UserId("user-id")

    @BeforeTest
    fun setUp() {
        isTelemetryEnabled = mockk()
        telemetryRepository = mockk(relaxUnitFun = true)
        scopeProvider =
            TestCoroutineScopeProvider(TestDispatcherProvider(UnconfinedTestDispatcher()))
        workerManager = mockk(relaxed = true)

        tested = TelemetryManager(
            isTelemetryEnabled,
            telemetryRepository,
            scopeProvider,
            workerManager
        )
    }

    @Test
    fun telemetryIsDisabled() = runTest {
        coEvery { isTelemetryEnabled(userId) } returns false

        // WHEN
        tested.enqueue(userId, TelemetryEvent(group = "group", name = "first"))


        // THEN
        coVerify(exactly = 0) { telemetryRepository.addEvent(userId, any()) }

        coVerify(exactly = 1) { telemetryRepository.deleteAllEvents(userId) }
        verify(exactly = 0) { workerManager.enqueueOrKeep(userId, any()) }
    }

    @Test
    fun sendWithDelay() = runTest {
        coEvery { isTelemetryEnabled(userId) } returns true
        val event = TelemetryEvent(group = "group", name = "first")

        // WHEN
        tested.enqueue(userId, event)

        // THEN
        coVerify(exactly = 0) { telemetryRepository.deleteAllEvents(userId) }
        verify(exactly = 0) { workerManager.cancel(userId) }

        coVerify(exactly = 1) { telemetryRepository.addEvent(userId, event) }
        verify(exactly = 1) { workerManager.enqueueOrKeep(userId, TelemetryManager.MAX_DELAY) }
    }

    @Test
    fun sendExceptionHappened() = runTest {
        coEvery { isTelemetryEnabled(userId) } returns true
        val event = TelemetryEvent(group = "group", name = "first")
        coEvery { telemetryRepository.addEvent(userId, event) } throws Exception("Test")

        // WHEN
        tested.enqueueEvent(userId, event)

        // THEN
        coVerify(exactly = 0) { telemetryRepository.deleteAllEvents(userId) }
        verify(exactly = 0) { workerManager.cancel(userId) }

        coVerify(exactly = 1) { telemetryRepository.addEvent(userId, event) }
        verify(exactly = 0) { workerManager.enqueueOrKeep(userId, TelemetryManager.MAX_DELAY) }
    }
}

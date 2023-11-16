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

package me.proton.core.eventmanager.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.extension.suspend
import kotlin.test.Test
import kotlin.test.assertTrue

class EventManagerKtTest {
    private val testUserId = UserId("user_id")
    private val testConfig = EventManagerConfig.Core(testUserId)

    private val manager = mockk<EventManager> {
        coEvery { this@mockk.suspend<Unit>(captureLambda()) } coAnswers {
            lambda<(suspend () -> Unit)>().captured()
        }
    }
    private val provider = mockk<EventManagerProvider> {
        coEvery { this@mockk.get(testConfig) } returns manager
    }

    @Test
    fun suspendIsCalled() = runTest {
        mockkStatic("me.proton.core.eventmanager.domain.extension.EventManagerKt")
        // GIVEN
        var called = false
        val block: suspend () -> Unit = { called = true }
        // WHEN
        provider.suspend(testConfig, block)
        // THEN
        coVerify { provider.get(testConfig) }
        coVerify { manager.suspend(block) }
        assertTrue(called)
    }
}

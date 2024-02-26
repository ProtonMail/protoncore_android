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

package me.proton.core.telemetry.presentation

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import io.mockk.declaringKotlinFile
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.utils.launchOnBackPressed
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ScreenMeasurementsKtTest {
    @BeforeTest
    fun setUp() {
        mockkStatic(LifecycleOwner::launchOnBackPressed.declaringKotlinFile.qualifiedName!!)
        mockkStatic(LifecycleOwner::launchOnScreenView.declaringKotlinFile.qualifiedName!!)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `measure screen displayed`() = runTest {
        // GIVEN
        val telemetryManager = mockk<TelemetryManager> {
            justRun { enqueue(any(), any()) }
        }
        val userId = UserId("user_id")
        val delegate = TestProductMetricsDelegate(
            productGroup = "group",
            productFlow = "flow",
            productDimensions = mapOf("delegate_extra" to "delegate_value"),
            telemetryManager = telemetryManager,
            userId = userId
        )
        val blockSlot = slot<suspend () -> Unit>()
        val lifecycleOwner = mockk<LifecycleOwner> {
            every {
                launchOnScreenView(any(), capture(blockSlot))
            } returns mockk()
        }
        val savedStateRegistryOwner = mockk<SavedStateRegistryOwner> {
            every { savedStateRegistry } returns mockk()
        }

        // WHEN
        measureOnScreenDisplayed(
            event = "event",
            dimensions = mapOf("extra" to "value"),
            delegate = delegate,
            lifecycleOwner = lifecycleOwner,
            savedStateRegistryOwner = savedStateRegistryOwner
        )
        blockSlot.captured.invoke()

        // THEN
        val expectedEvent = TelemetryEvent(
            group = "group",
            name = "event",
            dimensions = mapOf(
                "flow" to "flow",
                "extra" to "value",
                "delegate_extra" to "delegate_value"
            ),
        )
        verify { telemetryManager.enqueue(userId, expectedEvent) }
    }

    @Test
    fun `measure screen closed`() = runTest {
        // GIVEN
        val telemetryManager = mockk<TelemetryManager> {
            justRun { enqueue(any(), any()) }
        }
        val userId = UserId("user_id")
        val delegate = TestProductMetricsDelegate(
            productGroup = "group",
            productFlow = "flow",
            productDimensions = mapOf("delegate_extra" to "delegate_value"),
            telemetryManager = telemetryManager,
            userId = userId
        )
        val blockSlot = slot<() -> Unit>()
        val lifecycleOwner = mockk<LifecycleOwner> {
            every {
                launchOnBackPressed(any(), capture(blockSlot))
            } returns mockk()
        }
        val onBackPressedDispatcherOwner = mockk<OnBackPressedDispatcherOwner> {
            every { onBackPressedDispatcher } returns mockk()
        }

        // WHEN
        measureOnScreenClosed(
            event = "event",
            dimensions = mapOf("extra" to "value"),
            delegate = delegate,
            lifecycleOwner = lifecycleOwner,
            onBackPressedDispatcherOwner = onBackPressedDispatcherOwner
        )
        blockSlot.captured.invoke()

        // THEN
        val expectedEvent = TelemetryEvent(
            group = "group",
            name = "event",
            dimensions = mapOf(
                "flow" to "flow",
                "extra" to "value",
                "delegate_extra" to "delegate_value"
            ),
        )
        verify { telemetryManager.enqueue(userId, expectedEvent) }
    }
}

private class TestProductMetricsDelegate(
    override val productGroup: String,
    override val productFlow: String,
    override val productDimensions: Map<String, String> = emptyMap(),
    override val telemetryManager: TelemetryManager,
    override val userId: UserId? = null,
) : ProductMetricsDelegate

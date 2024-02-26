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

package me.proton.core.telemetry.presentation

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ViewMeasurementsKtTest {

    private val telemetryManager = mockk<TelemetryManager>(relaxed = true)
    private val productMetricsDelegate = mockk<ProductMetricsDelegate>(relaxed = true)
    private val testUserId = UserId("test-user-id")

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.telemetry.presentation.ViewMeasurementsKt")
        every { productMetricsDelegate.telemetryManager } returns telemetryManager
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.telemetry.presentation.ViewMeasurementsKt")
    }

    @Test
    fun `measureViewClickInteraction enqueues`() {
        val testEvent = "test.event"
        val testProductDimensions = mapOf(Pair("dimension1", "dim1val1"))
        every { productMetricsDelegate.userId } returns testUserId
        every { productMetricsDelegate.productFlow } returns "test-product-flow"

        measureOnViewClicked(testEvent, productMetricsDelegate, testProductDimensions)

        val dataSlot = slot<TelemetryEvent>()
        verify(exactly = 1) { telemetryManager.enqueue(testUserId, capture(dataSlot)) }
        val event = dataSlot.captured
        assertEquals(testEvent, event.name)
        assertEquals(mapOf(Pair("flow", "test-product-flow"), Pair("dimension1", "dim1val1")), event.dimensions)
    }

    @Test
    fun `measureViewFocusInteraction enqueues`() {
        val testEvent = "test.event"
        val testProductDimensions = mapOf(Pair("dimension1", "dim1val1"))
        every { productMetricsDelegate.userId } returns testUserId

        measureOnViewFocused(testEvent, productMetricsDelegate, testProductDimensions)

        val dataSlot = slot<TelemetryEvent>()
        verify(exactly = 1) { telemetryManager.enqueue(testUserId, capture(dataSlot)) }
        val event = dataSlot.captured
        assertEquals(testEvent, event.name)
    }
}
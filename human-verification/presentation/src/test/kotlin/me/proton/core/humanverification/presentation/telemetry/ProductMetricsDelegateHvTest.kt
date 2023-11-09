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

package me.proton.core.humanverification.presentation.telemetry

import io.mockk.mockk
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.presentation.ProductMetricsDelegate.Companion.KEY_ITEM
import me.proton.core.telemetry.presentation.ProductMetricsDelegate.Companion.KEY_RESULT
import me.proton.core.telemetry.presentation.ProductMetricsDelegate.Companion.VALUE_SUCCESS
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductMetricsDelegateHvTest {
    private val testProductGroup = "testGroup"
    private val testProductFlow = "testFlow"

    private val tested = object : ProductMetricsDelegateHv {
        override val productGroup: String = testProductGroup
        override val productFlow: String = testProductFlow
        override val telemetryManager: TelemetryManager = mockk()
    }

    @Test
    fun `convert to telemetry help event`() {
        val result = tested.toTelemetryEvent("helpTest", item = "itemTest")
        assertEquals(testProductGroup, result.group)
        assertEquals("helpTest", result.name)
        assertEquals(
            mapOf(KEY_ITEM to "itemTest"),
            result.dimensions
        )
    }

    @Test
    fun `convert to telemetry BE event`() {
        val result = tested.toTelemetryEvent("beCall", isSuccess = true)
        assertEquals(testProductGroup, result.group)
        assertEquals("beCall", result.name)
        assertEquals(
            mapOf(KEY_RESULT to VALUE_SUCCESS),
            result.dimensions
        )
    }
}

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

package me.proton.core.observability.domain

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.observability.domain.entity.SignupScreenViewTotalV1
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservabilityMetricsTest {
    @Test
    fun metricNameAndVersion() {
        val data = SignupScreenViewTotalV1(
            1,
            SignupScreenViewTotalV1.Labels(SignupScreenViewTotalV1.Screen_id.chooseInternalEmail)
        )
        assertEquals("android_core_signup_screenView_total", data.metricName)
        assertEquals(1, data.metricVersion)
    }

    @Test
    fun metricEventSerialization() {
        val data = SignupScreenViewTotalV1(
            1,
            SignupScreenViewTotalV1.Labels(SignupScreenViewTotalV1.Screen_id.chooseInternalEmail)
        )
        val jsonString = Json.encodeToString(data)
        val decodedData = Json.decodeFromString<SignupScreenViewTotalV1>(jsonString)
        assertEquals(data, decodedData)
    }

    @Test
    fun observabilityEvent() {
        val data = SignupScreenViewTotalV1(
            1,
            SignupScreenViewTotalV1.Labels(SignupScreenViewTotalV1.Screen_id.chooseInternalEmail)
        )
        val event = ObservabilityEvent(data)
        val jsonString = Json.encodeToString(event)
        val decodedEvent = Json.decodeFromString<ObservabilityEvent<SignupScreenViewTotalV1>>(jsonString)
        assertEquals(event, decodedEvent)
    }
}

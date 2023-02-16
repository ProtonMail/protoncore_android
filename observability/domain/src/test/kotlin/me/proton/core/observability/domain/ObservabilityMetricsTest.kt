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

import me.proton.core.observability.domain.metrics.SignupScreenViewTotalV1
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.serialize
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservabilityMetricsTest {
    @Test
    fun metricNameAndVersion() {
        val data = SignupScreenViewTotalV1(SignupScreenViewTotalV1.ScreenId.chooseInternalEmail)
        assertEquals("android_core_signup_screenView_total", data.metricName)
        assertEquals(1, data.metricVersion)
    }

    @Test
    fun metricEventSerialization() {
        val data = SignupScreenViewTotalV1(SignupScreenViewTotalV1.ScreenId.chooseInternalEmail)
        val jsonString = data.serialize()
        val decodedData = jsonString.deserialize<SignupScreenViewTotalV1>()
        assertEquals(data, decodedData)
    }
}

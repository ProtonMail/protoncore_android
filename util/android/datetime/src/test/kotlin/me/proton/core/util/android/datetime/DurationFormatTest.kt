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

package me.proton.core.util.android.datetime

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit


@RunWith(RobolectricTestRunner::class)
class DurationFormatTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    data class Input(
        val duration: Duration,
        val startUnit: DurationUnit,
        val endUnit: DurationUnit,
        val expected: String
    )

    private val tests = listOf(
        Input(
            duration = 1.hours + 1.minutes,
            startUnit = DurationUnit.HOURS,
            endUnit = DurationUnit.MINUTES,
            expected = "1 hour 1 minute"
        ),
        Input(
            duration = 12.hours + 12.minutes,
            startUnit = DurationUnit.HOURS,
            endUnit = DurationUnit.MINUTES,
            expected = "12 hours 12 minutes"
        ),
        Input(
            duration = 12.hours + 12.minutes,
            startUnit = DurationUnit.DAYS,
            endUnit = DurationUnit.NANOSECONDS,
            expected = "12 hours 12 minutes"
        ),
        Input(
            duration = 240.hours + 120.minutes + 120.seconds,
            startUnit = DurationUnit.DAYS,
            endUnit = DurationUnit.HOURS,
            expected = "10 days 2 hours"
        ),
        Input(
            duration = 240.hours + 120.minutes + 120.seconds,
            startUnit = DurationUnit.DAYS,
            endUnit = DurationUnit.MINUTES,
            expected = "10 days 2 hours 2 minutes"
        ),
        Input(
            duration = 240.hours + 120.minutes + 120.seconds,
            startUnit = DurationUnit.DAYS,
            endUnit = DurationUnit.SECONDS,
            expected = "10 days 2 hours 2 minutes"
        ),
        Input(
            duration = 240.hours + 120.minutes + 121.seconds,
            startUnit = DurationUnit.DAYS,
            endUnit = DurationUnit.SECONDS,
            expected = "10 days 2 hours 2 minutes 1 second"
        ),
    )


    @Test
    fun tests() {
        tests.forEach { input ->
            val actual = DurationFormat(context).format(input.duration, input.startUnit, input.endUnit)
            assertEquals(expected = input.expected, actual = actual)
        }
    }
}

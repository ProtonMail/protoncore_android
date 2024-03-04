/*
 * Copyright (c) 2024 Proton Technologies AG
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

import android.content.Context
import me.proton.core.presentation.R
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.DurationUnit.DAYS
import kotlin.time.DurationUnit.HOURS
import kotlin.time.DurationUnit.MICROSECONDS
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.DurationUnit.MINUTES
import kotlin.time.DurationUnit.NANOSECONDS
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

public class DurationFormat(
    context: Context
) {
    private val resources = context.resources

    public fun format(
        duration: Duration,
        startUnit: DurationUnit = DAYS,
        endUnit: DurationUnit = SECONDS,
        separator: Char = ' '
    ): String {
        if (startUnit < endUnit) return ""
        val current = duration.inWholeUnit(startUnit).toInt()
        val remaining = duration - current.toDuration(startUnit)
        val nextUnit = startUnit.getNextUnit()
        val currentFormat = startUnit.formatDuration(current)
        return when {
            nextUnit == null -> if (current == 0) "" else currentFormat
            current == 0 -> format(remaining, nextUnit, endUnit)
            else -> currentFormat + format(remaining, nextUnit, endUnit).let { next ->
                when {
                    next.isBlank() -> next
                    else -> separator + next
                }
            }
        }
    }

    private fun Duration.inWholeUnit(durationUnit: DurationUnit) = when (durationUnit) {
        DAYS -> inWholeDays
        HOURS -> inWholeHours
        MINUTES -> inWholeMinutes
        SECONDS -> inWholeSeconds
        MILLISECONDS -> inWholeMilliseconds
        MICROSECONDS -> inWholeMicroseconds
        NANOSECONDS -> inWholeNanoseconds
    }

    private fun DurationUnit.formatDuration(value: Int) = when (this) {
        DAYS -> resources.getQuantityString(R.plurals.presentation_datetime_day, value, value)
        HOURS -> resources.getQuantityString(R.plurals.presentation_datetime_hour, value, value)
        MINUTES -> resources.getQuantityString(R.plurals.presentation_datetime_minute, value, value)
        SECONDS -> resources.getQuantityString(R.plurals.presentation_datetime_second, value, value)
        MILLISECONDS -> resources.getQuantityString(R.plurals.presentation_datetime_millisecond, value, value)
        MICROSECONDS -> resources.getQuantityString(R.plurals.presentation_datetime_microsecond, value, value)
        NANOSECONDS -> resources.getQuantityString(R.plurals.presentation_datetime_nanosecond, value, value)
    }

    private fun DurationUnit.getNextUnit() = when (this) {
        DAYS -> HOURS
        HOURS -> MINUTES
        MINUTES -> SECONDS
        SECONDS -> MILLISECONDS
        MILLISECONDS -> MICROSECONDS
        MICROSECONDS -> NANOSECONDS
        NANOSECONDS -> null
    }
}

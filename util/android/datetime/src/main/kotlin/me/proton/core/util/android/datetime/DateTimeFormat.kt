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
import android.text.format.DateFormat
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage
import java.text.SimpleDateFormat
import java.util.Date

@ExcludeFromCoverage
public class DateTimeFormat(
    context: Context
) {
    private val shortDateFormat by lazy { DateFormat.getDateFormat(context) }
    private val mediumDateFormat by lazy { DateFormat.getMediumDateFormat(context) }
    private val longDateFormat by lazy { DateFormat.getLongDateFormat(context) }
    private val shortTimeFormat by lazy { SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT) }
    private val mediumTimeFormat by lazy { SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM) }
    private val longTimeFormat by lazy { SimpleDateFormat.getTimeInstance(SimpleDateFormat.LONG) }
    private fun Long.toDate(): Date = Date(this * 1000)
    private fun formatToShortDate(epochSeconds: Long): String = shortDateFormat.format(epochSeconds.toDate())
    private fun formatToMediumDate(epochSeconds: Long): String = mediumDateFormat.format(epochSeconds.toDate())
    private fun formatToLongDate(epochSeconds: Long): String = longDateFormat.format(epochSeconds.toDate())
    private fun formatToShortTime(epochSeconds: Long): String = shortTimeFormat.format(epochSeconds.toDate())
    private fun formatToMediumTime(epochSeconds: Long): String = mediumTimeFormat.format(epochSeconds.toDate())
    private fun formatToLongTime(epochSeconds: Long): String = longTimeFormat.format(epochSeconds.toDate())
    public fun format(epochSeconds: Long, style: DateTimeForm): String = when (style) {
        DateTimeForm.SHORT_DATE -> formatToShortDate(epochSeconds)
        DateTimeForm.MEDIUM_DATE -> formatToMediumDate(epochSeconds)
        DateTimeForm.LONG_DATE -> formatToLongDate(epochSeconds)
        DateTimeForm.SHORT_TIME -> formatToShortTime(epochSeconds)
        DateTimeForm.MEDIUM_TIME -> formatToMediumTime(epochSeconds)
        DateTimeForm.LONG_TIME -> formatToLongTime(epochSeconds)
    }

    public fun format(date: Date, style: DateTimeForm): String = format(date.time / 1000, style)

    public enum class DateTimeForm {
        /** Short form, such as "03.01.00". */
        SHORT_DATE,

        /** Medium form, such as "Jan 3, 2000". */
        MEDIUM_DATE,

        /** Long form, such as "Monday, January 3, 2000". */
        LONG_DATE,

        /** Short form, such as "11:01 AM". */
        SHORT_TIME,

        /** Medium form, such as "11:01:02 AM". */
        MEDIUM_TIME,

        /** Long form, such as "11:01:02 AM GMT+01:00". */
        LONG_TIME
    }
}

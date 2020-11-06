/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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
package me.proton.core.util.kotlin

import org.jetbrains.annotations.NonNls

/**
 * Abstract logger for Proton core. Should be used instead of directly logging to e.g. logcat.
 */
interface Logger {
    /** Log an error exception. */
    fun e(tag: String, e: Throwable)

    /** Log an error exception and a message. */
    fun e(tag: String, e: Throwable, @NonNls message: String)

    /** Log an info message. */
    fun i(tag: String, @NonNls message: String)

    /** Log an info exception and a message. */
    fun i(tag: String, e: Throwable, @NonNls message: String)

    /** Log a debug message. */
    fun d(tag: String, @NonNls message: String)

    /** Log a debug exception and a message. */
    fun d(tag: String, e: Throwable, @NonNls message: String)

    /** Log a verbose message. */
    fun v(tag: String, @NonNls message: String)

    /** Log a verbose exception and a message. */
    fun v(tag: String, e: Throwable, @NonNls message: String)

    /**
     * Log a message that do not belong to any level.
     *
     * Implementation of this function have to filter on [LoggerLogTag], then map/forward it according their own logic.
     *
     * Note: Use this only if you know on which [LoggerLogTag] you have to filter.
     */
    fun log(tag: LoggerLogTag, @NonNls message: String)
}

/** Type for all tags used in conjunction with [Logger.log]. */
inline class LoggerLogTag(val name: String)

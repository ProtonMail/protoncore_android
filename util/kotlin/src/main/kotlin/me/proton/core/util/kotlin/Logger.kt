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

import me.proton.core.util.kotlin.CoreLogger.set
import org.jetbrains.annotations.NonNls
import java.util.logging.LogManager

/**
 * Abstract logger for Proton core. Should be used instead of directly logging to e.g. logcat.
 */
interface Logger {
    /** Log an error message. */
    fun e(tag: String, @NonNls message: String)

    /** Log an error exception. */
    fun e(tag: String, e: Throwable)

    /** Log an error exception and a message. */
    fun e(tag: String, e: Throwable, @NonNls message: String)

    /** Log a warning message. */
    fun w(tag: String, message: String)

    /** Log a warning exception. */
    fun w(tag: String, e: Throwable)

    /** Log a warning exception and a message. */
    fun w(tag: String, e: Throwable, message: String)

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
}

/**
 * Main object/singleton any Core module is using to log.
 *
 * Call [set] to set your own [Logger].
 */
object CoreLogger : Logger {

    private var logger: Logger? = null

    fun set(logger: Logger) {
        this.logger = logger
        LogManager.getLogManager().apply {
            reset()
            getLogger("").addHandler(LoggingHandler(logger))
        }
    }

    override fun e(tag: String, message: String) {
        logger?.e(tag, message)
    }

    override fun e(tag: String, e: Throwable) {
        logger?.e(tag, e)
    }

    override fun e(tag: String, e: Throwable, message: String) {
        logger?.e(tag, e, message)
    }

    override fun w(tag: String, message: String) {
        logger?.w(tag, message)
    }

    override fun w(tag: String, e: Throwable) {
        logger?.w(tag, e)
    }

    override fun w(tag: String, e: Throwable, message: String) {
        logger?.w(tag, e, message)
    }

    override fun i(tag: String, message: String) {
        logger?.i(tag, message)
    }

    override fun i(tag: String, e: Throwable, message: String) {
        logger?.i(tag, e, message)
    }

    override fun d(tag: String, message: String) {
        logger?.d(tag, message)
    }

    override fun d(tag: String, e: Throwable, message: String) {
        logger?.d(tag, e, message)
    }

    override fun v(tag: String, message: String) {
        logger?.v(tag, message)
    }

    override fun v(tag: String, e: Throwable, message: String) {
        logger?.v(tag, e, message)
    }
}

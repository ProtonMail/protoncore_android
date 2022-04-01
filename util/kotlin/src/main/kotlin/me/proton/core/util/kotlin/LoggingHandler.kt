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

package me.proton.core.util.kotlin

import java.util.logging.Filter
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

internal class LoggingHandler(
    private val logger: Logger,
    loggingFilter: Filter? = null
) : Handler() {
    init {
        filter = loggingFilter
    }

    override fun close() {}
    override fun flush() {}

    override fun publish(record: LogRecord) {
        if (!isLoggable(record)) return

        val tag: String = record.loggerName ?: record.sourceName() ?: "LoggingHandler"
        val logLevel = record.level.intValue()
        val message: String = record.message ?: ""
        val thrown: Throwable? = record.thrown
        when {
            logLevel >= Level.SEVERE.intValue() -> {
                val throwable = thrown ?: Throwable("LoggingHandler: $message")
                logger.e(tag, throwable, message)
            }
            logLevel >= Level.CONFIG.intValue() -> if (thrown != null) {
                logger.d(tag, thrown, message)
            } else {
                logger.d(tag, message)
            }
            else -> if (thrown != null) {
                logger.v(tag, thrown, message)
            } else {
                logger.v(tag, message)
            }
        }
    }

    private fun LogRecord.sourceName(): String? {
        val separator = ":"
        return buildString {
            if (sourceClassName != null) append(sourceClassName)
            append(separator)
            if (sourceMethodName != null) append(sourceMethodName)
        }.takeIf { it.length > separator.length }
    }
}

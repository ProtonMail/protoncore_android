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

package me.proton.core.util.android.sentry

import android.util.Log
import io.sentry.Breadcrumb
import io.sentry.IHub
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import timber.log.Timber

@Suppress("TooManyFunctions")
public class TimberLoggerSentryTree(
    private val hub: IHub,
    private val minEventLevel: SentryLevel,
    private val minBreadcrumbLevel: SentryLevel
) : Timber.Tree() {
    private val pendingTag = ThreadLocal<String?>()

    private fun retrieveTag(): String? {
        val tag = pendingTag.get()
        if (tag != null) {
            this.pendingTag.remove()
        }
        return tag
    }

    override fun v(
        message: String?,
        vararg args: Any?
    ) {
        super.v(message, *args)
        logWithSentry(Log.VERBOSE, null, message, *args)
    }

    override fun v(
        t: Throwable?,
        message: String?,
        vararg args: Any?
    ) {
        super.v(t, message, *args)
        logWithSentry(Log.VERBOSE, t, message, *args)
    }

    override fun v(t: Throwable?) {
        super.v(t)
        logWithSentry(Log.VERBOSE, t, null)
    }

    override fun d(
        message: String?,
        vararg args: Any?
    ) {
        super.d(message, *args)
        logWithSentry(Log.DEBUG, null, message, *args)
    }

    override fun d(
        t: Throwable?,
        message: String?,
        vararg args: Any?
    ) {
        super.d(t, message, *args)
        logWithSentry(Log.DEBUG, t, message, *args)
    }

    override fun d(t: Throwable?) {
        super.d(t)
        logWithSentry(Log.DEBUG, t, null)
    }

    override fun i(
        message: String?,
        vararg args: Any?
    ) {
        super.d(message, *args)
        logWithSentry(Log.INFO, null, message, *args)
    }

    override fun i(
        t: Throwable?,
        message: String?,
        vararg args: Any?
    ) {
        super.i(t, message, *args)
        logWithSentry(Log.INFO, t, message, *args)
    }

    override fun i(t: Throwable?) {
        super.i(t)
        logWithSentry(Log.INFO, t, null)
    }

    override fun w(
        message: String?,
        vararg args: Any?
    ) {
        super.w(message, *args)
        logWithSentry(Log.WARN, null, message, *args)
    }

    override fun w(
        t: Throwable?,
        message: String?,
        vararg args: Any?
    ) {
        super.w(t, message, *args)
        logWithSentry(Log.WARN, t, message, *args)
    }

    override fun w(t: Throwable?) {
        super.w(t)
        logWithSentry(Log.WARN, t, null)
    }

    override fun wtf(
        message: String?,
        vararg args: Any?
    ) {
        super.wtf(message, *args)
        logWithSentry(Log.ASSERT, null, message, *args)
    }

    override fun wtf(
        t: Throwable?,
        message: String?,
        vararg args: Any?
    ) {
        super.wtf(t, message, *args)
        logWithSentry(Log.ASSERT, t, message, *args)
    }

    override fun wtf(t: Throwable?) {
        super.wtf(t)
        logWithSentry(Log.ASSERT, t, null)
    }


    override fun e(
        message: String?,
        vararg args: Any?
    ) {
        super.e(message, *args)
        logWithSentry(Log.ERROR, null, message, *args)
    }

    override fun e(
        t: Throwable?,
        message: String?,
        vararg args: Any?
    ) {
        super.e(t, message, *args)
        logWithSentry(Log.ERROR, t, message, *args)
    }

    override fun e(t: Throwable?) {
        super.e(t)
        logWithSentry(Log.ERROR, t, null)
    }

    override fun log(
        priority: Int,
        message: String?,
        vararg args: Any?
    ) {
        super.log(priority, message, *args)
        logWithSentry(priority, null, message, *args)
    }

    override fun log(
        priority: Int,
        t: Throwable?,
        message: String?,
        vararg args: Any?
    ) {
        super.log(priority, t, message, *args)
        logWithSentry(priority, t, message, *args)
    }

    override fun log(
        priority: Int,
        t: Throwable?
    ) {
        super.log(priority, t)
        logWithSentry(priority, t, null)
    }

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        pendingTag.set(tag)
    }

    private fun logWithSentry(
        priority: Int,
        throwable: Throwable?,
        message: String?,
        vararg args: Any?
    ) {
        val tag = retrieveTag()

        if (message.isNullOrEmpty() && throwable == null) {
            return // Swallow message if it's null and there's no throwable
        }

        val level = getSentryLevel(priority)
        val sentryMessage = Message().apply {
            this.message = message
            if (!message.isNullOrEmpty() && args.isNotEmpty()) {
                this.formatted = message.format(*args)
            }
            this.params = args.map { it.toString() }
        }

        captureEvent(level, tag, sentryMessage, throwable)
        addBreadcrumb(level, tag?.substringAfterLast('.'), sentryMessage, throwable)
    }

    /**
     * Captures an event with the given attributes
     */
    private fun captureEvent(
        sentryLevel: SentryLevel,
        tag: String?,
        msg: Message,
        throwable: Throwable?
    ) {
        if (isLoggable(sentryLevel, minEventLevel)) {
            val sentryEvent = SentryEvent().apply {
                level = sentryLevel
                throwable?.let { setThrowable(it) }
                tag?.let {
                    setTag(TIMBER_LOGGER_TAG, tag)
                }
                message = msg
                logger = TIMBER_LOGGER
            }

            hub.captureEvent(sentryEvent)
        }
    }

    /**
     * Adds a breadcrumb
     */
    private fun addBreadcrumb(
        sentryLevel: SentryLevel,
        cat: String?,
        msg: Message,
        throwable: Throwable?
    ) {
        // checks the breadcrumb level
        if (isLoggable(sentryLevel, minBreadcrumbLevel)) {
            val throwableMsg = throwable?.message
            val breadCrumb = when {
                msg.message != null -> Breadcrumb().apply {
                    level = sentryLevel
                    category = cat
                    message = msg.formatted ?: msg.message
                }

                throwableMsg != null -> Breadcrumb.error(throwableMsg).apply {
                    category = "exception"
                }

                else -> null
            }

            breadCrumb?.let { hub.addBreadcrumb(it) }
        }
    }

    /**
     * do not log if it's lower than min. required level.
     */
    private fun isLoggable(
        level: SentryLevel,
        minLevel: SentryLevel
    ): Boolean = level.ordinal >= minLevel.ordinal

    /**
     * Converts from Timber priority to SentryLevel.
     * Fallback to SentryLevel.DEBUG.
     */
    private fun getSentryLevel(priority: Int): SentryLevel {
        return when (priority) {
            Log.ASSERT -> SentryLevel.FATAL
            Log.ERROR -> SentryLevel.ERROR
            Log.WARN -> SentryLevel.WARNING
            Log.INFO -> SentryLevel.INFO
            Log.DEBUG -> SentryLevel.DEBUG
            Log.VERBOSE -> SentryLevel.DEBUG
            else -> SentryLevel.DEBUG
        }
    }
}
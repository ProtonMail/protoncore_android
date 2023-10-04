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

package me.proton.core.util.android.sentry

import io.sentry.EventProcessor
import io.sentry.Hint
import io.sentry.SentryEvent

/**
 * Decorates a [SentryEvent] with a tag ([TIMBER_LOGGER_TAG]).
 * The event is decorated only if:
 * - it's a [SentryEvent.isCrashed],
 * - there is no [TIMBER_LOGGER_TAG] present,
 * - any of its exception packages matches one of the [allowedModulePrefixes].
 * The value of the tag is always [TAG_UNCAUGHT_EXCEPTION] (if the event has been decorated).
 * @param allowedModulePrefixes A set of module prefixes for which the event can be decorated.
 */
public class CrashEventTimberTagDecorator(
    private val allowedModulePrefixes: Set<String> = setOf("")
) : EventProcessor {
    override fun process(event: SentryEvent, hint: Hint): SentryEvent {
        if (!event.isCrashed || event.getTag(TIMBER_LOGGER_TAG) != null) {
            return event
        }

        event.exceptions.orEmpty().flatMap {
            it.stacktrace?.frames.orEmpty().mapNotNull { frame -> frame.module }
        }.firstOrNull { module ->
            allowedModulePrefixes.any { prefix -> module.startsWith(prefix) }
        }?.let {
            event.setTag(TIMBER_LOGGER_TAG, TAG_UNCAUGHT_EXCEPTION)
        }

        return event
    }
}

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
 * Filters out [events][SentryEvent] based on the [allowedTagPrefixes] and [blockedTagPrefixes].
 * The [blockedTagPrefixes] take precedence over [allowedTagPrefixes].
 * @param allowedTagPrefixes A set of tag prefixes that are allowed.
 *  By default, all prefixes are allowed (including a case when [TIMBER_LOGGER_TAG] has a `null` value).
 * @param blockedTagPrefixes A set of tag prefixes that are blocked.
 *  By default, none are blocked.
 */
public class TimberTagEventFilter(
    private val allowedTagPrefixes: Set<String> = setOf(""),
    private val blockedTagPrefixes: Set<String> = emptySet()
) : EventProcessor {
    override fun process(event: SentryEvent, hint: Hint): SentryEvent? {
        val tag = event.getTag(TIMBER_LOGGER_TAG) ?: ""
        return when {
            blockedTagPrefixes.any { prefix -> tag.startsWith(prefix) } -> null
            allowedTagPrefixes.any { prefix -> tag.startsWith(prefix) } -> event
            else -> null
        }
    }
}

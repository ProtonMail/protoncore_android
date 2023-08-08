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

import io.sentry.Hub
import io.sentry.IHub
import io.sentry.NoOpHub
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.protocol.SentryId
import io.sentry.protocol.User
import kotlin.concurrent.getOrSet

/** Thread-safe wrapper for [Hub]. */
public class SentryHub(sentryOptions: SentryOptions) {
    private val currentHub = ThreadLocalValue<IHub>(
        originalValue = when {
            sentryOptions.dsn.isNullOrBlank() -> NoOpHub.getInstance()
            else -> Hub(sentryOptions)
        },
        clone = { it.clone() }
    )

    init {
        sentryOptions.integrations.forEach {
            it.register(currentHub(), sentryOptions)
        }
    }

    public fun captureEvent(event: SentryEvent): SentryId = currentHub().captureEvent(event)

    public fun setUser(user: User?) {
        currentHub().setUser(user)
    }
}

internal class ThreadLocalValue<T : Any>(
    private val originalValue: T,
    private val clone: (T) -> T
) {
    private val threadLocal = ThreadLocal<T>().apply {
        set(originalValue)
    }

    operator fun invoke(): T = threadLocal.getOrSet { clone(originalValue) }
}

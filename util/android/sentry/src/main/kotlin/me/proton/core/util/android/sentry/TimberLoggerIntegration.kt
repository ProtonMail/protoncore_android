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

import io.sentry.IHub
import io.sentry.ILogger
import io.sentry.Integration
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import timber.log.Timber
import java.io.Closeable

public class TimberLoggerIntegration(
    private val minEventLevel: SentryLevel = SentryLevel.ERROR,
    private val minBreadcrumbLevel: SentryLevel = SentryLevel.INFO
) : Integration, Closeable {
    private lateinit var tree: TimberLoggerSentryTree
    private lateinit var logger: ILogger

    override fun register(hub: IHub, options: SentryOptions) {
        logger = options.logger

        tree = TimberLoggerSentryTree(hub, minEventLevel, minBreadcrumbLevel)
        Timber.plant(tree)

        addIntegrationToSdkVersion()
    }

    override fun close() {
        if (this::tree.isInitialized) {
            Timber.uproot(tree)
        }
    }
}

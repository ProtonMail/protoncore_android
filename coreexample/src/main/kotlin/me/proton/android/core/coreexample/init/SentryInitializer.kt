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

package me.proton.android.core.coreexample.init

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import me.proton.android.core.coreexample.BuildConfig
import me.proton.core.usersettings.domain.DeviceSettingsHandler
import me.proton.core.usersettings.domain.onDeviceSettingsChanged
import me.proton.core.util.android.sentry.TimberLoggerIntegration

class SentryInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SentryInitializerEntryPoint::class.java
        )

        entryPoint.deviceSettingsHandler()
            .onDeviceSettingsChanged { settings ->
                if (settings.isCrashReportEnabled) {
                    if (BuildConfig.SENTRY_DSN.isNotBlank()) {
                        SentryAndroid.init(context) { options ->
                            options.dsn = BuildConfig.SENTRY_DSN
                            if (!BuildConfig.DEBUG) {
                                options.addIntegration(
                                    TimberLoggerIntegration(
                                        minEventLevel = SentryLevel.ERROR,
                                        minBreadcrumbLevel = SentryLevel.INFO
                                    )
                                )
                            }
                        }
                    }
                } else {
                    SentryAndroid.init(context) { options ->
                        options.dsn = ""
                    }
                }
            }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SentryInitializerEntryPoint {
        fun deviceSettingsHandler(): DeviceSettingsHandler
    }
}
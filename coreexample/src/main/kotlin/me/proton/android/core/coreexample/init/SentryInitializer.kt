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

package me.proton.android.core.coreexample.init

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import me.proton.android.core.coreexample.BuildConfig
import me.proton.core.usersettings.domain.DeviceSettingsHandler
import me.proton.core.usersettings.domain.onDeviceSettingsChanged
import me.proton.core.util.android.sentry.IsAccountSentryLoggingEnabled
import me.proton.core.util.android.sentry.TimberLoggerIntegration
import me.proton.core.util.android.sentry.project.AccountSentryHubBuilder
import java.util.concurrent.atomic.AtomicBoolean

class SentryInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val isAccountSentryEnabled = IsAccountSentryLoggingEnabled(context).invoke()
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SentryInitializerEntryPoint::class.java
        )

        val isCrashReportEnabled = AtomicBoolean(true)
        entryPoint.deviceSettingsHandler().onDeviceSettingsChanged { settings ->
            isCrashReportEnabled.set(settings.isCrashReportEnabled)
        }

        val beforeSendCallback = SentryOptions.BeforeSendCallback { event, _ ->
            if (isCrashReportEnabled.get()) event else null
        }

        SentryAndroid.init(context) { options ->
            options.beforeSend = beforeSendCallback
            options.dsn = BuildConfig.SENTRY_DSN.takeIf { !BuildConfig.DEBUG }.orEmpty()
            options.environment = BuildConfig.FLAVOR
            options.release = context.appPackageInfo().versionName ?: "unknown"

            options.addIntegration(
                TimberLoggerIntegration(
                    minEventLevel = SentryLevel.ERROR,
                    minBreadcrumbLevel = SentryLevel.INFO
                )
            )
        }

        // Account Sentry:
        entryPoint.accountSentryHubBuilder().invoke(
            sentryDsn = BuildConfig.ACCOUNT_SENTRY_DSN.takeIf { !BuildConfig.DEBUG && isAccountSentryEnabled}.orEmpty()
        ) { options ->
            options.beforeSend = beforeSendCallback
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SentryInitializerEntryPoint {
        fun accountSentryHubBuilder(): AccountSentryHubBuilder
        fun deviceSettingsHandler(): DeviceSettingsHandler
    }
}

private fun Context.appPackageInfo(): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        applicationContext.packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
    } else {
        @Suppress("DEPRECATION")
        applicationContext.packageManager.getPackageInfo(packageName, 0)
    }

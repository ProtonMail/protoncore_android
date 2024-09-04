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

import android.content.Context
import io.sentry.SentryLevel
import io.sentry.android.core.AppLifecycleIntegration
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.protocol.User
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.util.android.device.DeviceMetadata
import java.io.File
import javax.inject.Inject

/** Maximum length of a release name (as required by Sentry). */
private const val MAX_RELEASE_NAME_LEN = 200

/** A builder for creating an instance of custom [SentryHub]. */
public class SentryHubBuilder @Inject constructor() {
    /**
     * Configures a standalone instance of [SentryHub],
     * which can be used independently of the global [io.sentry.Sentry] instance.
     * This method should be called once, at the application start.
     * Prerequisite: call [me.proton.core.util.kotlin.CoreLogger.set] with [TimberLogger].
     * After the above is done, you can start logging with [me.proton.core.util.kotlin.CoreLogger].
     *
     * @param sentryDsn The DSN to use for this instance.
     *  Passing an empty string will create [io.sentry.NoOpHub], i.e. Sentry will be effectively disabled.
     * @param allowedPackagePrefixes A set of package prefixes, for which uncaught exceptions can be logged.
     *  By default, all are allowed. Applies only if [shouldReportUncaughtExceptions] is `true`.
     * @param allowedTagPrefixes A set of tag prefixes that are allowed to be logged. By default, all are allowed.
     * @param cacheDir Cache dir for [SentryAndroidOptions.cacheDirPath].
     * @param envName Environment name for [SentryAndroidOptions.environment].
     * @param inAppIncludes Package names for [SentryAndroidOptions.inAppIncludes].
     * @param installationId A unique ID to use for [io.sentry.Sentry.setUser].
     * @param minBreadcrumbLevel The minimum level required to log Sentry breadcrumbs.
     * @param minEventLevel The minimum level required to log separate Sentry events.
     * @param releaseName Release name for [SentryAndroidOptions.release].
     * @param shouldReportUncaughtExceptions If true, the uncaught exceptions will also be reported.
     * @param additionalConfiguration Any additional configuration for [SentryAndroidOptions].
     */
    public operator fun invoke(
        context: Context,
        apiClient: ApiClient,
        networkPrefs: NetworkPrefs,
        sentryDsn: String,
        allowedPackagePrefixes: Set<String> = setOf(""),
        allowedTagPrefixes: Set<String> = setOf(""),
        cacheDir: File? = null,
        envName: String? = null,
        inAppIncludes: List<String> = emptyList(),
        installationId: String? = null,
        minBreadcrumbLevel: SentryLevel = SentryLevel.INFO,
        minEventLevel: SentryLevel = SentryLevel.ERROR,
        releaseName: String? = null,
        shouldReportUncaughtExceptions: Boolean = true,
        additionalConfiguration: ((SentryAndroidOptions) -> Unit)? = null
    ): SentryHub = SentryHub(SentryAndroidOptions().apply {
        dsn = sentryDsn

        inAppIncludes.forEach { addInAppInclude(it) }

        addIntegration(AppLifecycleIntegration())
        addIntegration(
            TimberLoggerIntegration(
                minEventLevel = minEventLevel,
                minBreadcrumbLevel = minBreadcrumbLevel
            )
        )

        if (shouldReportUncaughtExceptions) {
            addEventProcessor(CrashEventTimberTagDecorator(allowedPackagePrefixes))
        }

        val uncaughtExceptionTag = when (shouldReportUncaughtExceptions) {
            true -> setOf(TAG_UNCAUGHT_EXCEPTION)
            else -> emptySet()
        }
        addEventProcessor(TimberTagEventFilter(allowedTagPrefixes = allowedTagPrefixes + uncaughtExceptionTag))
        addEventProcessor(CustomSentryTagsProcessor(
            context = context,
            apiClient = apiClient,
            deviceMetadata = DeviceMetadata(),
            networkPrefs = networkPrefs
        ))
        cacheDirPath = cacheDir?.absolutePath
        environment = envName
        isEnableUncaughtExceptionHandler = shouldReportUncaughtExceptions
        isSendDefaultPii = false
        release =
            releaseName?.filter { it.isLetterOrDigit() || it in "-_." }?.take(MAX_RELEASE_NAME_LEN)

        additionalConfiguration?.invoke(this)

        require(envName == null || environment == envName) { "Clients should not overwrite the `environment` option." }
        require(releaseName == null || release == releaseName) { "Clients should not overwrite the `release` option." }
    }).apply {
        setUser(installationId?.let { User().apply { id = it } })
    }
}

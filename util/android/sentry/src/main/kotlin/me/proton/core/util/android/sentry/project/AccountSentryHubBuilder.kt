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

package me.proton.core.util.android.sentry.project

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.android.core.SentryAndroidOptions
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.util.android.sentry.BuildConfig
import me.proton.core.util.android.sentry.GetInstallationId
import me.proton.core.util.android.sentry.IsAccountSentryLoggingEnabled
import me.proton.core.util.android.sentry.SentryHub
import me.proton.core.util.android.sentry.SentryHubBuilder
import okhttp3.HttpUrl
import java.io.File
import javax.inject.Inject

private const val ACCOUNT_SENTRY_PREFS_NAME = "core.account.sentry"
private const val KEY_INSTALLATION_ID = "installationId"


public class AccountSentryHubBuilder @Inject constructor(
    private val builder: SentryHubBuilder,
    @BaseProtonApiUrl private val apiUrl: HttpUrl,
    @ApplicationContext private val context: Context,
    private val getInstallationId: GetInstallationId,
    private val accountSentryEnabled: IsAccountSentryLoggingEnabled
) {
    private val allowedPackagePrefixes = setOf(
        "me.proton.core.account",
        "me.proton.core.accountmanager",
        "me.proton.core.accountrecovery",
        "me.proton.core.auth",
        "me.proton.core.challenge",
        "me.proton.core.data",
        "me.proton.core.data-room",
        "me.proton.core.devicemigration",
        "me.proton.core.domain",
        "me.proton.core.eventmanager",
        "me.proton.core.featureflag",
        "me.proton.core.humanverification",
        "me.proton.core.key",
        "me.proton.core.network",
        "me.proton.core.notification",
        "me.proton.core.observability",
        "me.proton.core.passvalidator",
        "me.proton.core.payment",
        "me.proton.core.paymentiap",
        "me.proton.core.plan",
        "me.proton.core.presentation",
        "me.proton.core.presentation-compose",
        "me.proton.core.push",
        "me.proton.core.report",
        "me.proton.core.telemetry",
        "me.proton.core.user",
        "me.proton.core.usersettings",
        "me.proton.core.userrecovery",
        "me.proton.core.util"
    )

    private val allowedTagPrefixes = setOf(
        "core.account",
        "core.accountmanager",
        "core.accountrecovery",
        "core.auth",
        "core.challenge",
        "core.data",
        "core.data-room",
        "core.devicemigration",
        "core.domain",
        "core.eventmanager",
        "core.featureflag",
        "core.humanverification",
        "core.key",
        "core.network",
        "core.notification",
        "core.observability",
        "core.pass-validator",
        "core.payment",
        "core.paymentiap",
        "core.plan",
        "core.presentation",
        "core.presentation-compose",
        "core.push",
        "core.report",
        "core.telemetry",
        "core.user",
        "core.usersettings",
        "core.userrecovery",
        "core.util"
    )

    public operator fun invoke(
        sentryDsn: String,
        installationId: String? = null,
        additionalConfiguration: ((SentryAndroidOptions) -> Unit)? = null
    ): SentryHub {
        return builder(
            sentryDsn = sentryDsn.takeIf { accountSentryEnabled() }.orEmpty(),
            allowedPackagePrefixes = allowedPackagePrefixes,
            allowedTagPrefixes = allowedTagPrefixes,
            cacheDir = File(context.cacheDir, "sentry-account"),
            envName = apiUrl.host,
            inAppIncludes = listOf("me.proton.core"),
            installationId = installationId ?: getInstallationId(
                prefsName = ACCOUNT_SENTRY_PREFS_NAME,
                key = KEY_INSTALLATION_ID
            ),
            releaseName = BuildConfig.CORE_VERSION,
            additionalConfiguration = additionalConfiguration
        )
    }
}
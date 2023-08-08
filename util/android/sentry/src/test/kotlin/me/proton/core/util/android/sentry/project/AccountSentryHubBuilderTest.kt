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
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.util.android.sentry.GetInstallationId
import me.proton.core.util.android.sentry.SentryHubBuilder
import okhttp3.HttpUrl
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertNotNull

class AccountSentryHubBuilderTest {

    // region mocks
    private val sentryHubBuilder = mockk<SentryHubBuilder>(relaxed = true)
    private val apiUrl = mockk<HttpUrl>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val getInstallationId = mockk<GetInstallationId>(relaxed = true)
    // endregion

    // region test variables
    private val allowedPackagePrefixes = setOf(
        "me.proton.core.account",
        "me.proton.core.accountmanager",
        "me.proton.core.accountrecovery",
        "me.proton.core.auth",
        "me.proton.core.challenge",
        "me.proton.core.data",
        "me.proton.core.data-room",
        "me.proton.core.domain",
        "me.proton.core.eventmanager",
        "me.proton.core.featureflag",
        "me.proton.core.humanverification",
        "me.proton.core.key",
        "me.proton.core.network",
        "me.proton.core.notification",
        "me.proton.core.observability",
        "me.proton.core.payment",
        "me.proton.core.paymentiap",
        "me.proton.core.plan",
        "me.proton.core.presentation",
        "me.proton.core.presentation-compose",
        "me.proton.core.push",
        "me.proton.core.report",
        "me.proton.core.user",
        "me.proton.core.usersettings",
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
        "core.domain",
        "core.eventmanager",
        "core.featureflag",
        "core.humanverification",
        "core.key",
        "core.network",
        "core.notification",
        "core.observability",
        "core.payment",
        "core.paymentiap",
        "core.plan",
        "core.presentation",
        "core.presentation-compose",
        "core.push",
        "core.report",
        "core.user",
        "core.usersettings",
        "core.util"
    )
    // endregion

    private lateinit var accountSentryHubBuilder: AccountSentryHubBuilder
    private val ACCOUNT_SENTRY_PREFS_NAME = "core.account.sentry"
    private val KEY_INSTALLATION_ID = "installationId"

    @Before
    fun beforeEveryTest() {
        accountSentryHubBuilder = AccountSentryHubBuilder(
            builder = sentryHubBuilder,
            apiUrl = apiUrl,
            context = context,
            getInstallationId = getInstallationId
        )
    }

    @Test
    fun `init Account Sentry Hub default params`() {
        val dsn = "test-dsn"
        every { context.cacheDir } returns File("test-cache-dir")
        every { apiUrl.host } returns "test-host"
        every { getInstallationId(
            prefsName = ACCOUNT_SENTRY_PREFS_NAME,
            key = KEY_INSTALLATION_ID
        ) } returns "test-installation-id"
        val result = accountSentryHubBuilder(sentryDsn = dsn)

        assertNotNull(result)
        verify { sentryHubBuilder.invoke(
            sentryDsn = dsn,
            allowedPackagePrefixes = allowedPackagePrefixes,
            allowedTagPrefixes = allowedTagPrefixes,
            cacheDir = any(),
            envName = "test-host",
            inAppIncludes = listOf("me.proton.core"),
            installationId = "test-installation-id",
            releaseName = any(),
            additionalConfiguration = null
        ) }
    }

    @Test
    fun `init Account Sentry Hub provided params`() {
        val dsn = "test-dsn"
        val installationId = "test-installation-id"
        every { context.cacheDir } returns File("test-cache-dir")
        every { apiUrl.host } returns "test-host"
        val result = accountSentryHubBuilder(sentryDsn = dsn, installationId = installationId)

        assertNotNull(result)
        verify { sentryHubBuilder.invoke(
            sentryDsn = dsn,
            allowedPackagePrefixes = allowedPackagePrefixes,
            allowedTagPrefixes = allowedTagPrefixes,
            cacheDir = any(),
            envName = "test-host",
            inAppIncludes = listOf("me.proton.core"),
            installationId = installationId,
            releaseName = any(),
            additionalConfiguration = null
        ) }
        verify(exactly = 0) { getInstallationId.invoke(any(), any()) }
    }
}
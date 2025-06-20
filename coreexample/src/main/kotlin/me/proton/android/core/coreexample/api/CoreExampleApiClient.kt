/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.android.core.coreexample.api

import android.os.Build
import me.proton.android.core.coreexample.BuildConfig
import me.proton.core.network.domain.ApiClient
import java.util.Locale
import javax.inject.Inject

open class CoreExampleApiClient @Inject constructor() : ApiClient {
    protected open val appName = "android-mail"
    protected open val productName = "ProtonMail"
    protected open val versionName = "6.0.0" // imitating ProtonMail version
    protected open val versionSuffix = if (BuildConfig.DEBUG) "-dev" else ""

    /**
     * Tells the lib if DoH should be used in a given moment (based e.g. on user setting or whether
     * VPN connection is active). Will be checked before  each API call.
     */
    override suspend fun shouldUseDoh(): Boolean = BuildConfig.USE_DOH

    /**
     * Client's value for 'x-pm-appversion' header.
     */
    override val appVersionHeader: String
        get() = "$appName@$versionName$versionSuffix"

    /**
     * Client's value for 'User-Agent' header.
     */
    override val userAgent: String
        get() = String.format(
            Locale.US,
            "%s/%s (Android %s; %s; %s %s; %s)",
            productName,
            versionName,
            Build.VERSION.RELEASE,
            Build.MODEL,
            Build.BRAND,
            Build.DEVICE,
            Locale.getDefault().language
        )

    /**
     * Enables debug logging in the underlying HTTP library.
     */
    override val enableDebugLogging: Boolean
        get() = true

    /**
     * Tells client to force update (this client will no longer be accepted by the API).
     *
     * @param errorMessage the localized error message the user should see.
     */
    override fun forceUpdate(errorMessage: String) {
        // dummy example, not implemented for now
    }
}

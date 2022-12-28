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
package me.proton.core.network.domain

import java.util.concurrent.TimeUnit

/**
 * Represents the client of the library. Enables 2-way communication between the lib and the client.
 */
interface ApiClient {

    /**
     * Tells the lib if DoH should be used in a given moment (based e.g. on user setting or whether
     * VPN connection is active). Will be checked before  each API call.
     */
    val shouldUseDoh: Boolean

    /**
     * Client's value for 'x-pm-appversion' header.
     */
    val appVersionHeader: String

    /**
     * Client's value for 'User-Agent' header.
     */
    val userAgent: String

    /**
     * Timeouts for internal api call attempt (due to error handling logic there might be multiple internal
     * calls in a single API call by the client).
     */
    val connectTimeoutSeconds: Long get() = 3
    val readTimeoutSeconds: Long get() = 30
    val writeTimeoutSeconds: Long get() = 30

    /**
     * Main timeout for single call including DNS, connecting, writes and reads. Cannot override for specific calls :(
     * Default is 0, meaning no timeout.
     * If > 0, call timeout has a precedence over any other timeout type.
     */
    val callTimeoutSeconds: Long get() = 0

    /**
     * This value will be applied to connect, read and write for API pings.
     */
    val pingTimeoutSeconds: Int get() = 3

    /**
     * How long alternative API proxy will be used before primary API is attempted again.
     */
    val proxyValidityPeriodMs: Long get() = TimeUnit.MINUTES.toMillis(90)

    /**
     * Timeout for DoH queries.
     */
    val dohServiceTimeoutMs: Long get() = TimeUnit.SECONDS.toMillis(10)

    /**
     * Total time for trying alternative routing proxies.
     */
    val alternativesTotalTimeout: Long get() = TimeUnit.SECONDS.toMillis(30)

    /**
     * Retry count for exponential backoff.
     */
    val backoffRetryCount: Int get() = 2

    /**
     * Base value (in milliseconds) for exponential backoff logic. e.g. for value 500 first retry
     * will happen randomly after 500-1000ms, next one after 1000-2000ms ...
     */
    val backoffBaseDelayMs: Int get() = 500

    /**
     * Enables debug logging in the underlying HTTP library.
     */
    val enableDebugLogging: Boolean

    /**
     * DNS record type to be used with DoH queries.
     */
    enum class DohRecordType {
        TXT, A
    }
    val dohRecordType get() = DohRecordType.TXT

    /**
     * Tells client to force update (this client will no longer be accepted by the API).
     *
     * @param errorMessage the localized error message the user should see.
     */
    fun forceUpdate(errorMessage: String)
}

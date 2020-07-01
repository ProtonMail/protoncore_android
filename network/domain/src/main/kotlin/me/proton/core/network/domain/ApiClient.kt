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
     * Timeout for internal api call attempt (due to error handling logic there might be internal
     * calls in a single API call by the client.
     */
    val timeoutSeconds: Long get() = 10L

    /**
     * Global timeout for DoH logic.
     */
    val dohTimeoutMs: Long get() = 60_000L

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
     * Tells client to force update (this client will no longer be accepted by the API).
     */
    fun forceUpdate()
}

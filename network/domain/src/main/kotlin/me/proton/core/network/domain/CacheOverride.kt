/*
 * Copyright (c) 2021 Proton Technologies AG
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
 * Used by `CacheOverrideInterceptor` to set a custom 'Cache-Control' header for a request.
 * See [RCF7234](https://datatracker.ietf.org/doc/html/rfc7234) for more info.
 */
class CacheOverride() {
    /** List of Cache-Control directives */
    val values = mutableListOf<String>()

    /** Forces a fresh fetch with no cache. */
    fun noCache() = this.also { values.add("no-cache") }
    /** Forbids the cache from storing any data related do this request and its response. */
    fun noStore() = this.also { values.add("no-store") }
    /** Indicates the client won't accept any a response older than the provided amount number of seconds. */
    fun maxAge(seconds: Int) = this.also { values.add("max-age=$seconds") }
    /** Indicates that even if the response is stale,
     * the client will accept it if it's only stale by that number of seconds. */
    fun maxStale(seconds: Int? = null) = this.also {
        values.add(seconds?.let { "max-stale=$seconds" } ?: "max-stale")
    }
    /** Indicates the client wants a response that will be fresh for at least the specified number of [seconds]. */
    fun minFresh(seconds: Int) = this.also { values.add("min-fresh=$seconds") }
    /** Indicates that no intermediary can modify the payload. */
    fun noTransform() = this.also { values.add("no-transform") }
    /** Forces the client to return a cached response or fail with 504 status code otherwise. */
    fun onlyIfCached() = this.also { values.add("only-if-cached") }
    /** Indicates that cache may be used if an error is encountered after becoming stale for [seconds] */
    fun staleIfError(seconds: Int) = this.also { values.add("stale-if-error=$seconds") }

    val controlHeaderValue: String get() = values.joinToString(", ")
}

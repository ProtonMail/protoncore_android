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

package me.proton.core.test.quark.v2

import kotlinx.serialization.json.Json
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URLEncoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

public object QuarkCommand {

    private var route: String? = null
    private var proxyToken: String? = null
    private var baseUrl: String? = null
    private var args: MutableList<String> = ArrayList()
    private var onRequestBuilder: Request.Builder.() -> Unit = {}
    private var httpClientTimeout: Duration = 15.seconds
    private var httpClientReadTimeout: Duration = 30.seconds
    private var httpClientWriteTimeout: Duration = 30.seconds

    private const val quarkCommandTag = "quark_command"

    public val json: Json = Json { ignoreUnknownKeys = true }

    public val client: OkHttpClient
        get() = OkHttpClient
            .Builder()
            .connectTimeout(httpClientTimeout.toJavaDuration())
            .readTimeout(httpClientReadTimeout.toJavaDuration())
            .writeTimeout(httpClientWriteTimeout.toJavaDuration())
            .build()

    public fun route(route: String): QuarkCommand = apply { this.route = route }

    public fun args(args: Array<String>): QuarkCommand = apply { this.args += args }

    public fun arg(arg: String): QuarkCommand = apply { this.args.add(arg) }

    public fun baseUrl(baseUrl: String): QuarkCommand = apply { this.baseUrl = baseUrl }

    public fun proxyToken(proxyToken: String): QuarkCommand = apply { this.proxyToken = proxyToken }

    public fun onRequestBuilder(requestBuilderBlock: Request.Builder.() -> Unit): QuarkCommand =
        apply { this.onRequestBuilder = requestBuilderBlock }

    public fun httpClientTimeout(
        clientTimeout: Duration,
        readTimeout: Duration = clientTimeout,
        writeTimeout: Duration = clientTimeout
    ): QuarkCommand = apply {
        this.httpClientTimeout = clientTimeout
        this.httpClientReadTimeout = readTimeout
        this.httpClientWriteTimeout = writeTimeout
    }

    public fun build(): Request = Request
        .Builder()
        .internalUrl(
            baseUrl = this.baseUrl ?: error("Internal api base url is not specified"),
            route = this.route ?: error("Internal api route is not specified"),
            args = this.args
        )
        .apply {
            onRequestBuilder.invoke(this)
            proxyToken?.let { header("x-atlas-secret", it) }
        }
        .build()

    public fun Request.execute(): Response = client.newCall(this).execute().apply {
        CoreLogger.i(quarkCommandTag, "Sent request to : ${request.url}; Response Code: $code")
        requireNotNull(body).string().apply {
            CoreLogger.d(quarkCommandTag, "Response body:\n$this")
            check(code <= 201) { this }
        }
    }

    private fun Request.Builder.internalUrl(
        baseUrl: String,
        route: String,
        args: MutableList<String>
    ): Request.Builder = args
        .filter { it.isNotEmpty() }
        .joinToString("&")
        .let {
            url("$baseUrl/$route?$it")
        }

    public object Route {
        /** Users **/
        public const val USERS_CREATE: String = "quark/raw::user:create"
        public const val USERS_CREATE_ADDRESS: String = "quark/user:create:address"
        public const val USERS_EXPIRE_SESSIONS: String = "quark/raw::user:expire:sessions"

        /** Payments **/
        public const val SEED_PAYMENT_METHOD: String = "quark/raw::payments:seed-payment-method"
        public const val SEED_SUBSCRIBER: String = "quark/raw::payments:seed-subscriber"

        /** Jails **/
        public const val JAIL_UNBAN: String = "quark/raw::jail:unban"

        /** Drive **/
        public const val DRIVE_POPULATE_USER_WITH_DATA: String = "quark/drive:populate"

        /** System **/
        public const val SYSTEM_ENV: String = "system/env"
    }

    public fun List<Pair<String, String>?>.toEncodedArgs(): Array<String> =
        filterNotNull().map { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }.toTypedArray()
}

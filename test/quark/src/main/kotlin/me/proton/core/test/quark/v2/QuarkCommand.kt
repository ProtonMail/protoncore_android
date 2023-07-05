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
import me.proton.core.util.kotlin.EMPTY_STRING
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URLEncoder
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * Represents a command for making HTTP requests with Quark internal api.
 */
public open class QuarkCommand(
    public val client: OkHttpClient = QuarkDefaultClient
) {
    /** Properties **/
    private var route: String? = null
    private var proxyToken: String? = null
    private var baseUrl: String? = null
    private var args: List<String> = ArrayList()
    private var onRequestBuilder: Request.Builder.() -> Unit = {}
    private var onResponse: OnQuarkResponse =
        OnQuarkResponse({ code > 300 }, { error("Quark response failed with status code: $code:\n$message") })

    /** JSON parser with configuration to ignore unknown keys **/
    public val json: Json = Json { ignoreUnknownKeys = true }

    /**
     * Sets the route for the request.
     * @param route the route as a string
     * @return the QuarkCommand instance for chaining
     */
    public fun route(route: String): QuarkCommand = apply { this.route = route }

    /**
     * Sets the arguments for the request.
     * @param args the array of strings to set as arguments
     * @return the QuarkCommand instance for chaining
     */
    public fun args(args: Array<String>): QuarkCommand = apply { this.args = args.toList() }

    /**
     * Sets the base URL for the request.
     * @param baseUrl the base URL as a string
     * @return the QuarkCommand instance for chaining
     */
    public fun baseUrl(baseUrl: String): QuarkCommand = apply {
        check(baseUrl.isNotEmpty())
        this.baseUrl = baseUrl
    }

    /**
     * Sets the proxy token for the request.
     * @param proxyToken the proxy token as a string
     * @return the QuarkCommand instance for chaining
     */
    public fun proxyToken(proxyToken: String): QuarkCommand = apply { this.proxyToken = proxyToken }

    /**
     * Customizes the request using a builder block.
     * @param requestBuilderBlock the block with custom request configuration
     * @return the QuarkCommand instance for chaining
     */
    public fun onRequestBuilder(requestBuilderBlock: Request.Builder.() -> Unit): QuarkCommand =
        apply { this.onRequestBuilder = requestBuilderBlock }

    /**
     * Configures the [QuarkCommand] to execute [handlerBlock] based on the [condition].
     *
     * @param handlerBlock Block to execute when [condition] is true.
     * @param condition Condition to check on the response.
     * @return The [QuarkCommand] instance with response handling set.
     */
    public fun onResponse(
        handlerBlock: Response.() -> Any,
        condition: Response.() -> Boolean,
    ): QuarkCommand =
        apply { this.onResponse = OnQuarkResponse(condition, handlerBlock) }

    /**
     * Builds the Request object to be sent.
     * @return the Request object
     * @throws IllegalStateException if the base URL or route is not specified
     */
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

    /**
     * Executes a Quark HTTP request using the given HTTP client.
     * @param request the Request to execute
     * @return the Response from the server
     */
    public fun OkHttpClient.executeQuarkRequest(request: Request): Response =
        newCall(request)
            .execute()
            .apply {
                CoreLogger.i(quarkCommandTag, "Sent request to : ${request.url}; Response Code: $code")
                onResponse.check(this)
            }

    /** build the internal URL with parameters **/
    private fun Request.Builder.internalUrl(
        baseUrl: String,
        route: String,
        args: List<String>
    ): Request.Builder = args
        .filter { it.isNotEmpty() }
        .joinToString("&")
        .let {
            url("$baseUrl/$route?$it")
        }

    public companion object {
        public const val quarkCommandTag: String = "quark_command"

        private val defaultTimeout = 10.seconds.toJavaDuration()

        public val QuarkDefaultClient: OkHttpClient by lazy {
            OkHttpClient
                .Builder()
                .connectTimeout(defaultTimeout)
                .readTimeout(defaultTimeout)
                .writeTimeout(defaultTimeout)
                .build()
        }
    }
}

/**
 * Converts a list of key-value pairs into an array of URL-encoded strings.
 * @param ignoreEmpty skips argument if value is empty
 * @return the array of URL-encoded strings.
 */
public fun List<Pair<String, String>?>.toEncodedArgs(ignoreEmpty: Boolean = true): Array<String> =
    filterNotNull()
        .map { (key, value) ->
            if (value.isEmpty() && ignoreEmpty)
                EMPTY_STRING
            else
                "$key=${URLEncoder.encode(value, "UTF-8")}"
        }.toTypedArray()

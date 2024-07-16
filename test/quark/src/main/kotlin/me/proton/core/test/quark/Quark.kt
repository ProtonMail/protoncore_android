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

package me.proton.core.test.quark

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.proton.core.domain.entity.AppStore
import me.proton.core.test.quark.Quark.InternalApiEndpoint.DRIVE_POPULATE_USER_WITH_DATA
import me.proton.core.test.quark.Quark.InternalApiEndpoint.EXPIRE_SESSION
import me.proton.core.test.quark.Quark.InternalApiEndpoint.JAIL_UNBAN
import me.proton.core.test.quark.Quark.InternalApiEndpoint.PAYMENTS_SEED_PAYMENT_METHOD
import me.proton.core.test.quark.Quark.InternalApiEndpoint.PAYMENTS_SEED_SUBSCRIBER
import me.proton.core.test.quark.Quark.InternalApiEndpoint.SYSTEM_ENV
import me.proton.core.test.quark.Quark.InternalApiEndpoint.USER_CREATE
import me.proton.core.test.quark.Quark.InternalApiEndpoint.USER_CREATE_ADDRESS
import me.proton.core.test.quark.Quark.InternalApiEndpoint.USER_RESET_PASSWORD
import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.data.randomPaidPlan
import me.proton.core.test.quark.response.CreateUserAddressQuarkResponse
import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.toInt
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

public class Quark constructor(
    private val host: String,
    private val proxyToken: String?,
    private val internalApi: InternalApi
) {
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    public data class InternalApi(
        val endpoints: LinkedHashMap<InternalApiEndpoint, String>,
        val constants: LinkedHashMap<String, String>
    )

    public enum class InternalApiEndpoint {
        SYSTEM_ENV,
        JAIL_UNBAN,
        USER_CREATE,
        USER_CREATE_ADDRESS,
        USER_RESET_PASSWORD,
        PAYMENTS_SEED_PAYMENT_METHOD,
        PAYMENTS_SEED_SUBSCRIBER,
        DRIVE_POPULATE_USER_WITH_DATA,
        EXPIRE_SESSION
    }

    private val client = OkHttpClient
        .Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    public val defaultVerificationCode: String = internalApi.constants["DEFAULT_VERIFICATION_CODE"]!!

    private fun executeRequest(request: Request): String {
        client.newCall(request).execute().use {
            println("Sent request to endpoint : ${request.url}; Response Code: ${it.code}")
            check(it.code <= 201) { "Response code error: ${it.body?.string()}" }
            return requireNotNull(it.body).string()
        }
    }

    private fun prepareRequest(endpoint: String, args: Array<String>): Request.Builder {
        val argString = args.filter { it.isNotEmpty() }.joinToString("&")
        val url = "https://${host}$endpoint?$argString"
        return Request.Builder().url(url).apply {
            proxyToken?.let<String, Unit> { header("x-atlas-secret", proxyToken) }
        }
    }

    private fun quarkRequest(
        endpoint: InternalApiEndpoint,
        args: Array<String> = emptyArray(),
        builder: Request.Builder.() -> Unit = {}
    ): String? {
        return internalApi.endpoints[endpoint]?.let {
            val requestBuilder = prepareRequest(it, args)
            builder.invoke(requestBuilder)
            executeRequest(requestBuilder.build())
        }
    }

    public fun systemEnv(variable: String, value: String) {
        val args = arrayOf("$variable=$value")
        quarkRequest(SYSTEM_ENV, args) { post("".toRequestBody()) }
    }

    public fun jailUnban() {
        quarkRequest(JAIL_UNBAN)
    }

    public fun seedNewSubscriber(user: User = User(plan = randomPaidPlan())): User {
        val args = arrayOf(
            "username=${user.name}",
            "password=${user.password}",
            "plan=${user.plan.planName}"
        )
        quarkRequest(PAYMENTS_SEED_SUBSCRIBER, args)
        return user
    }

    public fun seedNewSubscriberWithCycle(user: User = User(plan = randomPaidPlan()), cycleDurationMonths: Int): User {
        val args = arrayOf(
            "username=${user.name}",
            "password=${user.password}",
            "plan=${user.plan.planName}",
            "cycle=$cycleDurationMonths"
        )
        val response = quarkRequest(PAYMENTS_SEED_SUBSCRIBER, args)
        println("Quark response: $response")
        return user
    }

    public fun seedUserWithCreditCard(user: User = User()): User {
        val args = arrayOf(
            "-u=${user.name}",
            "-p=${user.password}",
            "-t=card"
        )
        quarkRequest(PAYMENTS_SEED_PAYMENT_METHOD, args)
        return user
    }

    public fun seedPaypalPaymentMethod(user: User) {
        val args = arrayOf(
            "-u=${user.name}",
            "-p=${user.password}",
            "-x=${internalApi.constants["PAYPAL_USERNAME"]!!}",
            "-z=${internalApi.constants["PAYPAL_PASSWORD"]!!}",
            "-t=paypal"
        )
        quarkRequest(PAYMENTS_SEED_PAYMENT_METHOD, args)
    }

    public fun userCreate(
        user: User = User(),
        createAddress: CreateAddress? = CreateAddress.WithKey(GenKeys.Curve25519)
    ): Pair<User, CreateUserQuarkResponse> {
        val args = arrayOf(
            if (user.isExternal) "-e=true" else "",
            if (user.isExternal) "--external-email=${URLEncoder.encode(user.email, "UTF-8")}" else "",
            if (user.name.isNotEmpty()) "-N=${user.name}" else "",
            if (user.password.isNotEmpty()) "-p=${user.password}" else "",
            if (user.passphrase.isNotEmpty()) "-m=${user.passphrase}" else "",
            if (user.recoveryEmail.isNotEmpty()) "-r=${user.recoveryEmail}" else "",
            if (createAddress is CreateAddress.NoKey) "-c=true" else "",
            if (createAddress is CreateAddress.WithKey) "-k=${createAddress.genKeys.name}" else "",
            "--format=json"
        )

        val responseJson = quarkRequest(USER_CREATE, args)
        println("Quark response: $responseJson")
        val response = json.decodeFromString<CreateUserQuarkResponse>(responseJson!!)
        return user to response
    }

    public fun userCreateAddress(
        decryptedUserId: Long,
        password: String,
        email: String,
        genKeys: GenKeys = GenKeys.Curve25519
    ): CreateUserAddressQuarkResponse {
        val args = listOf(
            "userID" to decryptedUserId.toString(),
            "password" to password,
            "email" to email,
            "--gen-keys" to genKeys.name,
            "--format" to "json"
        ).toEncodedArgs()
        val responseJson = quarkRequest(USER_CREATE_ADDRESS, args)
        println("Quark response: $responseJson")
        return json.decodeFromString(responseJson!!)
    }

    public fun populateUserWithData(user: User): User {
        val args = arrayOf(
            if (user.name.isNotEmpty()) "-u=${user.name}" else "",
            if (user.password.isNotEmpty()) "-p=${user.password}" else "",
            if (user.dataSetScenario.isNotEmpty()) "-S=${user.dataSetScenario}" else ""
        )
        quarkRequest(DRIVE_POPULATE_USER_WITH_DATA, args)
        return user
    }

    public fun expireSession(username: String, expireRefreshToken: Boolean = false) {
        val args = arrayOf(
            "User=$username",
            if (expireRefreshToken) "--refresh=null" else ""
        )
        quarkRequest(EXPIRE_SESSION, args)
    }

    /** WARNING:
     * Should be used only in [me.proton.core.test.android.uitests.tests.medium.plans]
     * or [me.proton.core.test.android.uitests.tests.medium.payments] tests,
     * to avoid overwriting this setting.
     */
    public fun setDefaultPaymentMethods(): Unit = setPaymentMethods(card = true, paypal = true, inApp = true)

    /** WARNING:
     * Should be used only in [me.proton.core.test.android.uitests.tests.medium.plans]
     * or [me.proton.core.test.android.uitests.tests.medium.payments] tests,
     * to avoid overwriting this setting.
     */
    public fun setPaymentMethods(
        appStore: AppStore = AppStore.GooglePlay,
        card: Boolean = true,
        paypal: Boolean = true,
        inApp: Boolean = true
    ) {
        val value: Int = (card.toInt() shl 0) + (paypal.toInt() shl 1) + (inApp.toInt() shl 2)
        val env = when (appStore) {
            AppStore.FDroid -> internalApi.constants["ENV_PAYMENT_STATUS_STORE_FDROID"]
            AppStore.GooglePlay -> internalApi.constants["ENV_PAYMENT_STATUS_STORE_GOOGLE"]
        }
        systemEnv(requireNotNull(env), "$value")
    }

    public fun resetPassword(
        userID: Long,
        newPassword: String,
        genKeys: GenKeys = GenKeys.Curve25519,
    ) {
        val args = arrayOf(
            "-k=${genKeys.name}",
            "userID=$userID",
            "newPassword=$newPassword"
        )
        val response = quarkRequest(USER_RESET_PASSWORD, args)
        println("Reset password: $response")
    }

    public sealed class CreateAddress {
        public object NoKey : CreateAddress()
        public data class WithKey(val genKeys: GenKeys = GenKeys.Curve25519) : CreateAddress()
    }

    public enum class GenKeys {
        None, RSA2048, RSA4096, Curve25519
    }

    private fun List<Pair<String, String>?>.toEncodedArgs(): Array<String> =
        filterNotNull().map { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }.toTypedArray()

    public companion object {
        public fun fromJson(
            json: String,
            host: String,
            proxyToken: String?
        ): Quark = Quark(
            host = host,
            proxyToken = proxyToken,
            json.deserialize()
        )

        public fun fromJavaResources(
            classLoader: ClassLoader,
            resourcePath: String,
            host: String,
            proxyToken: String?
        ): Quark = fromJson(
            json = classLoader
                .getResourceAsStream(resourcePath)
                .let { requireNotNull(it) { "Could not find resource file: $resourcePath" } }
                .bufferedReader()
                .use { it.readText() },
            host = host,
            proxyToken = proxyToken
        )

        /** Assumes `internal_apis.json` file exists in quark module `resources/sensitive` directory. */
        public fun fromDefaultResources(host: String, proxyToken: String?): Quark = fromJavaResources(
            Quark::class.java.classLoader,
            resourcePath = "sensitive/internal_apis.json",
            host = host,
            proxyToken = proxyToken
        )
    }
}

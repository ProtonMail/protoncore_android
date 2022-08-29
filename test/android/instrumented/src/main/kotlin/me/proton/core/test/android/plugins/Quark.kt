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

package me.proton.core.test.android.plugins

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.AppStore
import me.proton.core.test.android.instrumented.ProtonTest.Companion.testTag
import me.proton.core.test.android.plugins.Quark.InternalApiEndpoint.DRIVE_POPULATE_USER_WITH_DATA
import me.proton.core.test.android.plugins.Quark.InternalApiEndpoint.JAIL_UNBAN
import me.proton.core.test.android.plugins.Quark.InternalApiEndpoint.PAYMENTS_SEED_PAYMENT_METHOD
import me.proton.core.test.android.plugins.Quark.InternalApiEndpoint.PAYMENTS_SEED_SUBSCRIBER
import me.proton.core.test.android.plugins.Quark.InternalApiEndpoint.SYSTEM_ENV
import me.proton.core.test.android.plugins.Quark.InternalApiEndpoint.USER_CREATE
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.plugins.data.randomPaidPlan
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.toInt
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class Quark(private val host: String, private val proxyToken: String?, internalApiJsonPath: String) {

    @Serializable
    data class InternalApi(
        val endpoints: LinkedHashMap<InternalApiEndpoint, String>,
        val constants: LinkedHashMap<String, String>
    )

    enum class InternalApiEndpoint {
        SYSTEM_ENV,
        JAIL_UNBAN,
        USER_CREATE,
        PAYMENTS_SEED_PAYMENT_METHOD,
        PAYMENTS_SEED_SUBSCRIBER,
        DRIVE_POPULATE_USER_WITH_DATA
    }

    private val client = OkHttpClient
        .Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val internalApi: InternalApi = InstrumentationRegistry
        .getInstrumentation()
        .context
        .assets
        .open(internalApiJsonPath)
        .bufferedReader()
        .use { it.readText() }
        .deserialize()

    val defaultVerificationCode = internalApi.constants["DEFAULT_VERIFICATION_CODE"]!!

    private fun executeRequest(request: Request): String {
        client.newCall(request).execute().use {
            Log.d(testTag, "\nSent request to endpoint : ${request.url}; Response Code: ${it.code}")
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

    fun systemEnv(variable: String, value: String) {
        val args = arrayOf("$variable=$value")
        quarkRequest(SYSTEM_ENV, args) { post("".toRequestBody()) }
    }

    fun jailUnban() = quarkRequest(JAIL_UNBAN)

    fun seedSubscriber(user: User = User(plan = randomPaidPlan())): User {
        val args = arrayOf(
            "username=${user.name}",
            "password=${user.password}",
            "plan=${user.plan.planName}"
        )
        quarkRequest(PAYMENTS_SEED_SUBSCRIBER, args)
        return user
    }

    fun seedUserWithCreditCard(user: User = User()): User {
        val args = arrayOf(
            "-u=${user.name}",
            "-p=${user.password}",
            "-t=card"
        )
        quarkRequest(PAYMENTS_SEED_PAYMENT_METHOD, args)
        return user
    }

    fun seedPaypalPaymentMethod(user: User) {
        val args = arrayOf(
            "-u=${user.name}",
            "-p=${user.password}",
            "-x=${internalApi.constants["PAYPAL_USERNAME"]!!}",
            "-z=${internalApi.constants["PAYPAL_PASSWORD"]!!}",
            "-t=paypal"
        )
        quarkRequest(PAYMENTS_SEED_PAYMENT_METHOD, args)
    }

    fun userCreate(
        user: User = User(),
        createAddress: Boolean = true,
        keysEncryption: String = "Curve25519"
    ): User {
        val args = arrayOf(
            if (user.name.isNotEmpty()) "-N=${user.name}" else "",
            if (user.name.isNotEmpty()) "-p=${user.password}" else "",
            if (createAddress) "-c=true" else "",
            if (user.passphrase.isNotEmpty()) "-m=${user.passphrase}" else "",
            if (user.recoveryEmail.isNotEmpty()) "-r=${user.recoveryEmail}" else "",
            "-k=$keysEncryption"
        )

        quarkRequest(USER_CREATE, args)
        return user
    }

    fun populateUserWithData(
        user: User,
    ): User {
        val args = arrayOf(
            if (user.name.isNotEmpty()) "-u=${user.name}" else "",
            if (user.password.isNotEmpty()) "-p=${user.password}" else "",
            if (user.dataSetScenario.isNotEmpty()) "-S=${user.dataSetScenario}" else "",
        )
        quarkRequest(DRIVE_POPULATE_USER_WITH_DATA, args)
        return user
    }

    fun setDefaultPaymentMethods() = setPaymentMethods(card = true, paypal = true, inApp = true)

    fun setPaymentMethods(
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
}

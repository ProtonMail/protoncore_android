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
import me.proton.core.test.android.instrumented.ProtonTest.Companion.testTag
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.plugins.data.randomPaidPlan
import me.proton.core.util.kotlin.deserialize
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class Quark(private val host: String, private val proxyToken: String, internalApiJsonPath: String) {
    @Serializable
    data class InternalApi(
        val endpoints: LinkedHashMap<InternalApiEndpoint, String>,
        val constants: LinkedHashMap<String, String>
    )

    enum class InternalApiEndpoint {
        JAIL_UNBAN,
        USER_CREATE,
        PAYMENTS_SEED_PAYMENT_METHOD,
        PAYMENTS_SEED_SUBSCRIBER
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

    private fun quarkRequest(endpoint: String, args: Array<String> = emptyArray()): String {
        val argString = args.filter { it.isNotEmpty() }.joinToString("&")
        val url = "https://${host}$endpoint?$argString"
        val req = Request.Builder().url(url).header("x-atlas-secret", proxyToken).build()

        client.newCall(req).execute().use {
            Log.d(testTag, "\nSent request to endpoint : $endpoint; Response Code: ${it.code}")
            return it.body!!.string()
        }
    }

    fun jailUnban() = internalApi.endpoints[InternalApiEndpoint.JAIL_UNBAN]?.let { quarkRequest(it) }

    fun seedSubscriber(user: User = User(plan = randomPaidPlan())): User {
        val args = arrayOf(
            "username=${user.name}",
            "password=${user.password}",
            "plan=${user.plan.planName}"
        )
        internalApi.endpoints[InternalApiEndpoint.PAYMENTS_SEED_SUBSCRIBER]?.let {
            quarkRequest(it, args)
        }
        return user
    }

    fun seedUserWithCreditCard(user: User = User()): User {
        val args = arrayOf(
            "-u=${user.name}",
            "-p=${user.password}",
            "-t=card"
        )
        internalApi.endpoints[InternalApiEndpoint.PAYMENTS_SEED_PAYMENT_METHOD]?.let {
            quarkRequest(it, args)
        }
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
        internalApi.endpoints[InternalApiEndpoint.PAYMENTS_SEED_PAYMENT_METHOD]?.let {
            quarkRequest(it, args)
        }
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

        quarkRequest(internalApi.endpoints[InternalApiEndpoint.USER_CREATE]!!, args)
        return user
    }

    val defaultVerificationCode = internalApi.constants["DEFAULT_VERIFICATION_CODE"]!!
    val planDevName: String = internalApi.constants["PLAN_DEV_NAME"]!!
    val planDevText: String = internalApi.constants["PLAN_DEV_TEXT"]!!
}

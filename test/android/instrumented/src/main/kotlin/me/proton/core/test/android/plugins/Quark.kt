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
import me.proton.core.util.kotlin.deserialize
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object Quark {
    @Serializable
    data class InternalApi(
        val baseUrl: String,
        val endpoints: LinkedHashMap<InternalApiEndpoint, String>,
        val constants: LinkedHashMap<String, String>
    )

    enum class InternalApiEndpoint {
        JAIL_UNBAN,
        USER_CREATE,
        PAYMENTS_SEED_PAYMENT_METHOD,
        PAYMENTS_SEED_SUBSCRIBER
    }

    enum class EncryptionKeys {
        None,
        RSA2048,
        RSA4096,
        Curve25519
    }

    enum class PaymentMethodType(val value: String) {
        Card("card"),
        Card3DSecure("threeDSecureCard"),
        Paypal("paypal")
    }

    private val client = OkHttpClient
        .Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val internalApiJsonPath: String = "sensitive/internal_apis.json"

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
        val url = "https://${internalApi.baseUrl}$endpoint?$argString"
        val req = Request.Builder().url(url).build()

        client.newCall(req).execute().use {
            Log.d(testTag, "\nSent request to endpoint : $endpoint; Response Code: ${it.code}")
            return it.body!!.string()
        }
    }

    fun jailUnban() {
        internalApi.endpoints[InternalApiEndpoint.JAIL_UNBAN]?.let {
            quarkRequest(it)
        }
    }

    fun seedSubscriber(user: User): User {
        val args = arrayOf(
            "username=${user.name}",
            "password=${user.password}",
            "plan=${user.plan.value}"
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
            "-t=${PaymentMethodType.Card.value}"
        )
        internalApi.endpoints[InternalApiEndpoint.PAYMENTS_SEED_PAYMENT_METHOD]?.let {
            quarkRequest(it, args)
        }
        return user
    }

    fun seedPaypalPaymentMethod(user: User) {
        // Currently doesn't work due to bug in atlas
        val args = arrayOf(
            "-u=${user.name}",
            "-p=${user.password}",
            "-x=${Constants.PAYPAL_USERNAME.value}",
            "-z=${Constants.PAYPAL_PASSWORD.value}",
            "-t=${PaymentMethodType.Paypal.value}"
        )
        internalApi.endpoints[InternalApiEndpoint.PAYMENTS_SEED_PAYMENT_METHOD]?.let {
            quarkRequest(it, args)
        }
    }

    fun userCreate(
        user: User = User(),
        createAddress: Boolean = true,
        keysEncryption: EncryptionKeys = EncryptionKeys.Curve25519
    ): User {
        val args = arrayOf(
            if (user.name.isNotEmpty()) "-N=${user.name}" else "",
            if (user.name.isNotEmpty()) "-p=${user.password}" else "",
            if (createAddress) "-c=true" else "",
            if (keysEncryption != EncryptionKeys.None) "-k=${keysEncryption}" else "",
            if (user.passphrase.isNotEmpty()) "-m=${user.passphrase}" else ""
        )

        val responseString = quarkRequest((internalApi.endpoints[InternalApiEndpoint.USER_CREATE]!!), args)

        user.name = getQuarkOutputValueForKey("Name", responseString)
        user.password = getQuarkOutputValueForKey("Password", responseString)
        return user
    }

    private fun getQuarkOutputValueForKey(key: String, quarkOutput: String): String {
        return "$key: \\w+".toRegex().find(quarkOutput)!!.value.split(": ")[1]
    }

    enum class Constants(val value: String) {
        DEFAULT_VERIFICATION_CODE(internalApi.constants["DEFAULT_VERIFICATION_CODE"]!!),
        PAYPAL_USERNAME(internalApi.constants["PAYPAL_USERNAME"]!!),
        PAYPAL_PASSWORD(internalApi.constants["PAYPAL_PASSWORD"]!!)
    }
}

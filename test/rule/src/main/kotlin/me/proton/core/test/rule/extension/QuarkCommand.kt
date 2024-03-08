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

package me.proton.core.test.rule.extension

import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.command.USERS_CREATE
import me.proton.core.test.quark.v2.command.setPaymentMethods
import me.proton.core.test.quark.v2.toEncodedArgs
import me.proton.core.test.rule.annotation.TestPaymentMethods
import me.proton.core.test.rule.annotation.TestSubscriptionData
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.util.kotlin.EMPTY_STRING
import okhttp3.Response

public fun QuarkCommand.setPaymentMethods(methods: TestPaymentMethods): Response =
    setPaymentMethods(
        methods.appStore,
        methods.card,
        methods.paypal,
        methods.inApp
    )

public fun QuarkCommand.seedTestUserData(data: TestUserData): CreateUserQuarkResponse {
    val args = listOf(
        "-N" to data.name,
        "-p" to data.password,
        "-c" to data.createAddress.trueOrEmpty(),
        "-r" to data.recoveryEmail,
        "-s" to data.status.ordinal.toString(),
        "-a" to data.authVersion.toString(),
        "-c" to data.createAddress.trueOrEmpty(),
        "-k" to data.genKeys.valueOrEmpty(),
        "-m" to data.mailboxPassword,
        "-e" to data.external.trueOrEmpty(),
        "-ts" to data.toTpSecret,
        "-rp" to data.recoveryPhone,
        "-em" to data.externalEmail,
        "--format" to "json"
    ).toEncodedArgs(ignoreEmpty = true)

    return route(USERS_CREATE)
        .args(args)
        .build()
        .let {
            client.executeQuarkRequest(it)
        }
        .let {
            json.tryDecodeResponseBody(it)
        }
}

public fun QuarkCommand.subscriptionCreate(
    subscription: TestSubscriptionData,
    decryptedUserId: String
): Response =
    route("quark/raw::user:create:subscription")
        .args(
            listOf(
                "userID" to decryptedUserId,
                "--planID" to subscription.plan.planName,
                "--couponCode" to subscription.couponCode,
                "--cycle" to subscription.delinquent.trueOrEmpty(),
                "--format" to "json"
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

private fun Boolean.trueOrEmpty() = if (this) toString() else EMPTY_STRING

private fun TestUserData.GenKeys.valueOrEmpty() = if (this != TestUserData.GenKeys.None) toString() else EMPTY_STRING

private fun StringFormat.tryDecodeResponseBody(response: Response): CreateUserQuarkResponse {
    val responseString = response.body!!.string()
    return try {
        decodeFromString(responseString)
    } catch (ex: Exception) {
        error(responseString)
    }
}

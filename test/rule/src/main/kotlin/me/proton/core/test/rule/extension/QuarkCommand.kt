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
import me.proton.core.test.quark.v2.command.NEW_SEED_SUBSCRIBER
import me.proton.core.test.quark.v2.command.USERS_CREATE
import me.proton.core.test.quark.v2.command.seedSubscription
import me.proton.core.test.quark.v2.command.setPaymentMethods
import me.proton.core.test.quark.v2.toEncodedArgs
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.annotation.payments.TestPaymentMethods
import me.proton.core.test.rule.annotation.payments.TestSubscriptionData
import me.proton.core.util.kotlin.EMPTY_STRING
import okhttp3.Response

public fun QuarkCommand.setPaymentMethods(methods: TestPaymentMethods): Response =
    setPaymentMethods(
        methods.appStore,
        methods.card,
        methods.paypal,
        methods.inApp
    )

public fun QuarkCommand.seedTestUserData(userData: TestUserData): CreateUserQuarkResponse {
    val args = listOf(
        // If user is external we should provide an empty name otherwise it is not treated
        // as external even despite the userData.external value set to true.
        "-N" to (userData.name.takeIf { !userData.isExternal } ?: EMPTY_STRING),
        "-p" to userData.password,
        "-c" to userData.createAddress.trueOrEmpty(),
        "-r" to userData.recoveryEmail,
        "-s" to userData.status.ordinal.toString(),
        "-a" to userData.authVersion.toString(),
        "-c" to userData.createAddress.trueOrEmpty(),
        "-k" to userData.genKeys.valueOrEmpty(),
        "-m" to userData.passphrase,
        "--external" to userData.isExternal.trueOrEmpty(),
        "--vpn-settings" to userData.vpnSettings,
        "--creation-time" to userData.creationTime,
        "--totp-secret" to userData.twoFa,
        "--recovery-phone" to userData.recoveryPhone,
        "--external-email" to userData.externalEmail,
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
): Response = seedSubscription(
    userId = decryptedUserId,
    plan = subscription.plan.planName.takeIf { subscription.customPlan.isEmpty() } ?: subscription.customPlan,
    cycleDurationMonths = subscription.cycle,
    currency = subscription.currency,
    coupon = subscription.couponCode,
    delinquent = subscription.delinquent,
    isTrial = subscription.isTrial
)

public fun QuarkCommand.seedSubscriber(
    userData: PrepareUser,
    cycleDurationMonths: Int = 1
): CreateUserQuarkResponse =
    route(NEW_SEED_SUBSCRIBER)
        .args(
            listOf(
                "username" to userData.userData.name,
                "password" to userData.userData.password,
                "plan" to userData.subscriptionData.customPlan.ifEmpty {
                    userData.subscriptionData.plan.planName
                },
                "cycle" to cycleDurationMonths.toString(),
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }.let {
            json.tryDecodeResponseBody(it)
        }

private fun Boolean.trueOrEmpty() = if (this) toString() else EMPTY_STRING

private fun TestUserData.GenKeys.valueOrEmpty() =
    if (this != TestUserData.GenKeys.None) toString() else EMPTY_STRING

private fun StringFormat.tryDecodeResponseBody(response: Response): CreateUserQuarkResponse {
    val responseString = response.body!!.string()
    return try {
        decodeFromString(responseString)
    } catch (ex: Exception) {
        error(responseString)
    }
}

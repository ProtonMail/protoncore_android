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

package me.proton.core.test.quark.v2.command

import me.proton.core.domain.entity.AppStore
import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.data.randomPaidPlan
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.QuarkCommand.Route
import me.proton.core.util.kotlin.toInt
import okhttp3.Response


public fun QuarkCommand.seedNewSubscriber(user: User = User(plan = randomPaidPlan())): Response =
    route(Route.SEED_SUBSCRIBER)
        .args(
            arrayOf(
                "username=${user.name}",
                "password=${user.password}",
                "plan=${user.plan.planName}"
            )
        )
        .build()
        .execute()

public fun QuarkCommand.seedNewSubscriberWithCycle(
    user: User = User(plan = randomPaidPlan()),
    cycleDurationMonths: Int
): Response =
    route(Route.SEED_SUBSCRIBER)
        .args(
            arrayOf(
                "username=${user.name}",
                "password=${user.password}",
                "plan=${user.plan.planName}",
                "cycle=$cycleDurationMonths"
            )
        )
        .build()
        .execute()

public fun QuarkCommand.seedUserWithCreditCard(user: User = User()): Response =
    route(Route.SEED_PAYMENT_METHOD)
        .args(
            arrayOf(
                "-u=${user.name}",
                "-p=${user.password}",
                "-t=card"
            )
        )
        .build()
        .execute()

/** WARNING:
 * Should be used only in [me.proton.core.test.android.uitests.tests.medium.plans]
 * or [me.proton.core.test.android.uitests.tests.medium.payments] tests,
 * to avoid overwriting this setting.
 */
public fun QuarkCommand.setDefaultPaymentMethods(): Response =
    setPaymentMethods(card = true, paypal = true, inApp = true)

/** WARNING:
 * Should be used only in [me.proton.core.test.android.uitests.tests.medium.plans]
 * or [me.proton.core.test.android.uitests.tests.medium.payments] tests,
 * to avoid overwriting this setting.
 */
public fun QuarkCommand.setPaymentMethods(
    appStore: AppStore = AppStore.GooglePlay,
    card: Boolean = true,
    paypal: Boolean = true,
    inApp: Boolean = true
): Response {
    val value: Int = (card.toInt() shl 0) + (paypal.toInt() shl 1) + (inApp.toInt() shl 2)
    val env = when (appStore) {
        AppStore.FDroid -> "STATUS_ANDROID_FDROID"
        AppStore.GooglePlay -> "STATUS_ANDROID_GOOGLE"
    }
    return systemEnv(env, "$value")
}

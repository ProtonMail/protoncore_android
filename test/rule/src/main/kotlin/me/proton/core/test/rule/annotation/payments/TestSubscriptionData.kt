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

package me.proton.core.test.rule.annotation.payments

import me.proton.core.test.quark.data.Plan
import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.test.rule.annotation.AnnotationTestData
import me.proton.core.test.rule.extension.subscriptionCreate
import me.proton.core.test.rule.printInfo
import me.proton.core.util.kotlin.EMPTY_STRING

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class TestSubscriptionData(
    val plan: Plan = Plan.Free,
    val customPlan: String = "",
    val couponCode: String = EMPTY_STRING,
    val delinquent: Boolean = false,
)

public val TestSubscriptionData.annotationTestData: AnnotationTestData<TestSubscriptionData>
    get() = AnnotationTestData.forCreateUserQuarkResponse(
        default = this,
        implementation = { data: TestSubscriptionData, seededUser: CreateUserQuarkResponse? ->
            seededUser?.let {
                // Custom plan case is handled in subscriptionCreate() function.
                val response = subscriptionCreate(data, it.decryptedUserId.toString())
                if (response.code == 200) {
                    printInfo("Subscription seeding successful: { ${response.message} }")
                } else {
                    error(
                        "Could not create subscription. Seeding failed with status code: " +
                                "${response.code} - ${response.message}"
                    )
                }
            } ?: error("Could not create subscription. User is not seeded.")
        }
    )

public fun TestSubscriptionData.isDefault(): Boolean = this == TestSubscriptionData()

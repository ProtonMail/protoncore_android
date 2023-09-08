/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.data.api.response

import me.proton.core.domain.entity.AppStore
import me.proton.core.plan.domain.entity.DynamicPlanInstance
import me.proton.core.plan.domain.entity.DynamicPlanVendor
import me.proton.core.util.kotlin.deserialize
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicPlanInstanceResourceTest {
    @Test
    fun fromJsonToResource() {
        assertEquals(
            DynamicPlanInstanceResource(
                cycle = 1,
                description = "description",
                periodEnd = 100,
                price = emptyList(),
                vendors = mapOf(
                    "Google" to DynamicPlanVendorResource(
                        productId = "googlemail_plus_12_renewing",
                        customerId = "cus_google_fAx9TIdL63UmeYDmUo3l"
                    )
                )
            ),
            """
                {
                "ID": "123abc",
                "Cycle": 1,
                "Description": "description",
                "PeriodEnd": 100,
                "Price": [],
                "Vendors": { "Google": { "ProductID": "googlemail_plus_12_renewing", "CustomerID": "cus_google_fAx9TIdL63UmeYDmUo3l" } }
                }
            """.trimIndent().deserialize()
        )
    }

    @Test
    fun fromResourceToDomain() {
        assertEquals(
            DynamicPlanInstance(
                cycle = 1,
                description = "description",
                periodEnd = Instant.ofEpochSecond(100),
                price = emptyMap(),
                vendors = mapOf(
                    AppStore.GooglePlay to DynamicPlanVendor(
                        productId = "googlemail_plus_12_renewing",
                        customerId = "cus_google_fAx9TIdL63UmeYDmUo3l"
                    )
                )
            ),
            DynamicPlanInstanceResource(
                cycle = 1,
                description = "description",
                periodEnd = 100,
                price = emptyList(),
                vendors = mapOf(
                    "Google" to DynamicPlanVendorResource(
                        productId = "googlemail_plus_12_renewing",
                        customerId = "cus_google_fAx9TIdL63UmeYDmUo3l"
                    ),
                    "Apple" to DynamicPlanVendorResource(
                        productId = "applemail_plus_12_renewing",
                        customerId = null
                    )
                )
            ).toDynamicPlanInstance()
        )
    }
}

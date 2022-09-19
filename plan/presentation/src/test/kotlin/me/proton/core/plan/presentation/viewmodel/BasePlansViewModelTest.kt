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

package me.proton.core.plan.presentation.viewmodel

import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.presentation.entity.PaymentVendorDetails
import me.proton.core.plan.domain.entity.PlanDuration
import me.proton.core.plan.domain.entity.PlanVendorData
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanVendorDetails
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BasePlansViewModelTest {
    @Test
    fun `convert empty map to plan vendor details`() {
        assertTrue(emptyMap<AppStore, PlanVendorData>().toPlanVendorDetailsMap().isEmpty())
    }

    @Test
    fun `convert to plan vendor details`() {
        val input = mapOf(
            Pair(
                AppStore.GooglePlay,
                PlanVendorData(
                    customerId = "c-1",
                    names = mapOf(
                        PlanDuration(1) to "googlemail_mail2022_1_renewing",
                        PlanDuration(12) to "googlemail_mail2022_12_renewing",
                        PlanDuration(13) to "googlemail_mail2022_13_renewing"
                    )
                )
            ),
            Pair(
                AppStore.FDroid,
                PlanVendorData(
                    customerId = "c-2",
                    names = emptyMap()
                )
            )
        )

        val expected = mapOf(
            Pair(
                AppStore.GooglePlay,
                PlanVendorDetails(
                    customerId = "c-1",
                    names = mapOf(
                        PlanCycle.MONTHLY to "googlemail_mail2022_1_renewing",
                        PlanCycle.YEARLY to "googlemail_mail2022_12_renewing"
                    )
                )
            ),
            Pair(
                AppStore.FDroid,
                PlanVendorDetails(
                    customerId = "c-2",
                    names = mapOf()
                )
            )
        )

        assertEquals(expected, input.toPlanVendorDetailsMap())
    }

    @Test
    fun `filter empty map`() {
        assertTrue(mapOf<AppStore, PlanVendorDetails>().filterByCycle(PlanCycle.YEARLY).isEmpty())
    }

    @Test
    fun `filter by cycle`() {
        val input = mapOf(
            AppStore.GooglePlay to PlanVendorDetails(
                customerId = "c-1",
                names = mapOf(
                    PlanCycle.MONTHLY to "googlemail_mail2022_1_renewing",
                    PlanCycle.YEARLY to "googlemail_mail2022_12_renewing",
                    PlanCycle.TWO_YEARS to "googlemail_mail2022_24_renewing"
                )
            )
        )

        val expected = mapOf(
            AppStore.GooglePlay to PaymentVendorDetails(
                customerId = "c-1",
                vendorPlanName = "googlemail_mail2022_12_renewing"
            )
        )

        assertEquals(expected, input.filterByCycle(PlanCycle.YEARLY))
    }
}

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
import me.proton.core.plan.domain.entity.PlanVendorName
import me.proton.core.plan.presentation.entity.PlanCycle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BasePlansViewModelTest {
    @Test
    fun `group empty list`() {
        assertTrue(emptyList<PlanVendorName>().groupByVendorAndCycle().isEmpty())
    }

    @Test
    fun `group by vendor and cycle`() {
        val input = listOf(
            PlanVendorName("custommail_mail2022_12_renewing", 12, "custom"),
            PlanVendorName("googlemail_mail2022_1_renewing", 1, "google"),
            PlanVendorName("googlemail_mail2022_12_renewing", 12, "google"),
            PlanVendorName("googlemail_mail2022_13_renewing", 13, "google")
        )

        val expected = mapOf(
            AppStore.GooglePlay to mapOf(
                PlanCycle.MONTHLY to "googlemail_mail2022_1_renewing",
                PlanCycle.YEARLY to "googlemail_mail2022_12_renewing"
            )
        )

        assertEquals(expected, input.groupByVendorAndCycle())
    }

    @Test
    fun `filter empty map`() {
        assertTrue(mapOf<AppStore, Map<PlanCycle, String>>().filterByCycle(PlanCycle.YEARLY).isEmpty())
    }

    @Test
    fun `filter by cycle`() {
        val input = mapOf(
            AppStore.GooglePlay to mapOf(
                PlanCycle.MONTHLY to "googlemail_mail2022_1_renewing",
                PlanCycle.YEARLY to "googlemail_mail2022_12_renewing",
                PlanCycle.TWO_YEARS to "googlemail_mail2022_24_renewing"
            )
        )

        val expected = mapOf(
            AppStore.GooglePlay to "googlemail_mail2022_12_renewing"
        )

        assertEquals(expected, input.filterByCycle(PlanCycle.YEARLY))
    }
}

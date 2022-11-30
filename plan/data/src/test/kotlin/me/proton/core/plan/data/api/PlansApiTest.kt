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

package me.proton.core.plan.data.api

import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.plan.domain.entity.MASK_CALENDAR
import me.proton.core.plan.domain.entity.MASK_DRIVE
import me.proton.core.plan.domain.entity.MASK_MAIL
import me.proton.core.plan.domain.entity.MASK_VPN
import me.proton.core.plan.domain.entity.PlanDuration
import me.proton.core.plan.domain.entity.PlanVendorData
import me.proton.core.test.kotlin.BuildRetrofitApi
import me.proton.core.test.kotlin.enqueueFromResourceFile
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlansApiTest {
    private lateinit var tested: PlansApi
    private lateinit var webServer: MockWebServer

    @BeforeTest
    fun setUp() {
        webServer = MockWebServer()
        tested = BuildRetrofitApi(webServer.url("/"))
    }

    @AfterTest
    fun tearDown() {
        webServer.shutdown()
    }

    @Test
    fun `get plans with vendor plan names`() = runTest {
        // Given
        webServer.enqueueFromResourceFile("GET/payments/v4/plans.json", javaClass.classLoader)

        // When
        val response = tested.getPlans()
        val plans = response.plans.map { it.toPlan() }

        // Then
        assertEquals(3, plans.size)

        assertEquals("mail2022", plans[0].name)
        assertEquals("Mail Plus", plans[0].title)
        assertEquals(499, plans[0].pricing?.monthly)
        assertEquals(4788, plans[0].pricing?.yearly)
        assertEquals(8376, plans[0].pricing?.twoYearly)
        assertEquals(1, plans[0].cycle)
        assertEquals(true, plans[0].enabled)
        assertEquals(499, plans[0].amount)
        assertEquals(MASK_MAIL, plans[0].services)
        assertEquals(
            mapOf(
                AppStore.GooglePlay to PlanVendorData(
                    customerId = "cus_google_unCjt-CANFkU-RVD8s0y",
                    names = mapOf(
                        PlanDuration(12) to "googlemail_mail2022_12_renewing"
                    )
                )
            ),
            plans[0].vendors
        )

        assertEquals(MASK_MAIL or MASK_CALENDAR or MASK_DRIVE or MASK_VPN, plans[1].services)

        assertEquals(false, plans[2].enabled)
    }

    @Test
    fun `get plans without vendor plan names`() = runTest {
        // Given
        webServer.enqueueFromResourceFile("GET/payments/v4/plans-no-vendors.json", javaClass.classLoader)

        // When
        val response = tested.getPlans()
        val plans = response.plans.map { it.toPlan() }

        // Then
        assertEquals(1, plans.size)
        assertTrue(plans[0].vendors.isEmpty())
    }
}

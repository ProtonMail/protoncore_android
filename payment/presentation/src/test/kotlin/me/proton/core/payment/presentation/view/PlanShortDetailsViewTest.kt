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

package me.proton.core.payment.presentation.view

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.entity.PaymentVendorDetails
import me.proton.core.payment.presentation.entity.PlanShortDetails
import org.junit.Rule
import org.junit.Test

class PlanShortDetailsViewTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun `initial empty state`() {
        val view = makePlanView()
        paparazzi.snapshot(view)
    }

    @Test
    fun `set monthly plan`() {
        val planDetails = PlanShortDetails(
            name = "plan-paid",
            displayName = "Plan Paid",
            SubscriptionCycle.MONTHLY,
            amount = 10,
            services = 1,
            type = 1,
            vendors = mapOf(
                AppStore.GooglePlay to PaymentVendorDetails("cus-id", "vendor-paid-plan")
            )
        )
        val view = makePlanView()
        view.plan = planDetails
        paparazzi.snapshot(view)
    }

    @Test
    fun `set yearly plan`() {
        val planDetails = PlanShortDetails(
            name = "plan-paid",
            displayName = "Plan Paid",
            SubscriptionCycle.YEARLY,
            amount = 10,
            services = 1,
            type = 1,
            vendors = mapOf(
                AppStore.GooglePlay to PaymentVendorDetails("cus-id", "vendor-paid-plan")
            )
        )
        val view = makePlanView()
        view.plan = planDetails
        paparazzi.snapshot(view)
    }

    @Test
    fun `set two years plan`() {
        val planDetails = PlanShortDetails(
            name = "plan-paid",
            displayName = "Plan Paid",
            SubscriptionCycle.TWO_YEARS,
            amount = 10,
            services = 1,
            type = 1,
            vendors = mapOf(
                AppStore.GooglePlay to PaymentVendorDetails("cus-id", "vendor-paid-plan")
            )
        )
        val view = makePlanView()
        view.plan = planDetails
        paparazzi.snapshot(view)
    }

    @Test
    fun `set other plan`() {
        val planDetails = PlanShortDetails(
            name = "plan-paid",
            displayName = "Plan Paid",
            SubscriptionCycle.TWO_YEARS,
            amount = 10,
            services = 1,
            type = 1,
            vendors = mapOf(
                AppStore.GooglePlay to PaymentVendorDetails("cus-id", "vendor-paid-plan")
            )
        )
        val view = makePlanView()
        view.plan = planDetails
        paparazzi.snapshot(view)
    }

    private fun makePlanView(): PlanShortDetailsView {
        return PlanShortDetailsView(paparazzi.context)
    }
}

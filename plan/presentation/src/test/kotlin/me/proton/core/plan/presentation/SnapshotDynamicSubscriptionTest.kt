/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.plan.presentation

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.plan.presentation.view.DynamicEntitlementDescriptionView
import me.proton.core.plan.presentation.view.DynamicEntitlementProgressView
import me.proton.core.plan.presentation.view.DynamicPlanView
import org.junit.Rule
import org.junit.Test

class SnapshotDynamicSubscriptionTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme",
        showSystemUi = false
    )

    @Test
    fun dynamicPlanEntitlementDescriptionView() {
        val view = DynamicEntitlementDescriptionView(paparazzi.context)
        view.text = "1 of 1 user"
        view.icon = R.drawable.ic_proton_checkmark
        paparazzi.snapshot(view)
    }

    @Test
    fun dynamicPlanEntitlementStorageView() {
        val view = DynamicEntitlementProgressView(paparazzi.context)
        view.text = "50/100"
        view.progress = 50
        paparazzi.snapshot(view)
    }

    @Test
    fun dynamicPlanView() {
        val view = DynamicPlanView(paparazzi.context)
        view.title = "Proton Free"
        view.description = "Description, not too long, but still 2 lines should be possible."
        view.priceText = "CHF200"
        view.priceCycle = "For 1 year"
        view.pricePercentage = "-50%"
        view.promoPercentage = "-50%"
        view.promoTitle = "1 month super promo"
        view.renewalText = "Your plan will automatically renew on 4 Jun 1982."
        view.isCollapsable = false
        view.starred = true
        view.entitlements.addView(DynamicEntitlementProgressView(paparazzi.context).apply {
            text = "50 MB on 100 MB"
            progress = 50
        })
        view.entitlements.addView(DynamicEntitlementDescriptionView(paparazzi.context).apply {
            text = "100MB of free Storage"
            icon = R.drawable.ic_proton_storage
        })
        paparazzi.snapshot(view)
    }
}

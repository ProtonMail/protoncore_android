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

package me.proton.core.plan.test.robot

import me.proton.core.plan.presentation.R
import me.proton.test.fusion.Fusion.view

public object UnredeemedPurchaseRobot {
    private val cancelButton = view.withText(R.string.presentation_alert_cancel)
    private val redeemButton = view.withText(R.string.payments_giap_unredeemed_confirm)

    public fun clickCancel() {
        cancelButton.await { checkIsDisplayed() }
        cancelButton.click()
    }

    public fun clickRedeem() {
        redeemButton.await { checkIsDisplayed() }
        redeemButton.click()
    }
}

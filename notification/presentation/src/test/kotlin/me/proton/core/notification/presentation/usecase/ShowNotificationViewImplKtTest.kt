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

package me.proton.core.notification.presentation.usecase

import me.proton.core.domain.entity.Product
import me.proton.core.notification.presentation.R
import org.junit.Test
import kotlin.test.assertEquals

class ShowNotificationViewImplKtTest {
    @Test
    fun testSmallIconPerProduct() {
        var product = Product.Calendar
        var result = product.getSmallIconResId()
        assertEquals(R.drawable.ic_proton_brand_proton_calendar, result)

        product = Product.Drive
        result = product.getSmallIconResId()
        assertEquals(R.drawable.ic_proton_brand_proton_drive, result)

        product = Product.Mail
        result = product.getSmallIconResId()
        assertEquals(R.drawable.ic_proton_brand_proton_mail, result)

        product = Product.Vpn
        result = product.getSmallIconResId()
        assertEquals(R.drawable.ic_proton_brand_proton_vpn, result)

        product = Product.Pass
        result = product.getSmallIconResId()
        assertEquals(R.drawable.ic_proton_brand_proton_pass, result)
    }
}

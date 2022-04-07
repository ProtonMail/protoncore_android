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

package me.proton.core.challenge.domain

import me.proton.core.domain.entity.Product
import org.junit.Test
import kotlin.test.assertEquals

public class ChallengeUtilsKtTest {

    @Test
    public fun `test mail client`(): Unit {
        val product = Product.Mail
        assertEquals("mail-android-v4-challenge", product.framePrefix())
    }

    @Test
    public fun `test calendar client`(): Unit {
        val product = Product.Calendar
        assertEquals("calendar-android-v4-challenge", product.framePrefix())
    }

    @Test
    public fun `test drive client`(): Unit {
        val product = Product.Drive
        assertEquals("drive-android-v4-challenge", product.framePrefix())
    }

    @Test
    public fun `test vpn client`(): Unit {
        val product = Product.Vpn
        assertEquals("vpn-android-v4-challenge", product.framePrefix())
    }
}

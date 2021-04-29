/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.user.domain.entity

import org.junit.Test
import kotlin.test.assertEquals

class UserVerificationTokenTypeTest {
    @Test
    fun `from string returns correct type`() {
        val expected = UserVerificationTokenType.EMAIL
        val input = "email"

        val result = UserVerificationTokenType.fromString(input)
        assertEquals(expected, result)
    }

    @Test
    fun `from string unknown returns default`() {
        val expected = UserVerificationTokenType.SMS
        val input = "sometype"

        val result = UserVerificationTokenType.fromString(input)
        assertEquals(expected, result)
    }
}

/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.humanverification.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Dino Kadrikj.
 */
internal class TokenTypeTest {

    @Test
    fun `from string returns correct type`() {
        val expected = TokenType.EMAIL
        val input = "email"

        val result = TokenType.fromString(input)
        assertEquals(expected, result)
    }

    @Test
    fun `from string unknown returns default`() {
        val expected = TokenType.SMS
        val input = "sometype"

        val result = TokenType.fromString(input)
        assertEquals(expected, result)
    }
}

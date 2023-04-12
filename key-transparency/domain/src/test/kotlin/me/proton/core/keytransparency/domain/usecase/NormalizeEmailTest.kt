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

package me.proton.core.keytransparency.domain.usecase

import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

internal class NormalizeEmailTest {

    private lateinit var normalizeEmail: NormalizeEmail

    @Before
    fun setUp() {
        normalizeEmail = NormalizeEmail()
    }

    @Test
    fun normalizeRegularEmailTest() {
        // given
        val email = "normalize_this-test.proton@proton.black"
        val expected = "normalizethistestproton@proton.black"
        // when
        val actual = normalizeEmail(email)
        // then
        kotlin.test.assertEquals(expected, actual)
    }

    @Test
    fun normalizeCatchallTest() {
        // given
        val email = "normalize_this-test.proton@proton.black"
        val expected = "@proton.black"
        // when
        val actual = normalizeEmail(email, isCatchAll = true)
        // then
        kotlin.test.assertEquals(expected, actual)
    }

    @Test
    fun normalizeWrongEmailTest() {
        // given
        val email = "not an email"
        // when &then
        assertFailsWith<IllegalArgumentException> {
            normalizeEmail(email, isCatchAll = true)
        }
    }
}

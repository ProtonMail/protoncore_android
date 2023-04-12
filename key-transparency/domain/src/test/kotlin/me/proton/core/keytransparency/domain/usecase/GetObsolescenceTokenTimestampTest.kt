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

import me.proton.core.test.kotlin.assertEquals
import kotlin.test.Test

class GetObsolescenceTokenTimestampTest {

    private val getObsolescenceTokenTimestamp = GetObsolescenceTokenTimestamp()

    @Test
    fun `getTimestamp() compatibility test`() {
        // Given
        val expected = 1_650_373_779L
        val token = "00000000625eb493b9cf3fc036cd6e8a932f0bded2a089203ecba1a3"
        // When
        val actualTimestamp = getObsolescenceTokenTimestamp(token)
        // Then
        assertEquals(expected, actualTimestamp) { "Timestamp extracted is wrong" }
    }
}

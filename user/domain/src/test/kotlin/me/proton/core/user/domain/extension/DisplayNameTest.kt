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

package me.proton.core.user.domain.extension

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.user.domain.entity.DisplayName
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.displayNameSplit
import org.junit.Test
import kotlin.test.assertEquals

class DisplayNameTest {

    private val user = mockk<User> {
        every { displayName } returns "John Do"
    }

    private val userNoLast = mockk<User> {
        every { displayName } returns "John"
    }

    private val userNull = mockk<User> {
        every { displayName } returns null
    }

    @Test
    fun displayNameSplit() = runTest {
        assertEquals(
            expected = DisplayName(
                firstName = "John",
                lastName = "Do"
            ),
            actual = user.displayNameSplit
        )
        assertEquals(
            expected = DisplayName(
                firstName = "John",
                lastName = null
            ),
            actual = userNoLast.displayNameSplit
        )
        assertEquals(
            expected = null,
            actual = userNull.displayNameSplit
        )
    }
}

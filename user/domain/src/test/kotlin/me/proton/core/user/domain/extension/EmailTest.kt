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
import me.proton.core.user.domain.entity.Email
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.emailSplit
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EmailTest {

    private val user = mockk<User> {
        every { email } returns "name@domain.com"
    }

    private val userWrong = mockk<User> {
        every { email } returns "nameATdomain.com"
    }

    private val userAddress = mockk<User> {
        every { email } returns "name@domain.com"
    }

    private val userAddressWrong = mockk<User> {
        every { email } returns "@domain.com"
    }

    @Test
    fun emailSplit() = runTest {
        assertEquals(
            expected = Email(
                username = "name",
                domain = "domain.com",
                value = "name@domain.com"
            ),
            actual = user.emailSplit
        )

        assertEquals(
            expected = Email(
                username = "name",
                domain = "domain.com",
                value = "name@domain.com"
            ),
            actual = userAddress.emailSplit
        )
    }

    @Test
    fun emailSplitThrows() = runTest {
        assertFailsWith<IllegalArgumentException> { userWrong.emailSplit }
        assertFailsWith<IllegalArgumentException> { userAddressWrong.emailSplit }
    }
}

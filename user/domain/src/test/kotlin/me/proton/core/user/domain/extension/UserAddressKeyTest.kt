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
import me.proton.core.key.domain.entity.key.KeyFlags
import me.proton.core.user.domain.entity.UserAddressKey
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserAddressKeyTest {

    private val addressKey = mockk<UserAddressKey> {
        every { flags } returns KeyFlags.NotCompromised + KeyFlags.NotObsolete
    }

    private val addressKeyCompromised = mockk<UserAddressKey> {
        every { flags } returns KeyFlags.NotObsolete
    }

    private val addressKeyObsolete = mockk<UserAddressKey> {
        every { flags } returns KeyFlags.NotCompromised
    }

    @Test
    fun canVerify() = runTest {
        assertTrue(addressKey.canVerify())
        assertTrue(addressKeyObsolete.canVerify())
        assertFalse(addressKeyCompromised.canVerify())
    }

    @Test
    fun canEncrypt() = runTest {
        assertTrue(addressKey.canEncrypt())
        assertFalse(addressKeyObsolete.canEncrypt())
        assertTrue(addressKeyCompromised.canEncrypt())
    }
}

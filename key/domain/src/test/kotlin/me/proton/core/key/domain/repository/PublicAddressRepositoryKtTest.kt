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

package me.proton.core.key.domain.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.key.domain.entity.key.PublicAddress
import org.junit.Assert.*
import org.junit.Test

class PublicAddressRepositoryKtTest {

    private val repository = mockk<PublicAddressRepository>()
    private val testUserId = SessionUserId("test-user-id")
    private val testEmail = "test-email"

    @Test
    fun `get public address default source`() = runTest {
        coEvery { repository.getPublicAddress(testUserId, testEmail, any()) } returns PublicAddress(
            testEmail, 1, "mime", emptyList(), null, null
        )
        val result = repository.getPublicAddressOrNull(testUserId, testEmail)

        assertNotNull(result)
        assertEquals(testEmail, result!!.email)
        coVerify { repository.getPublicAddress(testUserId, testEmail, Source.RemoteNoCache) }
    }

    @Test
    fun `get public address pass source`() = runTest {
        coEvery { repository.getPublicAddress(testUserId, testEmail, any()) } returns PublicAddress(
            testEmail, 1, "mime", emptyList(), null, null
        )
        val result = repository.getPublicAddressOrNull(testUserId, testEmail, Source.LocalIfAvailable)

        assertNotNull(result)
        assertEquals(testEmail, result!!.email)
        coVerify { repository.getPublicAddress(testUserId, testEmail, Source.LocalIfAvailable) }
    }
}
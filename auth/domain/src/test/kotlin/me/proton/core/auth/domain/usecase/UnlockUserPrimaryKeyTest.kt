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

package me.proton.core.auth.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.runTestWithResultContext
import me.proton.core.user.domain.UserManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UnlockUserPrimaryKeyTest {
    @MockK
    private lateinit var keyStoreCrypto: KeyStoreCrypto

    @MockK
    private lateinit var userManager: UserManager

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun vpnSuccess_result() = runTestWithResultContext {
        // WHEN
        runTested(Product.Vpn)

        // THEN
        val result = assertSingleResult("unlockUserPrimaryKey")
        assertEquals(UserManager.UnlockResult.Success, result.getOrThrow())
    }

    @Test
    fun noKeysSuccess_result() = runTestWithResultContext {
        // GIVEN
        coEvery { userManager.getUser(any(), any()) } returns mockk {
            every { keys } returns emptyList()
        }

        // WHEN
        runTested()

        // THEN
        val result = assertSingleResult("unlockUserPrimaryKey")
        assertEquals(UserManager.UnlockResult.Success, result.getOrThrow())
    }

    @Test
    fun unlock_result() = runTestWithResultContext {
        // GIVEN
        every { keyStoreCrypto.decrypt(any<EncryptedString>()) } answers { firstArg() }
        coEvery { userManager.getUser(any(), any()) } returns mockk {
            every { keys } returns listOf(mockk())
        }
        coEvery { userManager.unlockWithPassword(any(), any()) } returns
                UserManager.UnlockResult.Error.NoPrimaryKey

        // WHEN
        runTested()

        // THEN
        val result = assertSingleResult("unlockUserPrimaryKey")
        assertEquals(UserManager.UnlockResult.Error.NoPrimaryKey, result.getOrThrow())
    }

    private suspend fun runTested(product: Product = Product.Mail) {
        val tested = UnlockUserPrimaryKey(userManager, keyStoreCrypto, product)
        tested(UserId("test_user_id"), "test_password")
    }
}

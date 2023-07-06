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

package me.proton.core.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressType
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import org.junit.Test
import kotlin.test.assertIs

class SetupAccountCheckTest {

    private val testUserId = UserId("test-user-id")

    private val userRepository: UserRepository = mockk()
    private val userAddressRepository: UserAddressRepository = mockk()

    @Test
    fun `return NoSetupNeeded for Username AccountType`() = runTest {
        userRepository.returnsNoKeys()
        userAddressRepository.returnsNoAddresses()

        val result = SetupAccountCheck(
            product = Product.Vpn,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = true,
            requiredAccountType = AccountType.Username,
            isTemporaryPassword = false
        )
        assertIs<SetupAccountCheck.Result.NoSetupNeeded>(result)
    }

    @Test
    fun `return ChangePasswordNeeded if isTemporaryPassword for Internal AccountType`() = runTest {
        userRepository.returnsWithKeys()
        userAddressRepository.returnsOriginalWithKeys()

        val result = SetupAccountCheck(
            product = Product.Mail,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = false,
            requiredAccountType = AccountType.Internal,
            isTemporaryPassword = true
        )
        assertIs<SetupAccountCheck.Result.ChangePasswordNeeded>(result)
    }

    @Test
    fun `return ChangePasswordNeeded if isTemporaryPassword for External AccountType`() = runTest {
        userRepository.returnsWithKeys()
        userAddressRepository.returnsOriginalWithKeys()

        val result = SetupAccountCheck(
            product = Product.Drive,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = false,
            requiredAccountType = AccountType.External,
            isTemporaryPassword = true
        )
        assertIs<SetupAccountCheck.Result.ChangePasswordNeeded>(result)
    }

    @Test
    fun `return NoSetupNeeded if isTemporaryPassword for Username AccountType`() = runTest {
        userRepository.returnsWithKeys()
        userAddressRepository.returnsNoAddresses()

        val result = SetupAccountCheck(
            product = Product.Vpn,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = false,
            requiredAccountType = AccountType.Username,
            isTemporaryPassword = true
        )
        assertIs<SetupAccountCheck.Result.NoSetupNeeded>(result)
    }

    @Test
    fun `return SetupPrimaryKeysNeeded if user has no keys for Internal AccountType`() = runTest {
        userRepository.returnsNoKeys()
        userAddressRepository.returnsNoAddresses()

        val result = SetupAccountCheck(
            product = Product.Mail,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = false,
            requiredAccountType = AccountType.Internal,
            isTemporaryPassword = false
        )
        assertIs<SetupAccountCheck.Result.SetupPrimaryKeysNeeded>(result)
    }

    @Test
    fun `return SetupPrimaryKeysNeeded if user has no keys for External AccountType`() = runTest {
        userRepository.returnsNoKeys()
        userAddressRepository.returnsNoAddresses()

        val result = SetupAccountCheck(
            product = Product.Drive,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = false,
            requiredAccountType = AccountType.External,
            isTemporaryPassword = false
        )
        assertIs<SetupAccountCheck.Result.SetupPrimaryKeysNeeded>(result)
    }

    @Test
    fun `return NoSetupNeeded for VPN only even if isTwoPassModeNeeded`() = runTest {
        userRepository.returnsWithKeys()
        userAddressRepository.returnsNoAddresses()

        val result = SetupAccountCheck(
            product = Product.Vpn,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = true,
            requiredAccountType = AccountType.External,
            isTemporaryPassword = false
        )
        assertIs<SetupAccountCheck.Result.NoSetupNeeded>(result)
    }

    @Test
    fun `return TwoPassModeNeeded for External AccountType if isTwoPassModeNeeded`() = runTest {
        userRepository.returnsWithKeys()
        userAddressRepository.returnsOriginalWithKeys()

        val result = SetupAccountCheck(
            product = Product.Drive,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = true,
            requiredAccountType = AccountType.External,
            isTemporaryPassword = false
        )
        assertIs<SetupAccountCheck.Result.TwoPassNeeded>(result)
    }

    @Test
    fun `return SetupExternalAddressKeysNeeded if user has no address keys for External AccountType`() = runTest {
        userRepository.returnsWithKeys()
        userAddressRepository.returnsExternalNoKeys()

        val result = SetupAccountCheck(
            product = Product.Drive,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = false,
            requiredAccountType = AccountType.External,
            isTemporaryPassword = false
        )
        assertIs<SetupAccountCheck.Result.SetupExternalAddressKeysNeeded>(result)
    }

    @Test
    fun `return SetupInternalAddressNeeded if user has no address for Internal AccountType`() = runTest {
        userRepository.returnsWithKeys()
        userAddressRepository.returnsNoAddresses()

        val result = SetupAccountCheck(
            product = Product.Mail,
            userRepository = userRepository,
            addressRepository = userAddressRepository,
        ).invoke(
            userId = testUserId,
            isTwoPassModeNeeded = false,
            requiredAccountType = AccountType.Internal,
            isTemporaryPassword = false
        )
        assertIs<SetupAccountCheck.Result.SetupInternalAddressNeeded>(result)
    }

    private fun UserRepository.returnsNoKeys() {
        coEvery { getUser(any(), any()) } returns mockk {
            every { userId } returns testUserId
            every { name } returns "username"
            every { keys } returns emptyList()
            every { private } returns true
        }
    }

    private fun UserRepository.returnsWithKeys() {
        coEvery { getUser(any(), any()) } returns mockk {
            every { userId } returns testUserId
            every { name } returns "username"
            every { keys } returns listOf(mockk())
            every { private } returns true
        }
    }

    private fun UserAddressRepository.returnsNoAddresses() {
        coEvery { getAddresses(any(), any()) } returns emptyList()
    }

    private fun UserAddressRepository.returnsOriginalWithKeys() {
        coEvery { getAddresses(any(), any()) } returns listOf(
            mockk {
                every { type } returns AddressType.Original
                every { enabled } returns true
                every { keys } returns listOf(mockk())
            }
        )
    }

    private fun UserAddressRepository.returnsExternalNoKeys() {
        coEvery { getAddresses(any(), any()) } returns listOf(
            mockk {
                every { type } returns AddressType.External
                every { enabled } returns true
                every { keys } returns emptyList()
            }
        )
    }
}

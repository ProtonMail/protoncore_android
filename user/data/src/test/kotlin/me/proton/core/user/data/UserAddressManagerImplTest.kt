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

package me.proton.core.user.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.AddressType
import me.proton.core.user.domain.entity.Role
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserAddressManagerImplTest {

    private val userIdOriginal = UserId("userIdOriginal")
    private val userIdExternal = UserId("userIdExternal")
    private val userPrimaryPrivateKey = mockk<PrivateKey> {
        every { key } returns "userPrivateKey"
        every { isPrimary } returns true
    }
    private val userPrimaryKey = mockk<UserKey> {
        every { keyId } returns KeyId("keyId")
        every { privateKey } returns userPrimaryPrivateKey
    }
    private val user = mockk<User> {
        every { userId } returns userIdOriginal
        every { keys } returns listOf(userPrimaryKey)
        every { role } returns Role.OrganizationAdmin
    }
    private val addressKey = mockk<UserAddressKey> {
        every { keyId } returns KeyId("keyId")
        every { token } returns "token"
        every { signature } returns "signature"
    }
    private val addressIdOriginal = AddressId("addressIdOriginal")
    private val addressOriginal = mockk<UserAddress> {
        every { type } returns AddressType.Original
        every { userId } returns userIdOriginal
        every { addressId } returns addressIdOriginal
        every { keys } returns listOf(addressKey)
    }
    private val addressIdExternal = AddressId("addressIdExternal")
    private val addressExternal = mockk<UserAddress> {
        every { type } returns AddressType.External
        every { userId } returns userIdExternal
        every { addressId } returns addressIdExternal
        every { keys } returns listOf(addressKey)
    }
    private val addressIdNew = AddressId("addressIdNew")
    private val addressCopy = mockk<UserAddress> {
        every { type } returns AddressType.Original
        every { addressId } returns addressIdNew
        every { keys } returns emptyList()
    }
    private val addressNew = mockk<UserAddress> {
        every { type } returns AddressType.Original
        every { addressId } returns addressIdNew
        every { keys } returns emptyList()
        every { this@mockk.copy(keys = any()) } returns addressCopy
    }

    private val userRepository: UserRepository = mockk(relaxed = true) {
        coEvery { this@mockk.getUser(any(), any()) } returns user
    }
    private val userAddressRepository: UserAddressRepository = mockk(relaxed = true) {
        coEvery { this@mockk.getAddresses(userIdOriginal, any()) } returns listOf(addressOriginal)
        coEvery { this@mockk.getAddresses(userIdExternal, any()) } returns listOf(addressExternal, addressNew)
        coEvery { this@mockk.createAddress(any(), any(), any()) } returns addressNew
    }
    private val privateKeyRepository: PrivateKeyRepository = mockk(relaxed = true)
    private val userAddressKeySecretProvider: UserAddressKeySecretProvider = mockk(relaxed = true)

    private lateinit var manager: UserAddressManagerImpl

    @BeforeTest
    fun setUp() {
        manager = UserAddressManagerImpl(
            userRepository = userRepository,
            userAddressRepository = userAddressRepository,
            privateKeyRepository = privateKeyRepository,
            userAddressKeySecretProvider = userAddressKeySecretProvider,
            generateSignedKeyList = mockk(relaxed = true),
            signedKeyListChangeListener = mockk(relaxed = true)
        )
    }

    @Test
    fun observeAddresses() = runTest {
        // When
        manager.observeAddresses(userIdOriginal, refresh = false)
        // Then
        coVerify { userAddressRepository.observeAddresses(userIdOriginal, refresh = false) }
    }

    @Test
    fun getAddresses() = runTest {
        // When
        manager.getAddresses(userIdOriginal, refresh = false)
        // Then
        coVerify { userAddressRepository.getAddresses(userIdOriginal, refresh = false) }
    }

    @Test
    fun getAddress() = runTest {
        // When
        manager.getAddress(userIdOriginal, addressIdOriginal, refresh = false)
        // Then
        coVerify { userAddressRepository.getAddress(userIdOriginal, addressIdOriginal, refresh = false) }
    }

    @Test
    fun setupInternalAddressOriginal() = runTest {
        // Given
        val spyManager = spyk(manager)
        val displayName = "displayName"
        val domain = "domain"
        // When
        spyManager.setupInternalAddress(userIdOriginal, displayName, domain)
        // Then
        coVerify { userAddressRepository.getAddresses(userIdOriginal, refresh = false) }
        coVerify { spyManager.createAddressKey(userIdOriginal, addressIdOriginal, isPrimary = true) }
        coVerify(exactly = 0) { userAddressRepository.createAddress(userIdOriginal, displayName, domain) }
    }

    @Test
    fun setupInternalAddressExternal() = runTest {
        // Given
        val spyManager = spyk(manager)
        val displayName = "displayName"
        val domain = "domain"
        // When
        spyManager.setupInternalAddress(userIdExternal, displayName, domain)
        // Then
        coVerify { userAddressRepository.getAddresses(userIdExternal, refresh = false) }
        coVerify { spyManager.createAddressKey(userIdExternal, addressIdNew, isPrimary = true) }
    }

    @Test
    fun createAddressKey() = runTest {
        // When
        manager.createAddressKey(userIdExternal, addressIdNew, isPrimary = true)
        // Then
        coVerify { userAddressRepository.getAddresses(userIdExternal, refresh = false) }
        coVerify { userAddressKeySecretProvider.generateUserAddressKey(any(), any(), any(), any()) }
        coVerify { privateKeyRepository.createAddressKey(userIdExternal, any()) }
    }

    @Test
    fun updateAddress() = runTest {
        // Given
        val displayName = "displayName"
        val signature = "signature"
        // When
        manager.updateAddress(userIdOriginal, addressIdOriginal, displayName, signature)
        // Then
        coVerify { userAddressRepository.updateAddress(userIdOriginal, addressIdOriginal, displayName, signature) }
    }

    @Test
    fun updateOrder() = runTest {
        // Given
        val list = listOf(addressIdOriginal, addressIdExternal)
        // When
        manager.updateOrder(userIdOriginal, list)
        // Then
        coVerify { userAddressRepository.updateOrder(userIdOriginal, list) }
    }
}

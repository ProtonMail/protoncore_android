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

package me.proton.core.user.data

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.extension.primary
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.user.data.usecase.GenerateSignedKeyList
import me.proton.core.user.domain.UserAddressManager
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.firstInternalOrNull
import me.proton.core.user.domain.extension.generateNewKeyFormat
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.user.domain.SignedKeyListChangeListener
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAddressManagerImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val privateKeyRepository: PrivateKeyRepository,
    private val userAddressKeySecretProvider: UserAddressKeySecretProvider,
    private val generateSignedKeyList: GenerateSignedKeyList,
    private val signedKeyListChangeListener: Optional<SignedKeyListChangeListener>
) : UserAddressManager {

    override fun observeAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<List<UserAddress>> = userAddressRepository.observeAddresses(sessionUserId, refresh = refresh)

    override fun getAddressesFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<List<UserAddress>>> = userAddressRepository.getAddressesFlow(sessionUserId, refresh = refresh)

    override suspend fun getAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): List<UserAddress> = userAddressRepository.getAddresses(sessionUserId, refresh = refresh)

    override suspend fun getAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        refresh: Boolean
    ): UserAddress? = userAddressRepository.getAddress(sessionUserId, addressId, refresh = refresh)

    override suspend fun setupInternalAddress(
        sessionUserId: SessionUserId,
        displayName: String,
        domain: String
    ): UserAddress {
        // Check if internal UserAddress already exist, and if needed create remotely.
        val userAddresses = userAddressRepository.getAddresses(sessionUserId)
        val userAddress = userAddresses.firstInternalOrNull() ?: createAddress(sessionUserId, displayName, domain)
        return createAddressKey(sessionUserId, userAddress.addressId, isPrimary = true)
    }

    private suspend fun createAddress(
        sessionUserId: SessionUserId,
        displayName: String,
        domain: String
    ) = userAddressRepository.createAddress(sessionUserId, displayName, domain)

    override suspend fun createAddressKey(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        isPrimary: Boolean
    ): UserAddress {
        // Get User to get Primary Private Key.
        val user = userRepository.getUser(sessionUserId)
        val userPrimaryKey = user.keys.primary()
        checkNotNull(userPrimaryKey) { "User Primary Key doesn't exist." }

        val userAddresses = userAddressRepository.getAddresses(sessionUserId)
        val userAddress = userAddresses.firstOrNull { it.addressId == addressId }
        check(userAddress != null) { "User Address id doesn't exist." }

        // Check if key already exist.
        if (userAddress.keys.isNotEmpty()) return userAddress

        // Generate new UserAddressKey from user PrivateKey (according old vs new format).
        val userAddressKey = userAddressKeySecretProvider.generateUserAddressKey(
            generateNewKeyFormat = userAddresses.generateNewKeyFormat(),
            userAddress = userAddress,
            userPrivateKey = userPrimaryKey.privateKey,
            isPrimary = isPrimary
        )
        val userAddressWithKeys = userAddress.copy(keys = userAddress.keys.plus(userAddressKey))

        if (signedKeyListChangeListener.isPresent) {
            // Key transparency needs to be checked before generating SKLs
            signedKeyListChangeListener.get().onSKLChangeRequested(user.userId, userAddressWithKeys)
        }
        val skl = generateSignedKeyList(userAddressWithKeys)

        // Create the new generated UserAddressKey, remotely.
        privateKeyRepository.createAddressKey(
            sessionUserId = sessionUserId,
            key = PrivateAddressKey(
                addressId = addressId.id,
                privateKey = userAddressKey.privateKey,
                token = userAddressKey.token,
                signature = userAddressKey.signature,
                signedKeyList = skl
            )
        )

        val address = checkNotNull(userAddressRepository.getAddress(sessionUserId, addressId, refresh = true))
        if (signedKeyListChangeListener.isPresent) {
            // Key transparency needs to be checked later for new keys
            signedKeyListChangeListener.get().onSKLChangeAccepted(user.userId, address, skl)
        }
        return address
    }

    override suspend fun updateAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        displayName: String?,
        signature: String?
    ): UserAddress = userAddressRepository.updateAddress(sessionUserId, addressId, displayName, signature)

    override suspend fun updateOrder(
        sessionUserId: SessionUserId,
        addressIds: List<AddressId>
    ): List<UserAddress> = userAddressRepository.updateOrder(sessionUserId, addressIds)
}

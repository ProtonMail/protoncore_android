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

package me.proton.core.user.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.updateIsActive
import me.proton.core.key.domain.useKeysAs
import me.proton.core.user.data.UserAddressKeySecretProvider
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.extension.toEntity
import me.proton.core.user.data.extension.toUserAddress
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRemoteDataSource
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("TooManyFunctions")
class UserAddressRepositoryImpl @Inject constructor(
    private val db: AddressDatabase,
    private val userRepository: UserRepository,
    private val userAddressRemoteDataSource: UserAddressRemoteDataSource,
    private val userAddressKeySecretProvider: UserAddressKeySecretProvider,
    private val context: CryptoContext,
    scopeProvider: CoroutineScopeProvider
) : UserAddressRepository, PassphraseRepository.OnPassphraseChangedListener {

    private val addressDao = db.addressDao()
    private val addressKeyDao = db.addressKeyDao()
    private val addressWithKeysDao = db.addressWithKeysDao()

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { userId: UserId ->
            userAddressRemoteDataSource.fetchAll(userId)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { userId: UserId ->
                observeAddressesLocal(userId).map { addresses ->
                    addresses.takeIf { it.isNotEmpty() }?.filterNot { it.isFetchedTag() }
                }
            },
            writer = { userId, addresses ->
                replaceAll(addresses.plus(userId.getFetchedTagAddress()))
            },
            delete = null, // Not used.
            deleteAll = null // Not used.
        )
    ).buildProtonStore(scopeProvider)

    init {
        userRepository.addOnPassphraseChangedListener(this)
    }

    @OptIn(ExperimentalStoreApi::class)
    private suspend fun invalidateMemCache(userId: UserId? = null): Unit =
        if (userId != null) store.clear(userId) else store.clear()

    private suspend fun List<UserAddressKey>.updateIsActive(userId: UserId): List<UserAddressKey> =
        userRepository.getUser(userId).useKeysAs(context) { userContext ->
            map { key ->
                val passphrase = userAddressKeySecretProvider.getPassphrase(userId, userContext, key)
                key.copy(privateKey = key.privateKey.updateIsActive(context, passphrase))
            }
        }

    private suspend fun getAddressesLocal(userId: UserId): List<UserAddress> =
        addressWithKeysDao.getByUserId(userId).map { it.toUserAddress() }

    private fun observeAddressesLocal(userId: UserId): Flow<List<UserAddress>> =
        addressWithKeysDao.observeByUserId(userId).mapLatest { list -> list.map { it.toUserAddress() } }

    private suspend fun insertOrUpdate(userId: UserId, addresses: List<UserAddress>) {
        // Update isActive and passphrase.
        val addressKeys = addresses.flatMap { it.keys }.updateIsActive(userId)
        // Insert in Database.
        addressDao.insertOrUpdate(*addresses.map { it.toEntity() }.toTypedArray())
        addresses.forEach { address -> addressKeyDao.deleteAllByAddressId(address.addressId) }
        addressKeyDao.insertOrUpdate(*addressKeys.map { it.toEntity() }.toTypedArray())
        invalidateMemCache(userId)
    }

    private suspend fun replaceAll(userId: UserId, addresses: List<UserAddress>) {
        val oldAddressIds = addressDao.getByUserId(userId).map { it.addressId }
        val newAddressIds = addresses.map { it.addressId }
        val deletedAddresses = oldAddressIds.filterNot { it in newAddressIds }
        addressDao.delete(deletedAddresses)
        insertOrUpdate(userId, addresses)
    }

    private suspend fun replaceAll(addresses: List<UserAddress>): Unit =
        db.inTransaction {
            addresses.groupBy { it.userId }.entries.forEach { (userId, addresses) ->
                replaceAll(userId, addresses)
            }
        }

    private suspend fun insertOrUpdate(addresses: List<UserAddress>): Unit =
        db.inTransaction {
            addresses.groupBy { it.userId }.entries.forEach { (userId, addresses) ->
                insertOrUpdate(userId, addresses)
            }
        }

    private suspend fun delete(addressIds: List<AddressId>) {
        addressDao.delete(addressIds)
        invalidateMemCache()
    }

    private suspend fun deleteAll(userId: UserId) {
        addressDao.deleteAll(userId)
        invalidateMemCache(userId)
    }

    override suspend fun onPassphraseChanged(userId: UserId) =
        db.inTransaction {
            insertOrUpdate(getAddressesLocal(userId))
        }

    override suspend fun addAddresses(addresses: List<UserAddress>) =
        insertOrUpdate(addresses)

    override suspend fun updateAddresses(addresses: List<UserAddress>) =
        insertOrUpdate(addresses)

    override suspend fun deleteAddresses(addressIds: List<AddressId>) =
        delete(addressIds)

    override suspend fun deleteAllAddresses(userId: UserId) =
        deleteAll(userId)

    override fun observeAddresses(sessionUserId: SessionUserId, refresh: Boolean): Flow<List<UserAddress>> =
        store.stream(StoreReadRequest.cached(sessionUserId, refresh = refresh))
            .map { it.dataOrNull().orEmpty() }
            .distinctUntilChanged()

    override fun observeAddress(sessionUserId: SessionUserId, addressId: AddressId, refresh: Boolean): Flow<UserAddress?> =
        observeAddresses(sessionUserId, refresh).map { list -> list.firstOrNull { it.addressId == addressId } }

    override suspend fun getAddresses(sessionUserId: SessionUserId, refresh: Boolean): List<UserAddress> =
        if (refresh) store.fresh(sessionUserId) else store.get(sessionUserId)

    override suspend fun getAddress(sessionUserId: SessionUserId, addressId: AddressId, refresh: Boolean): UserAddress? =
        getAddresses(sessionUserId, refresh).firstOrNull { it.addressId == addressId }

    override suspend fun createAddress(
        sessionUserId: SessionUserId,
        displayName: String,
        domain: String
    ): UserAddress {
        val address = userAddressRemoteDataSource.createAddress(sessionUserId, displayName, domain)
        insertOrUpdate(listOf(address))
        return checkNotNull(getAddress(sessionUserId, address.addressId))
    }

    override suspend fun updateAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        displayName: String?,
        signature: String?
    ): UserAddress {
        userAddressRemoteDataSource.updateAddress(sessionUserId, addressId, displayName, signature)
        return checkNotNull(getAddress(sessionUserId, addressId, refresh = true))
    }

    override suspend fun updateOrder(
        sessionUserId: SessionUserId,
        addressIds: List<AddressId>
    ): List<UserAddress> {
        userAddressRemoteDataSource.updateOrder(sessionUserId, addressIds)
        return getAddresses(sessionUserId, refresh = true)
    }

    companion object {
        internal fun UserAddress.isFetchedTag() = email == "fetched"
        // Fake Address tagging the repo the addresses have been fetched once.
        private fun UserId.getFetchedTagAddress() = UserAddress(
            userId = this,
            addressId = AddressId("fetched"),
            email = "fetched",
            displayName = null,
            signature = null,
            domainId = null,
            canSend = false,
            canReceive = false,
            enabled = false,
            type = null,
            order = 0,
            keys = emptyList(),
            signedKeyList = null
        )
    }
}

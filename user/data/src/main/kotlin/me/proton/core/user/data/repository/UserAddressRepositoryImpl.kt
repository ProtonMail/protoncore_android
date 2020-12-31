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

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.simple.encrypt
import me.proton.core.crypto.common.simple.use
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.decryptDataOrNull
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyData
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.data.api.AddressApi
import me.proton.core.user.data.api.request.SetupAddressRequest
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.AddressKeyEntity
import me.proton.core.user.data.extension.toEntity
import me.proton.core.user.data.extension.toEntityList
import me.proton.core.user.data.extension.toUserAddress
import me.proton.core.user.data.extension.toUserAddressKey
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.takeIfNotEmpty

class UserAddressRepositoryImpl(
    private val userRepository: UserRepository,
    private val passphraseRepository: PassphraseRepository,
    private val db: AddressDatabase,
    private val provider: ApiProvider,
    private val cryptoContext: CryptoContext
) : UserAddressRepository {

    private val addressDao = db.addressDao()
    private val addressKeyDao = db.addressKeyDao()
    private val addressWithKeysDao = db.addressWithKeysDao()

    private data class StoreKey(val userId: UserId, val addressId: AddressId? = null)

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: StoreKey ->
            val list = provider.get<AddressApi>(key.userId).invoke {
                if (key.addressId != null)
                    listOf(getAddress(key.addressId.id).address)
                else
                    getAddresses().addresses
            }.valueOrThrow
            list.map { getAddressLocal(it.toEntity(key.userId), it.keys?.toEntityList(AddressId(it.id)).orEmpty()) }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> getAddressesLocal(key.userId).map { it.takeIfNotEmpty() } },
            writer = { _, input -> insertOrUpdate(*input.toTypedArray()) },
            delete = { key -> delete(key.userId) },
            deleteAll = { deleteAll() }
        )
    ).build()

    private fun getAddressesLocal(userId: UserId): Flow<List<UserAddress>> =
        userRepository.getUser(userId)
            .flatMapLatest {
                // Resubscribe every time User flow emit a value (e.g. on [User.keys] locked/unlocked).
                addressWithKeysDao.findByUserId(userId.id)
                    .map { list -> list.map { getAddressLocal(it.entity, it.keys) } }
            }

    private suspend fun getAddressLocal(entity: AddressEntity, keys: List<AddressKeyEntity>): UserAddress {
        val keyList = keys.map { key -> getAddressKeyLocal(UserId(entity.userId), key) }
        return entity.toUserAddress(keyList)
    }

    private suspend fun getAddressKeyLocal(userId: UserId, key: AddressKeyEntity): UserAddressKey {
        return if (key.token == null || key.signature == null) {
            // Old address key format -> user passphrase.
            key.toUserAddressKey(passphrase = passphraseRepository.getPassphrase(userId))
        } else {
            // New address key format -> token + signature -> address passphrase.
            userRepository.getUserBlocking(userId).useKeys(cryptoContext) {
                val decryptedAddressPassphrase = decryptDataOrNull(key.token)?.takeIf { verifyData(it, key.signature) }
                val encryptedAddressPassphrase = decryptedAddressPassphrase?.use {
                    it.encrypt(cryptoContext.simpleCrypto)
                }
                key.toUserAddressKey(passphrase = encryptedAddressPassphrase)
            }
        }
    }

    private suspend fun insertOrUpdate(vararg addresses: UserAddress) =
        db.inTransaction {
            addresses.forEach { insertOrUpdate(it.toEntity(), it.keys.toEntityList()) }
        }

    private suspend fun insertOrUpdate(address: AddressEntity, addressKeys: List<AddressKeyEntity>) =
        db.inTransaction {
            addressDao.insertOrUpdate(address)
            addressKeyDao.insertOrUpdate(*addressKeys.toTypedArray())
        }

    private suspend fun delete(userId: UserId) =
        addressDao.delete(userId.id)

    private suspend fun deleteAll() =
        addressDao.deleteAll()

    private suspend fun getAddressListBlocking(
        sessionUserId: SessionUserId,
        addressId: AddressId?,
        refresh: Boolean
    ): List<UserAddress> = StoreKey(sessionUserId, addressId).let { if (refresh) store.fresh(it) else store.get(it) }

    override fun getAddresses(sessionUserId: SessionUserId, refresh: Boolean): Flow<DataResult<List<UserAddress>>> =
        store.stream(StoreRequest.cached(StoreKey(sessionUserId), refresh = refresh)).map { it.toDataResult() }

    override suspend fun getAddressesBlocking(sessionUserId: SessionUserId, refresh: Boolean): List<UserAddress> =
        getAddressListBlocking(sessionUserId, null, refresh)

    override suspend fun getAddressBlocking(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        refresh: Boolean
    ): UserAddress? = getAddressListBlocking(sessionUserId, addressId, refresh).firstOrNull()

    override suspend fun setupAddress(
        sessionUserId: SessionUserId,
        displayName: String,
        domain: String
    ): UserAddress {
        return provider.get<AddressApi>(sessionUserId).invoke {
            val response = setupAddress(SetupAddressRequest(displayName = displayName, domain = domain))
            val address = response.address.toEntity(sessionUserId)
            val addressId = AddressId(address.addressId)
            val addressKeys = response.address.keys?.toEntityList(addressId).orEmpty()
            insertOrUpdate(address, addressKeys)
            checkNotNull(getAddressBlocking(sessionUserId, addressId))
        }.valueOrThrow
    }
}

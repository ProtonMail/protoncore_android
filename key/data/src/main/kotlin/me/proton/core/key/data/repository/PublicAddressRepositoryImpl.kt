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

package me.proton.core.key.data.repository

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.KeyApi
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.key.data.extension.toEntity
import me.proton.core.key.data.extension.toEntityList
import me.proton.core.key.data.extension.toPublicAddress
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.CacheOverride
import javax.inject.Inject

class PublicAddressRepositoryImpl @Inject constructor(
    private val db: PublicAddressDatabase,
    private val provider: ApiProvider
) : PublicAddressRepository {

    private val publicAddressDao = db.publicAddressDao()
    private val publicAddressKeyDao = db.publicAddressKeyDao()
    private val publicAddressWithKeysDao = db.publicAddressWithKeysDao()

    private data class StoreKey(val userId: UserId, val email: String, val forceNoCache: Boolean)

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: StoreKey ->
            provider.get<KeyApi>(key.userId).invoke {
                getPublicAddressKeys(
                    key.email,
                    if (key.forceNoCache) CacheOverride().noCache() else null
                ).toPublicAddress(key.email)
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> getPublicAddressLocal(key.email) },
            writer = { _, input -> insertOrUpdate(input) },
            delete = { key -> delete(key.email) },
            deleteAll = { deleteAll() }
        )
    ).buildProtonStore()

    private fun getPublicAddressLocal(email: String): Flow<PublicAddress?> =
        publicAddressWithKeysDao.findWithKeysByEmail(email)
            .map { it?.entity?.toPublicAddress(it.keys) }

    private suspend fun insertOrUpdate(publicAddress: PublicAddress) =
        db.inTransaction {
            publicAddressDao.insertOrUpdate(publicAddress.toEntity())
            publicAddressKeyDao.insertOrUpdate(*publicAddress.keys.toEntityList().toTypedArray())
        }

    private suspend fun delete(email: String) = publicAddressDao.deleteByEmail(email)

    private suspend fun deleteAll() = publicAddressDao.deleteAll()

    override suspend fun getPublicAddress(
        sessionUserId: SessionUserId,
        email: String,
        source: Source
    ): PublicAddress = StoreKey(sessionUserId, email, source == Source.RemoteNoCache)
        .let { if (source == Source.LocalIfAvailable) store.get(it) else store.fresh(it) }

    override suspend fun clearAll() = store.clearAll()
}

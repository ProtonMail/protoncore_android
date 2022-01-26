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
import kotlinx.coroutines.flow.map
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.KeyApi
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.extension.toPrivateKeySaltList
import me.proton.core.key.domain.entity.key.PrivateKeySalt
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.util.kotlin.takeIfNotEmpty

class KeySaltRepositoryImpl(
    db: KeySaltDatabase,
    private val provider: ApiProvider
) : KeySaltRepository {

    private val keySaltDao = db.keySaltDao()

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { userId: UserId ->
            provider.get<KeyApi>(userId).invoke {
                getSalts().toKeySaltEntityList(userId)
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { userId -> keySaltDao.findAllByUserId(userId).map { it.takeIfNotEmpty() } },
            writer = { _, input -> keySaltDao.insertOrUpdate(*input.toTypedArray()) },
            delete = { userId -> keySaltDao.deleteByUserId(userId) },
            deleteAll = { keySaltDao.deleteAll() }
        )
    ).buildProtonStore()

    override suspend fun getKeySalts(sessionUserId: SessionUserId, refresh: Boolean): List<PrivateKeySalt> =
        (if (refresh) store.fresh(sessionUserId) else store.get(sessionUserId)).toPrivateKeySaltList()

    override suspend fun clear(userId: UserId) = store.clear(userId)

    override suspend fun clearAll() = store.clearAll()
}

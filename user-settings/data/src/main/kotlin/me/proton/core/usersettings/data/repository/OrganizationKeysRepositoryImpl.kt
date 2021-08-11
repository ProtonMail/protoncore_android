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

package me.proton.core.usersettings.data.repository

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.usersettings.data.api.OrganizationApi
import me.proton.core.usersettings.data.db.OrganizationKeysDatabase
import me.proton.core.usersettings.data.extension.fromEntity
import me.proton.core.usersettings.data.extension.fromResponse
import me.proton.core.usersettings.data.extension.toEntity
import me.proton.core.usersettings.domain.entity.OrganizationKeys
import me.proton.core.usersettings.domain.repository.OrganizationKeysRepository

class OrganizationKeysRepositoryImpl(
    db: OrganizationKeysDatabase,
    private val apiProvider: ApiProvider
) : OrganizationKeysRepository {

    private val organizationKeysDao = db.organizationKeysDao()

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: UserId ->
            apiProvider.get<OrganizationApi>(key).invoke {
                getOrganizationKeys().fromResponse(key)
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> observeByUserId(key) },
            writer = { _, input -> insertOrUpdate(input) },
            delete = { key -> delete(key) },
            deleteAll = { deleteAll() }
        )
    ).disableCache().build() // We don't want potential stale data from memory cache

    private fun observeByUserId(userId: UserId): Flow<OrganizationKeys?> =
        organizationKeysDao.observeByUserId(userId).map { it?.fromEntity() }

    private suspend fun insertOrUpdate(organizationKeys: OrganizationKeys) =
        organizationKeysDao.insertOrUpdate(organizationKeys.toEntity())

    private suspend fun delete(userId: UserId) =
        organizationKeysDao.delete(userId)

    private suspend fun deleteAll() =
        organizationKeysDao.deleteAll()

    override suspend fun getOrganizationKeysFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<OrganizationKeys>> {
        return store.stream(StoreRequest.cached(sessionUserId, refresh = refresh)).map { it.toDataResult() }
    }

    override suspend fun getOrganizationKeys(sessionUserId: SessionUserId, refresh: Boolean) =
        if (refresh) store.fresh(sessionUserId) else store.get(sessionUserId)

}
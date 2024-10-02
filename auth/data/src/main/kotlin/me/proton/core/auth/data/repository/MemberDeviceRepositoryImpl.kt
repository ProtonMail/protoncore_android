/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.data.repository

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.auth.domain.entity.MemberDevice
import me.proton.core.auth.domain.repository.MemberDeviceLocalDataSource
import me.proton.core.auth.domain.repository.MemberDeviceRemoteDataSource
import me.proton.core.auth.domain.repository.MemberDeviceRepository
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

class MemberDeviceRepositoryImpl @Inject constructor(
    private val localDataSource: MemberDeviceLocalDataSource,
    private val remoteDataSource: MemberDeviceRemoteDataSource,
    scopeProvider: CoroutineScopeProvider,
) : MemberDeviceRepository {

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: UserId -> remoteDataSource.getPendingMemberDevices(key) },
        sourceOfTruth = SourceOfTruth.Companion.of(
            reader = { key -> localDataSource.observeByUserId(key) },
            writer = { _, input -> localDataSource.upsert(input) },
            delete = { key -> localDataSource.deleteAll(key) },
            deleteAll = { localDataSource.deleteAll() }
        )
    ).disableCache().buildProtonStore(scopeProvider)

    override suspend fun getByMemberId(userId: UserId, memberId: UserId, refresh: Boolean): List<MemberDevice> =
        (if (refresh) store.fresh(userId) else store.get(userId)).filter { it.memberId == memberId }

    override suspend fun getByUserId(userId: UserId, refresh: Boolean): List<MemberDevice> =
        if (refresh) store.fresh(userId) else store.get(userId)

    override fun observeByMemberId(userId: UserId, memberId: UserId, refresh: Boolean): Flow<List<MemberDevice>> =
        store.stream(StoreRequest.cached(userId, refresh))
            .map { it.dataOrNull().orEmpty() }
            .map { memberDevices -> memberDevices.filter { it.memberId == memberId } }

    override fun observeByUserId(userId: UserId, refresh: Boolean): Flow<List<MemberDevice>> =
        store.stream(StoreRequest.cached(userId, refresh))
            .map { it.dataOrNull().orEmpty() }
}

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

package me.proton.core.push.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.UserId
import me.proton.core.push.data.remote.worker.DeletePushWorker
import me.proton.core.push.data.remote.worker.FetchPushesWorker
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.local.PushLocalDataSource
import me.proton.core.push.domain.remote.PushRemoteDataSource
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class PushRepositoryImpl @Inject constructor(
    private val remoteDataSource: PushRemoteDataSource,
    private val localDataSource: PushLocalDataSource,
    private val workManager: WorkManager,
    scopeProvider: CoroutineScopeProvider
) : PushRepository {
    private data class StoreKey(val userId: UserId, val pushType: PushObjectType)

    private val pushStore: Store<StoreKey, List<Push>> = StoreBuilder.from(
        fetcher = Fetcher.of { key: StoreKey ->
            remoteDataSource.getAllPushes(key.userId)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: StoreKey ->
                localDataSource.observeAllPushes(key.userId, key.pushType).map { pushes ->
                    pushes.takeIf { it.isNotEmpty() }?.minus(key.getFetchedTagPush())
                }
            },
            writer = { key, pushes: List<Push> ->
                localDataSource.mergePushes(key.userId, *pushes.plus(key.getFetchedTagPush()).toTypedArray())
            }
        ),
    )
        // Cache is disabled, because we have to allow to delete by PushId,
        // but our StoreKey doesn't contain a PushId.
        .disableCache()
        .buildProtonStore(scopeProvider)

    override suspend fun deletePush(userId: UserId, pushId: PushId) {
        localDataSource.getPush(userId, pushId)?.let {
            localDataSource.deletePushesById(userId, pushId)
            workManager.enqueue(DeletePushWorker.makeWorkerRequest(userId, pushId, it.type))
        }
    }

    override suspend fun getAllPushes(userId: UserId, type: PushObjectType, refresh: Boolean): List<Push> {
        val storeKey = StoreKey(userId, type)
        return if (refresh) pushStore.fresh(storeKey) else pushStore.get(storeKey)
    }

    override fun observeAllPushes(userId: UserId, type: PushObjectType, refresh: Boolean): Flow<List<Push>> {
        val storeKey = StoreKey(userId, type)
        return pushStore.stream(StoreRequest.cached(storeKey, refresh)).mapNotNull { it.dataOrNull() }
    }

    override fun markAsStale(userId: UserId, type: PushObjectType) {
        // Replace any existing FetchPushesWorker.
        workManager.enqueueUniqueWork(
            getUniqueWorkName(userId, type),
            ExistingWorkPolicy.REPLACE,
            FetchPushesWorker.getRequest(userId, type),
        )
    }

    private companion object {
        // Currently for FetchPushesWorker (ExistingWorkPolicy.REPLACE).
        private fun getUniqueWorkName(userId: UserId, type: PushObjectType) =
            "${PushRepositoryImpl::class.simpleName}-$userId-$type"

        private fun StoreKey.getFetchedTagPush() = Push(
            userId = userId,
            pushId = PushId("fetched-${pushType.value}"),
            objectId = "",
            type = pushType.value
        )
    }
}

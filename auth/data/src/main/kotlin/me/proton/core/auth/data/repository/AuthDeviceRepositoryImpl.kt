/*
 * Copyright (c) 2024 Proton AG
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

import androidx.work.WorkManager
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.DeviceTokenString
import me.proton.core.auth.domain.entity.InitDeviceStatus
import me.proton.core.auth.domain.repository.AuthDeviceLocalDataSource
import me.proton.core.auth.domain.repository.AuthDeviceRemoteDataSource
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

class AuthDeviceRepositoryImpl @Inject constructor(
    private val localDataSource: AuthDeviceLocalDataSource,
    private val remoteDataSource: AuthDeviceRemoteDataSource,
    scopeProvider: CoroutineScopeProvider,
    private val workManager: WorkManager,
) : AuthDeviceRepository {

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: UserId ->
            remoteDataSource.getAuthDevices(key)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> localDataSource.observeByUserId(key) },
            writer = { _, input -> localDataSource.upsert(input) },
            delete = { key -> localDataSource.deleteAll(key) },
            deleteAll = { localDataSource.deleteAll() }
        )
    ).buildProtonStore(scopeProvider)

    override suspend fun associateDeviceWithSession(
        sessionId: SessionId,
        deviceId: AuthDeviceId,
        deviceToken: DeviceTokenString
    ): String = remoteDataSource.associateDeviceWithSession(sessionId, deviceId, deviceToken)

    override fun observeByUserId(userId: UserId, refresh: Boolean): Flow<List<AuthDevice>> =
        store.stream(StoreRequest.cached(userId, refresh = refresh))
            .map { it.dataOrNull().orEmpty() }
            .distinctUntilChanged()

    override suspend fun getByUserId(sessionUserId: SessionUserId, refresh: Boolean): List<AuthDevice> {
        return (if (refresh) store.fresh(sessionUserId) else store.get(sessionUserId))
    }

    override suspend fun getByAddressId(sessionUserId: SessionUserId, addressId: AddressId, refresh: Boolean): List<AuthDevice> {
        return getByUserId(sessionUserId).filter { it.addressId == addressId }
    }

    override suspend fun deleteById(deviceId: AuthDeviceId, userId: UserId) {
        localDataSource.deleteByDeviceId(deviceId)
        workManager.enqueue(DeleteAuthDeviceWorker.makeWorkerRequest(deviceId, userId))
    }

    override suspend fun initDevice(
        sessionUserId: SessionUserId,
        name: String,
        activationToken: String
    ): InitDeviceStatus {
        return remoteDataSource.initDevice(sessionUserId, name, activationToken)
    }
}

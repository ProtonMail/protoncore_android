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
import me.proton.core.auth.domain.entity.CreatedDevice
import me.proton.core.auth.domain.entity.UnprivatizationInfo
import me.proton.core.auth.domain.repository.AuthDeviceLocalDataSource
import me.proton.core.auth.domain.repository.AuthDeviceRemoteDataSource
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.crypto.common.aead.AeadEncryptedString
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.UserId
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
    ).disableCache().buildProtonStore(scopeProvider)

    override suspend fun createDevice(
        userId: UserId,
        deviceName: String,
        activationToken: String?
    ): CreatedDevice = try {
        remoteDataSource.createDevice(userId, deviceName, activationToken)
    } finally {
        localDataSource.upsert(remoteDataSource.getAuthDevices(userId))
    }

    override suspend fun associateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        deviceToken: String
    ): String = try {
        remoteDataSource.associateDevice(userId, deviceId, deviceToken)
    } finally {
        localDataSource.upsert(remoteDataSource.getAuthDevices(userId))
    }

    override suspend fun activateDevice(
        userId: UserId,
        deviceId: AuthDeviceId,
        encryptedSecret: AeadEncryptedString
    ) = try {
        remoteDataSource.activateDevice(userId, deviceId, encryptedSecret)
    } finally {
        localDataSource.upsert(remoteDataSource.getAuthDevices(userId))
    }

    override suspend fun rejectAuthDevice(
        userId: UserId,
        deviceId: AuthDeviceId
    ) = try {
        remoteDataSource.rejectAuthDevice(userId, deviceId)
    } finally {
        localDataSource.upsert(remoteDataSource.getAuthDevices(userId))
    }

    override suspend fun requestAdminHelp(
        userId: UserId,
        deviceId: AuthDeviceId
    ) = try {
        remoteDataSource.requestAdminHelp(userId, deviceId)
    } finally {
        localDataSource.upsert(remoteDataSource.getAuthDevices(userId))
    }

    override fun observeByUserId(userId: UserId, refresh: Boolean): Flow<List<AuthDevice>> =
        store.stream(StoreRequest.cached(userId, refresh = refresh))
            .map { it.dataOrNull().orEmpty() }
            .distinctUntilChanged()

    override fun observeByDeviceId(userId: UserId, deviceId: AuthDeviceId, refresh: Boolean): Flow<AuthDevice?> =
        store.stream(StoreRequest.cached(userId, refresh))
            .map { it.dataOrNull().orEmpty() }
            .map { devices -> devices.firstOrNull { it.deviceId == deviceId } }
            .distinctUntilChanged()

    override suspend fun getByUserId(
        userId: UserId,
        refresh: Boolean
    ): List<AuthDevice> = (if (refresh) store.fresh(userId) else store.get(userId))

    override suspend fun getByDeviceId(
        userId: UserId,
        deviceId: AuthDeviceId,
        refresh: Boolean
    ): AuthDevice? = getByUserId(userId).firstOrNull { it.deviceId == deviceId }

    override suspend fun deleteByUserId(userId: UserId) {
        localDataSource.deleteAll(userId)
    }

    override suspend fun deleteByDeviceId(userId: UserId, deviceId: AuthDeviceId) {
        localDataSource.deleteByDeviceId(deviceId)
        workManager.enqueue(DeleteAuthDeviceWorker.makeWorkerRequest(deviceId, userId))
    }

    override suspend fun getUnprivatizationInfo(userId: UserId): UnprivatizationInfo =
        remoteDataSource.getUnprivatizationInfo(userId)
}

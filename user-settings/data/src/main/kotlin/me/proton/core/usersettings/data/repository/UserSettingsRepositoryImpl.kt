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

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.data.extension.toUserSettingsPropertySerializable
import me.proton.core.usersettings.data.worker.FetchUserSettingsWorker
import me.proton.core.usersettings.data.worker.UpdateUserSettingsWorker
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.entity.UserSettingsProperty
import me.proton.core.usersettings.domain.entity.UserSettingsProperty.CrashReports
import me.proton.core.usersettings.domain.entity.UserSettingsProperty.Telemetry
import me.proton.core.usersettings.domain.repository.UserSettingsLocalDataSource
import me.proton.core.usersettings.domain.repository.UserSettingsRemoteDataSource
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

class UserSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: UserSettingsLocalDataSource,
    private val remoteDataSource: UserSettingsRemoteDataSource,
    private val validateServerProof: ValidateServerProof,
    private val workManager: WorkManager,
    scopeProvider: CoroutineScopeProvider
) : UserSettingsRepository {

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: UserId ->
            remoteDataSource.fetch(key)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> localDataSource.observeByUserId(key) },
            writer = { _, input -> localDataSource.insertOrUpdate(input) },
            delete = { key -> localDataSource.delete(key) },
            deleteAll = { localDataSource.deleteAll() }
        )
    ).disableCache().buildProtonStore(scopeProvider) // We don't want potential stale data from memory cache

    override suspend fun setUsername(sessionUserId: SessionUserId, username: String): Boolean =
        remoteDataSource.setUsername(sessionUserId, username)

    override suspend fun updateUserSettings(userSettings: UserSettings) {
        localDataSource.insertOrUpdate(userSettings)
    }

    override fun getUserSettingsFlow(sessionUserId: SessionUserId, refresh: Boolean): Flow<DataResult<UserSettings>> {
        return store.stream(StoreRequest.cached(sessionUserId, refresh = refresh)).map { it.toDataResult() }
    }

    override suspend fun getUserSettings(sessionUserId: SessionUserId, refresh: Boolean) =
        if (refresh) store.fresh(sessionUserId) else store.get(sessionUserId)

    override suspend fun updateRecoveryEmail(
        sessionUserId: SessionUserId,
        email: String,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorProof: SecondFactorProof?,
    ): UserSettings {
        val (userSettings, serverProof) = remoteDataSource.updateRecoveryEmail(
            sessionUserId = sessionUserId,
            email = email,
            srpProofs = srpProofs,
            srpSession = srpSession,
            secondFactorProof = secondFactorProof
        )
        validateServerProof(serverProof, srpProofs.expectedServerProof) { "recovery email update failed" }
        localDataSource.insertOrUpdate(userSettings)
        return getUserSettings(sessionUserId)
    }

    override suspend fun updateLoginPassword(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorProof: SecondFactorProof?,
        auth: Auth
    ): UserSettings {
        val (userSettings, serverProof) = remoteDataSource.updateLoginPassword(
            sessionUserId = sessionUserId,
            srpProofs = srpProofs,
            srpSession = srpSession,
            secondFactorProof = secondFactorProof,
            auth = auth
        )
        validateServerProof(serverProof, srpProofs.expectedServerProof) { "password change failed" }
        localDataSource.insertOrUpdate(userSettings)
        return getUserSettings(sessionUserId)
    }


    override fun markAsStale(userId: UserId) {
        // Replace any existing FetchUserSettingsWorker.
        workManager.enqueueUniqueWork(
            "freshUserSettingsWork-${userId.id}",
            ExistingWorkPolicy.REPLACE,
            FetchUserSettingsWorker.getRequest(userId),
        )
    }

    override suspend fun updateCrashReports(
        userId: UserId,
        isEnabled: Boolean
    ) = updateProperty(userId, CrashReports(isEnabled)) {
        it.copy(crashReports = isEnabled)
    }

    override suspend fun updateTelemetry(
        userId: UserId,
        isEnabled: Boolean
    ) = updateProperty(userId, Telemetry(isEnabled)) {
        it.copy(telemetry = isEnabled)
    }

    private suspend fun updateProperty(
        userId: UserId,
        settingsProperty: UserSettingsProperty,
        updateProperty: suspend (UserSettings) -> UserSettings
    ): UserSettings {
        val userSettings = getUserSettings(userId)
        val updatedUserSettings = updateProperty(userSettings)
        localDataSource.insertOrUpdate(updatedUserSettings)

        workManager.enqueueUniqueWork(
            "updateUserSettingsWork-${userId.id}-${settingsProperty.javaClass.simpleName}",
            ExistingWorkPolicy.REPLACE,
            UpdateUserSettingsWorker.getRequest(userId, settingsProperty.toUserSettingsPropertySerializable())
        )
        return updatedUserSettings
    }
}

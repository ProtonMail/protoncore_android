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
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess
import me.proton.core.usersettings.data.api.UserSettingsApi
import me.proton.core.usersettings.data.api.request.SetUsernameRequest
import me.proton.core.usersettings.data.api.request.UpdateRecoveryEmailRequest
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import me.proton.core.usersettings.data.extension.fromEntity
import me.proton.core.usersettings.data.extension.fromResponse
import me.proton.core.usersettings.data.extension.toEntity
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository

class UserSettingsRepositoryImpl(
    db: UserSettingsDatabase,
    private val apiProvider: ApiProvider
) : UserSettingsRepository {

    private val userSettingsDao = db.userSettingsDao()

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: UserId ->
            apiProvider.get<UserSettingsApi>(key).invoke {
                getUserSettings().settings.fromResponse(key)
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> observeByUserId(key) },
            writer = { _, input -> insertOrUpdate(input) },
            delete = { key -> delete(key) },
            deleteAll = { deleteAll() }
        )
    ).disableCache().build() // We don't want potential stale data from memory cache

    private fun observeByUserId(userId: UserId): Flow<UserSettings?> =
        userSettingsDao.observeByUserId(userId).map { it?.fromEntity() }

    private suspend fun insertOrUpdate(settings: UserSettings) =
        userSettingsDao.insertOrUpdate(settings.toEntity())

    private suspend fun delete(userId: UserId) =
        userSettingsDao.delete(userId)

    private suspend fun deleteAll() =
        userSettingsDao.deleteAll()

    override suspend fun setUsername(sessionUserId: SessionUserId, username: String): Boolean =
        apiProvider.get<UserSettingsApi>(sessionUserId).invoke {
            setUsername(SetUsernameRequest(username = username)).isSuccess()
        }.valueOrThrow

    override suspend fun getUserSettings(sessionUserId: SessionUserId, refresh: Boolean) =
        if (refresh) store.fresh(sessionUserId) else store.get(sessionUserId)

    override suspend fun updateRecoveryEmail(
        sessionUserId: SessionUserId,
        email: String,
        clientEphemeral: String,
        clientProof: String,
        srpSession: String,
        secondFactorCode: String
    ): UserSettings {
        return apiProvider.get<UserSettingsApi>(sessionUserId).invoke {
            val response = updateRecoveryEmail(
                UpdateRecoveryEmailRequest(
                    email = email,
                    twoFactorCode = secondFactorCode,
                    clientEphemeral = clientEphemeral,
                    clientProof = clientProof,
                    srpSession = srpSession
                )
            )
            insertOrUpdate(response.settings.fromResponse(sessionUserId))
            getUserSettings(sessionUserId)
        }.valueOrThrow
    }
}

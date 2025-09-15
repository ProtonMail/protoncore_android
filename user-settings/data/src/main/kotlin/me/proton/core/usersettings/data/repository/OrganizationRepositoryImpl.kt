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

import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.toPublicKey
import me.proton.core.network.data.ApiProvider
import me.proton.core.usersettings.data.api.OrganizationApi
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.extension.fromEntity
import me.proton.core.usersettings.data.extension.fromResponse
import me.proton.core.usersettings.data.extension.toEntity
import me.proton.core.usersettings.domain.entity.Organization
import me.proton.core.usersettings.domain.entity.OrganizationKeys
import me.proton.core.usersettings.domain.entity.OrganizationSettings
import me.proton.core.usersettings.domain.entity.OrganizationSignature
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class OrganizationRepositoryImpl @Inject constructor(
    db: OrganizationDatabase,
    private val apiProvider: ApiProvider,
    scopeProvider: CoroutineScopeProvider
) : OrganizationRepository {

    private val organizationDao = db.organizationDao()
    private val organizationKeysDao = db.organizationKeysDao()

    private val storeOrganization = StoreBuilder.from(
        fetcher = Fetcher.of { key: UserId ->
            apiProvider.get<OrganizationApi>(key).invoke {
                getOrganization().organization.fromResponse(key)
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> observeOrganizationByUserId(key) },
            writer = { _, input -> insertOrUpdate(input) },
            delete = { key -> deleteOrganization(key) },
            deleteAll = { deleteAllOrganizations() }
        )
    ).disableCache().buildProtonStore(scopeProvider) // We don't want potential stale data from memory cache

    private val storeOrganizationKeys = StoreBuilder.from(
        fetcher = Fetcher.of { key: UserId ->
            apiProvider.get<OrganizationApi>(key).invoke {
                getOrganizationKeys().fromResponse(key)
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> observeOrganizationKeysByUserId(key) },
            writer = { _, input -> insertOrUpdate(input) },
            delete = { key -> deleteOrganizationKeys(key) },
            deleteAll = { deleteAllOrganizationKeys() }
        )
    ).disableCache().buildProtonStore(scopeProvider) // We don't want potential stale data from memory cache

    private val organizationSignature = Cache.Builder<String, OrganizationSignature>()
        .expireAfterWrite(1.hours)
        .build()

    private val organizationSettings = Cache.Builder<String, OrganizationSettings>()
        .expireAfterWrite(1.hours)
        .build()

    private fun observeOrganizationByUserId(userId: UserId): Flow<Organization?> =
        organizationDao.observeByUserId(userId).map { it?.fromEntity() }

    private suspend fun insertOrUpdate(organization: Organization) =
        organizationDao.insertOrUpdate(organization.toEntity())

    private suspend fun deleteOrganization(userId: UserId) =
        organizationDao.delete(userId)

    private suspend fun deleteAllOrganizations() =
        organizationDao.deleteAll()

    override fun getOrganizationFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<Organization>> =
        storeOrganization.stream(StoreReadRequest.cached(sessionUserId, refresh)).map { it.toDataResult() }

    override suspend fun getOrganization(sessionUserId: SessionUserId, refresh: Boolean): Organization =
        if (refresh) storeOrganization.fresh(sessionUserId) else storeOrganization.get(sessionUserId)

    // organization keys
    private fun observeOrganizationKeysByUserId(userId: UserId): Flow<OrganizationKeys?> =
        organizationKeysDao.observeByUserId(userId).map { it?.fromEntity() }

    private suspend fun insertOrUpdate(organizationKeys: OrganizationKeys) =
        organizationKeysDao.insertOrUpdate(organizationKeys.toEntity())

    private suspend fun deleteOrganizationKeys(userId: UserId) =
        organizationKeysDao.delete(userId)

    private suspend fun deleteAllOrganizationKeys() =
        organizationKeysDao.deleteAll()

    override fun getOrganizationKeysFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<OrganizationKeys>> =
        storeOrganizationKeys.stream(StoreReadRequest.cached(sessionUserId, refresh)).map { it.toDataResult() }

    override suspend fun getOrganizationKeys(sessionUserId: SessionUserId, refresh: Boolean) =
        if (refresh) storeOrganizationKeys.fresh(sessionUserId) else storeOrganizationKeys.get(sessionUserId)

    override suspend fun getOrganizationSignature(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): OrganizationSignature = organizationSignature.get(sessionUserId.id) {
        apiProvider.get<OrganizationApi>(sessionUserId).invoke {
            val signature = getOrganizationSignature()
            OrganizationSignature(
                publicKey = signature.publicKey.toPublicKey(),
                fingerprintSignature = signature.fingerprintSignature,
                fingerprintSignatureAddress = signature.fingerprintSignatureAddress
            )
        }.valueOrThrow
    }

    override suspend fun getOrganizationSettings(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): OrganizationSettings = organizationSettings.get(sessionUserId.id) {
        apiProvider.get<OrganizationApi>(sessionUserId).invoke {
            val settings = getOrganizationSettings()
            OrganizationSettings(
                logoId = settings.logoId
            )
        }.valueOrThrow
    }

    override suspend fun getOrganizationLogo(
        sessionUserId: SessionUserId,
        logoId: String
    ): ByteArray = apiProvider.get<OrganizationApi>(sessionUserId).invoke {
        getOrganizationLogo(logoId).bytes()
    }.valueOrThrow
}

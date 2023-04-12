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

package me.proton.core.keytransparency.data.repository

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.data.local.KeyTransparencyDatabase
import me.proton.core.keytransparency.data.local.entity.SelfAuditResultEntity
import me.proton.core.keytransparency.data.local.toAddressChange
import me.proton.core.keytransparency.data.local.toEntity
import me.proton.core.keytransparency.data.remote.KeyTransparencyApi
import me.proton.core.keytransparency.data.remote.request.UploadVerifiedEpochRequest
import me.proton.core.keytransparency.data.remote.response.toVerifiedEpochEntity
import me.proton.core.keytransparency.domain.entity.AddressChange
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.entity.EpochId
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.SelfAuditResult
import me.proton.core.keytransparency.domain.entity.VerifiedEpoch
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.keytransparency.domain.usecase.NormalizeEmail
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.domain.entity.AddressId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressWarnings("TooManyFunctions")
public class KeyTransparencyRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val cryptoContext: CryptoContext,
    keyTransparencyDatabase: KeyTransparencyDatabase,
    private val normalizeEmail: NormalizeEmail
) : KeyTransparencyRepository {

    private val addressChangeDao = keyTransparencyDatabase.addressChangeDao()
    private val selfAuditResultDao = keyTransparencyDatabase.selfAuditResultDao()

    override suspend fun getProof(
        userId: UserId,
        epochId: EpochId,
        email: String
    ): ProofPair = apiProvider.get<KeyTransparencyApi>(userId).invoke {
        getProof(epochId, email).toProofPair()
    }.valueOrThrow

    override suspend fun getEpoch(userId: UserId, epochId: EpochId): Epoch =
        apiProvider.get<KeyTransparencyApi>(userId).invoke {
            getEpoch(epochId).toEpoch()
        }.valueOrThrow

    override suspend fun getLastEpoch(userId: UserId): Epoch = apiProvider.get<KeyTransparencyApi>(userId).invoke {
        val epochsResponse = getLastEpoch()
        require(epochsResponse.epochs.size == 1) { "API returned several epochs when fetching the last one" }
        epochsResponse.epochs.first().toEpoch()
    }.valueOrThrow

    override suspend fun getVerifiedEpoch(userId: UserId, addressId: AddressId): VerifiedEpoch =
        apiProvider.get<KeyTransparencyApi>(userId).invoke {
            getVerifiedEpoch(addressId.id).toVerifiedEpochEntity()
        }.valueOrThrow

    override suspend fun uploadVerifiedEpoch(
        userId: UserId,
        addressId: AddressId,
        verifiedEpoch: VerifiedEpoch
    ) {
        apiProvider.get<KeyTransparencyApi>(userId).invoke {
            val request = UploadVerifiedEpochRequest(verifiedEpoch.data, verifiedEpoch.signature)
            uploadVerifiedEpoch(addressId.id, request)
        }.throwIfError()
    }

    override suspend fun storeAddressChange(addressChange: AddressChange) {
        val entity = addressChange.toEntity(cryptoContext.keyStoreCrypto)
        addressChangeDao.insertOrUpdate(entity)
    }

    override suspend fun removeAddressChange(addressChange: AddressChange) {
        addressChangeDao.deleteAddressChange(addressChange.userId, addressChange.changeId)
    }

    override suspend fun removeAddressChangesForAddress(userId: UserId, email: String) {
        getAddressChangesForAddress(userId, email).forEach {
            removeAddressChange(it)
        }
    }

    override suspend fun getAllAddressChanges(userId: UserId): List<AddressChange> =
        addressChangeDao.getAddressChanges(userId).map { it.toAddressChange(cryptoContext.keyStoreCrypto) }

    override suspend fun getAddressChangesForAddress(userId: UserId, email: String): List<AddressChange> {
        val normalizedEmail = normalizeEmail(email)
        return addressChangeDao
            .getAddressChanges(userId)
            .map { it.toAddressChange(cryptoContext.keyStoreCrypto) }
            .filter { normalizeEmail(it.email) == normalizedEmail }
    }

    override suspend fun getTimestampOfSelfAudit(userId: UserId): Long? =
        selfAuditResultDao.getTimestampOfSelfAudit(userId)

    override suspend fun storeSelfAuditResult(userId: UserId, result: SelfAuditResult) {
        selfAuditResultDao.insertOrUpdate(
            SelfAuditResultEntity(userId, result.timestamp)
        )
    }
}

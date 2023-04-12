/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.keytransparency.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.AddressChange
import me.proton.core.keytransparency.domain.entity.TimedState
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.UnverifiableSKLException
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheck
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheckNotNull
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import javax.inject.Inject

internal class VerifyAddressChangeWasIncluded @Inject constructor(
    private val verifyProofInEpoch: VerifyProofInEpoch,
    private val keyTransparencyRepository: KeyTransparencyRepository,
    private val publicAddressRepository: PublicAddressRepository,
    private val getCurrentTime: GetCurrentTime,
    private val verifySignedKeyListSignature: VerifySignedKeyListSignature,
    private val verifyObsolescenceInclusion: VerifyObsolescenceInclusion
) {
    suspend operator fun invoke(userId: UserId, addressChange: AddressChange) {
        val email = addressChange.email
        val expectedMinEpochId = addressChange.epochId
        val creationTimestamp = addressChange.creationTimestamp

        val targetSKL = getTargetSKL(userId, addressChange)
            ?: return // Couldn't get the SKL, either too early or too late for verification

        val isObsolescence = targetSKL.data == null

        val targetSKLTimestamp = if (!isObsolescence) {
            // Address is not obsolete
            val publicAddress = runCatching { // Could fail if address was disabled recently
                publicAddressRepository.getPublicAddress(userId, email, source = Source.RemoteOrCached)
            }.getOrNull()
            val savedPublicKeys = addressChange.publicKeys.map { key ->
                PublicKey(key, isActive = true, isPrimary = false, canEncrypt = false, canVerify = true)
            }
            val verificationKeys = PublicKeyRing(
                keys = savedPublicKeys +
                    (publicAddress?.keys?.map { it.publicKey } ?: emptyList())
            )
            val timestamp = runCatching {
                verifySignedKeyListSignature(verificationKeys, targetSKL)
            }.getOrElse {
                throw UnverifiableSKLException(it)
            }
            keyTransparencyCheck(timestamp >= creationTimestamp) { "Target SKL is older than the one in local storage" }
            keyTransparencyCheck(
                creationTimestamp >= timestamp - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
            ) { "Target SKL is too long after the one in local storage" }
            timestamp
        } else null

        val minEpochId = keyTransparencyCheckNotNull(targetSKL.minEpochId) { "Target SKL was not yet in KT" }
        keyTransparencyCheck(minEpochId <= expectedMinEpochId) { "Target SKL was included after expected min epoch" }
        val epoch = keyTransparencyRepository.getEpoch(userId, minEpochId)
        val proofs = keyTransparencyRepository.getProof(userId, minEpochId, addressChange.email)
        val verifiedState = verifyProofInEpoch.invoke(
            addressChange.email,
            targetSKL,
            epoch,
            proofs
        )
        keyTransparencyCheck(
            verifiedState is VerifiedState.Existent || verifiedState is VerifiedState.Obsolete
        ) { "State of inclusion proof needs to be existent or obsolete" }
        keyTransparencyCheck(
            (verifiedState as TimedState).notBefore <= creationTimestamp + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
        ) { "Inclusion proof is too long after the creation of the local change" }
        if (addressChange.isObsolete) {
            // Special handling needed for obsolete addresses
            verifyObsolescenceInclusion(proofs, creationTimestamp, targetSKLTimestamp)
        }
        keyTransparencyRepository.removeAddressChange(addressChange)
    }

    private suspend fun getTargetSKL(userId: UserId, addressChange: AddressChange): PublicSignedKeyList? {
        val email = addressChange.email
        val expectedMinEpochId = addressChange.epochId
        val creationTimestamp = addressChange.creationTimestamp
        return runCatching {
            publicAddressRepository.getSKLAtEpoch(userId, expectedMinEpochId, email)
        }.getOrElse { reason ->
            if (
                reason is ApiException &&
                reason.error is ApiResult.Error.Http &&
                (reason.error as ApiResult.Error.Http).httpCode == HttpResponseCodes.HTTP_UNPROCESSABLE
            ) {
                val now = getCurrentTime()
                val epochHasExpired = creationTimestamp < now - Constants.KT_EPOCH_VALIDITY_PERIOD_SECONDS
                if (epochHasExpired) {
                    // Address change is too old, can't be checked
                    keyTransparencyRepository.removeAddressChange(addressChange)
                } else {
                    // Happens if the expected min epoch id wasn't created yet.
                    keyTransparencyCheck(
                        creationTimestamp >= getCurrentTime() - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
                    ) { "Change was not included after the max allowed interval" }
                }
                return null // Early termination, the epoch is not ready yet, or already expired
            } else {
                throw reason
            }
        }
    }
}

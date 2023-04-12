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
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.KeyTransparencyLogger
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheck
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import javax.inject.Inject

@SuppressWarnings("LongParameterList")
internal class VerifyPublicAddress @Inject internal constructor(
    private val verifyProofInEpoch: VerifyProofInEpoch,
    private val checkAbsenceProof: CheckAbsenceProof,
    private val keyTransparencyRepository: KeyTransparencyRepository,
    private val checkSignedKeyListMatch: CheckSignedKeyListMatch,
    private val verifySignedKeyListSignature: VerifySignedKeyListSignature,
    private val storeAddressChange: StoreAddressChange,
    private val getCurrentTime: GetCurrentTime
) {
    suspend operator fun invoke(userId: UserId, address: PublicAddress): PublicKeyVerificationResult = try {
        val skl = address.signedKeyList
        when {
            address.ignoreKT != 0 -> {
                KeyTransparencyLogger.d("Skipping verification because IgnoreKT flag is: ${address.ignoreKT}")
                PublicKeyVerificationResult.Success(VerifiedState.Absent(getCurrentTime()))
            }
            skl == null -> {
                val absentState = checkAbsenceProof(userId, address)
                PublicKeyVerificationResult.Success(absentState)
            }
            skl.data == null && skl.signature == null -> { // obsolete address
                val sklMaxEpochId = skl.maxEpochId
                if (sklMaxEpochId == null) {
                    storeAddressChange(userId, address, skl, isObsolete = true)
                    PublicKeyVerificationResult.Success(VerifiedState.NotYetIncluded)
                } else {
                    val verifiedState = verifyAddressInEpoch(userId, sklMaxEpochId, address)
                    keyTransparencyCheck(
                        verifiedState is VerifiedState.Obsolete
                    ) { "Expected an obsolescence proof" }
                    keyTransparencyCheck(
                        verifiedState.notBefore >= getCurrentTime() - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
                    ) { "Epoch is too old" }
                    PublicKeyVerificationResult.Success(verifiedState)
                }
            }
            else -> {
                verifySignedKeyListSignature(address, skl)
                val sklMaxEpochId = skl.maxEpochId
                if (sklMaxEpochId == null) {
                    storeAddressChange(userId, address, skl)
                    PublicKeyVerificationResult.Success(VerifiedState.NotYetIncluded)
                } else {
                    checkSignedKeyListMatch(address, skl)
                    val verifiedState = verifyAddressInEpoch(userId, sklMaxEpochId, address)
                    keyTransparencyCheck(verifiedState is VerifiedState.Existent) { "Expected an existence proof" }
                    keyTransparencyCheck(
                        verifiedState.notBefore >= getCurrentTime() - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
                    ) { "Epoch is too old" }
                    PublicKeyVerificationResult.Success(verifiedState)
                }
            }
        }
    } catch (exception: KeyTransparencyException) {
        PublicKeyVerificationResult.Failure(exception)
    }

    private suspend fun verifyAddressInEpoch(
        userId: UserId,
        sklMaxEpochId: Int,
        address: PublicAddress
    ): VerifiedState {
        val epoch = keyTransparencyRepository.getEpoch(userId, sklMaxEpochId)
        val proofs = keyTransparencyRepository.getProof(userId, epoch.epochId, address.email)
        return verifyProofInEpoch(
            address.email,
            address.signedKeyList,
            epoch,
            proofs
        )
    }
}

public sealed class PublicKeyVerificationResult {
    public data class Success(val state: VerifiedState) : PublicKeyVerificationResult()
    public data class Failure(val cause: Throwable) : PublicKeyVerificationResult()
}

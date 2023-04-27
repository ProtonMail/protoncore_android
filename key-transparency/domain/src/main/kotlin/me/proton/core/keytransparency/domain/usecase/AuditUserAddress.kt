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
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.TimedState
import me.proton.core.keytransparency.domain.entity.UserAddressAuditResult
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheck
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheckNotNull
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

@SuppressWarnings("LongParameterList")
internal class AuditUserAddress @Inject constructor(
    private val checkAbsenceProof: CheckAbsenceProof,
    private val checkSignedKeyListMatch: CheckSignedKeyListMatch,
    private val keyTransparencyRepository: KeyTransparencyRepository,
    private val verifyProofInEpoch: VerifyProofInEpoch,
    private val verifySignedKeyListSignature: VerifySignedKeyListSignature,
    private val uploadVerifiedEpoch: UploadVerifiedEpoch,
    private val buildInitialEpoch: BuildInitialEpoch,
    private val fetchVerifiedEpoch: FetchVerifiedEpoch,
    private val getCurrentTime: GetCurrentTime,
    private val publicAddressRepository: PublicAddressRepository
) {

    suspend operator fun invoke(userId: UserId, userAddress: UserAddress): UserAddressAuditResult {
        return try {
            if (!userAddress.enabled) {
                return UserAddressAuditResult.Warning.Disabled
            }
            val inputSKL = userAddress.signedKeyList
            if (inputSKL == null) {
                checkAbsenceProof(userId, userAddress)
                // User needs to log in to web client to activate KT
                UserAddressAuditResult.Warning.AddressNotInKT
            } else {
                auditAddress(userId, userAddress, inputSKL)
            }
        } catch (exception: KeyTransparencyException) {
            UserAddressAuditResult.Failure(exception)
        }
    }

    private suspend fun auditAddress(
        userId: UserId,
        userAddress: UserAddress,
        inputSKL: PublicSignedKeyList
    ): UserAddressAuditResult {
        val verifiedEpoch = fetchVerifiedEpoch(userId, userAddress)
        val newSKLs = publicAddressRepository.getSKLsAfterEpoch(
            userId,
            verifiedEpoch?.epochId ?: 0,
            userAddress.email
        )
        val initialEpoch =
            buildInitialEpoch(verifiedEpoch, newSKLs, userId, userAddress, inputSKL)
                // Initial epoch can't be bootstrapped
                ?: return UserAddressAuditResult.Warning.CreationTooRecent
        if (newSKLs.isEmpty()) {
            updateVerifiedEpochNoChanges(inputSKL, userId, userAddress, initialEpoch)
        } else {
            updateVerifiedEpochWithChanges(newSKLs, initialEpoch, userAddress, userId, inputSKL)
        }
        return UserAddressAuditResult.Success
    }

    private suspend fun updateVerifiedEpochNoChanges(
        inputSKL: PublicSignedKeyList,
        userId: UserId,
        userAddress: UserAddress,
        initialEpoch: VerifiedEpochData
    ) {
        val maxEpochId = keyTransparencyCheckNotNull(inputSKL.maxEpochId) { "Input SKL max epoch ID is null" }
        val (verifiedState, revision) = verifyMaxEpoch(userId, maxEpochId, userAddress, inputSKL)
        keyTransparencyCheck(verifiedState is TimedState) { "Expected a state with a time" }
        keyTransparencyCheck(
            verifiedState.notBefore >= getCurrentTime() - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
        ) { "New verified epoch is too old" }
        keyTransparencyCheck(revision == initialEpoch.revision) { "Revision has changed but no SKL were provided" }
        if (maxEpochId > initialEpoch.epochId) {
            // Update the verified epoch
            uploadVerifiedEpoch(
                userId,
                userAddress.addressId,
                VerifiedEpochData(maxEpochId, initialEpoch.revision, initialEpoch.sklCreationTime)
            )
        }
    }

    private suspend fun updateVerifiedEpochWithChanges(
        newSKLs: List<PublicSignedKeyList>,
        initialEpoch: VerifiedEpochData,
        userAddress: UserAddress,
        userId: UserId,
        inputSKL: PublicSignedKeyList
    ) {
        var previousVerifiedEpoch = initialEpoch
        var previousCertificateDate: Long? = null
        var previousSKLCreationTime = initialEpoch.sklCreationTime
        newSKLs.forEachIndexed { index, newSKL ->
            val isLast = index == newSKLs.size - 1
            val isFirst = index == 0
            val maxEpochId = if (isLast) {
                newSKL.maxEpochId
            } else {
                keyTransparencyCheckNotNull(newSKL.maxEpochId) { "SKL without MaxEpochId was not the last one" }
            }
            val timestamp = if (newSKL.data != null) {
                verifySignedKeyListSignature(userAddress, newSKL)
            } else null
            if (timestamp != null) {
                keyTransparencyCheck(
                    timestamp >= previousSKLCreationTime
                ) { "SKL Creation time must increase monotonically."}
                previousSKLCreationTime = timestamp
            }
            if (maxEpochId != null) {
                val (verifiedState, revision) = verifyMaxEpoch(userId, maxEpochId, userAddress, newSKL)
                val isRevisionConsistent = if (isFirst) {
                    revision == previousVerifiedEpoch.revision || revision == previousVerifiedEpoch.revision + 1
                } else {
                    revision == previousVerifiedEpoch.revision + 1
                }
                keyTransparencyCheck(isRevisionConsistent) { "Revision chain is inconsistent" }
                previousVerifiedEpoch = VerifiedEpochData(
                    maxEpochId,
                    revision,
                    timestamp ?: previousVerifiedEpoch.sklCreationTime
                )
                previousCertificateDate = (verifiedState as TimedState).notBefore
            } else {
                // the last SKL cannot be an obsolescence because the address is not disabled
                keyTransparencyCheckNotNull(timestamp) { "Last SKL was obsolescent" }
                // the last SKL is not yet included
                keyTransparencyCheck(
                    timestamp >= getCurrentTime() - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
                ) { "Last SKL was not included after max epoch interval" }
            }
        }
        keyTransparencyCheck(inputSKL.data == newSKLs.last().data) { "Last SKL was different from the input one" }
        checkSignedKeyListMatch(userAddress, inputSKL)
        if (newSKLs.any { it.maxEpochId != null }) {
            val certificateDate = keyTransparencyCheckNotNull(previousCertificateDate) { "Certificate date is null" }
            val newVerifiedEpoch = previousVerifiedEpoch
            keyTransparencyCheck(
                certificateDate >= getCurrentTime() - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
            ) { "Certificate is too old" }
            uploadVerifiedEpoch(userId, userAddress.addressId, newVerifiedEpoch)
        }
    }

    private suspend fun verifyMaxEpoch(
        userId: UserId,
        maxEpochId: Int,
        userAddress: UserAddress,
        inputSKL: PublicSignedKeyList
    ): Pair<VerifiedState, Int> {
        val epoch = keyTransparencyRepository.getEpoch(userId, maxEpochId)
        val proof = keyTransparencyRepository.getProof(userId, maxEpochId, userAddress.email)
        val verifiedState = verifyProofInEpoch(userAddress.email, inputSKL, epoch, proof)
        keyTransparencyCheck(
            verifiedState is VerifiedState.Existent || verifiedState is VerifiedState.Obsolete
        ) { "Self audit should only encounter existent or obsolete SKLs" }
        val revision = keyTransparencyCheckNotNull(proof.proof.revision) { "Audited epoch has no revision" }
        return Pair(verifiedState, revision)
    }
}

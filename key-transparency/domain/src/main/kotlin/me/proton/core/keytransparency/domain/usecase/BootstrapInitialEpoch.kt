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
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.TimedState
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheck
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheckNotNull
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

internal class BootstrapInitialEpoch @Inject constructor(
    private val verifySignedKeyListSignature: VerifySignedKeyListSignature,
    private val getCurrentTime: GetCurrentTime,
    private val keyTransparencyRepository: KeyTransparencyRepository,
    private val verifyProofInEpoch: VerifyProofInEpoch
) {

    suspend operator fun invoke(
        userId: UserId,
        userAddress: UserAddress,
        inputSKL: PublicSignedKeyList,
        newSKLs: List<PublicSignedKeyList>
    ): VerifiedEpochData? {
        // bootstrapping the verified epoch
        keyTransparencyCheck(newSKLs.isNotEmpty()) { "Can't bootstrap, no SKL available" }
        val oldestSKL = newSKLs[0]
        if (oldestSKL.minEpochId == null) {
            keyTransparencyCheck(
                oldestSKL.data == inputSKL.data &&
                    oldestSKL.signature == inputSKL.signature
            ) { "Input SKL did not equal the only SKL" }
            // The address is too recent to bootstrap the verified epoch
            keyTransparencyCheck(newSKLs.size == 1) { "New address had more SKLs than the current one" }
            val timestamp = verifySignedKeyListSignature(userAddress, inputSKL)
            keyTransparencyCheck(
                timestamp >= getCurrentTime() - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
            ) { "New address was not included after max epoch interval" }
            return null
        }
        val minEpochId = keyTransparencyCheckNotNull(oldestSKL.minEpochId)
        val epoch = keyTransparencyRepository.getEpoch(userId, minEpochId)
        val proof = keyTransparencyRepository.getProof(userId, minEpochId, userAddress.email)
        val verifiedState = verifyProofInEpoch(userAddress.email, oldestSKL, epoch, proof)
        val revision = proof.proof.revision
        keyTransparencyCheck(verifiedState is TimedState) { "Can't be not included yet" }
        if (revision != 0) {
            val backThen = getCurrentTime() - Constants.KT_EPOCH_VALIDITY_PERIOD_SECONDS
            val oldEpochLowerBound = backThen - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
            val oldEpochHigherBound = backThen + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
            keyTransparencyCheck(
                verifiedState.notBefore in oldEpochLowerBound..oldEpochHigherBound
            ) { "Oldest epoch is not in range" }
        }
        val bootstrappedRevision = keyTransparencyCheckNotNull(revision) { "Bootstrapped epoch had no revision" }
        return VerifiedEpochData(minEpochId, bootstrappedRevision, 0)
    }
}

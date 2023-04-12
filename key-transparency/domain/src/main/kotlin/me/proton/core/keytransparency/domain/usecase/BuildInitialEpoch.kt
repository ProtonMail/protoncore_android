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
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheckNotNull
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

internal class BuildInitialEpoch @Inject constructor(
    private val keyTransparencyRepository: KeyTransparencyRepository,
    private val bootstrapInitialEpoch: BootstrapInitialEpoch
) {

    suspend operator fun invoke(
        verifiedEpoch: VerifiedEpochData?,
        newSKLs: List<PublicSignedKeyList>,
        userId: UserId,
        userAddress: UserAddress,
        inputSKL: PublicSignedKeyList
    ) = if (verifiedEpoch != null) {
        val firstRevision = if (newSKLs.isNotEmpty() && newSKLs[0].minEpochId != null) {
            val firstSKLMinEpochId = keyTransparencyCheckNotNull(newSKLs[0].minEpochId)
            val proofs = keyTransparencyRepository.getProof(userId, firstSKLMinEpochId, userAddress.email)
            keyTransparencyCheckNotNull(proofs.proof.revision) { "Expected a revision during self audit" }
        } else null
        when {
            firstRevision == null -> verifiedEpoch
            firstRevision == verifiedEpoch.revision -> verifiedEpoch
            firstRevision == verifiedEpoch.revision + 1 -> verifiedEpoch
            firstRevision > verifiedEpoch.revision + 1 -> {
                // Gap in revision, it can happen if the verified epoch is old.
                bootstrapInitialEpoch(userId, userAddress, inputSKL, newSKLs)
            }
            else -> throw KeyTransparencyException("First SKL's revision was smaller than the verified epoch's")
        }
    } else {
        bootstrapInitialEpoch(userId, userAddress, inputSKL, newSKLs)
    }
}

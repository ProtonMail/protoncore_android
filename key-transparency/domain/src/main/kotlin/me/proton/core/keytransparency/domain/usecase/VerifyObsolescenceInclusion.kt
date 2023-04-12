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

import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.Proof
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.ProofType
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheck
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheckNotNull
import javax.inject.Inject

internal class VerifyObsolescenceInclusion @Inject constructor(
    private val getObsolescenceTokenTimestamp: GetObsolescenceTokenTimestamp
) {

    operator fun invoke(
        proofs: ProofPair,
        creationTimestamp: Long,
        targetSKLTimestamp: Long?
    ) {
        when (proofs.proof.type.enum) {
            ProofType.OBSOLESCENCE -> {
                checkObsoleteTokenRange(proofs.proof, creationTimestamp)
            }
            ProofType.EXISTENCE -> {
                val timestamp = keyTransparencyCheckNotNull(
                    targetSKLTimestamp
                ) { "Target SKL must have a timestamp for an existence proof" }
                keyTransparencyCheck(
                    timestamp > creationTimestamp
                ) { "An obsolete local change requires the included change to be strictly more recent" }
                keyTransparencyCheck(
                    timestamp <= creationTimestamp + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
                ) { "Target SKL is too long after the creation timestamp" }
            }
            else -> {
                val typeVal = proofs.proof.type.value
                error("Inclusion proof of obsolete address should be obsolete or existent, was $typeVal")
            }
        }
    }

    private fun checkObsoleteTokenRange(proof: Proof, creationTimestamp: Long) {
        val token = keyTransparencyCheckNotNull(proof.obsolescenceToken) { "Obsolescence token was null" }
        val tokenTimestamp = getObsolescenceTokenTimestamp(token)
        keyTransparencyCheck(
            tokenTimestamp <= creationTimestamp + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
        ) { "Target SKL is too long after the creation timestamp" }
        keyTransparencyCheck(
            tokenTimestamp >= creationTimestamp - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS
        ) { "Target SKL is too long before the creation timestamp" }
    }
}

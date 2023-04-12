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

import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.ProofType
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheck
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheckNotNull
import javax.inject.Inject

internal class VerifyProofInEpoch @Inject constructor(
    private val verifyProof: VerifyProof,
    private val verifyEpoch: VerifyEpoch,
    private val normalizeEmail: NormalizeEmail
) {
    suspend operator fun invoke(
        email: String,
        signedKeyList: PublicSignedKeyList?,
        epoch: Epoch,
        proofs: ProofPair
    ): VerifiedState {
        val notBefore = verifyEpoch(epoch)
        val proof = proofs.proof
        val proofType = checkNotNull(proof.type.enum) { "Unknown proof type, was ${proof.type.value}" }
        val signedKeyListData: String? = when (proofType) {
            ProofType.EXISTENCE -> {
                keyTransparencyCheckNotNull(signedKeyList?.data) { "Address doesn't have a signed key list" }
            }
            ProofType.ABSENCE -> {
                keyTransparencyCheck(signedKeyList == null) { "Address not in KT should not have an SKL" }
                null
            }
            ProofType.OBSOLESCENCE -> {
                val token = keyTransparencyCheckNotNull(
                    proof.obsolescenceToken
                ) { "Address doesn't have an obsolescence token" }
                keyTransparencyCheck(token.isHexadecimal()) { "Obsolescence token is not a valid hexadecimal string" }
                token
            }
        }
        if (proofType != ProofType.ABSENCE) {
            keyTransparencyCheckNotNull(proof.revision) { "Revision was null for proof of existence" }
        }
        verifyProof(
            email = normalizeEmail(email),
            signedKeyList = signedKeyListData,
            proof = proof,
            rootHash = epoch.treeHash
        )
        return when (proofType) {
            ProofType.EXISTENCE -> VerifiedState.Existent(notBefore)
            ProofType.ABSENCE -> VerifiedState.Absent(notBefore)
            ProofType.OBSOLESCENCE -> VerifiedState.Obsolete(notBefore)
        }
    }

    companion object {
        private const val HEX_DIGITS = "0123456789abcdefABCDEF"
        private fun String.isHexadecimal(): Boolean {
            for (char in this) {
                if (!HEX_DIGITS.contains(char)) {
                    return false
                }
            }
            return true
        }
    }
}

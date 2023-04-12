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

package me.proton.core.keytransparency.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.keytransparency.domain.entity.Proof
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.ProofType

@Serializable
internal data class ProofPairResponse(
    @SerialName("Proof")
    val proof: ProofResponse,
    @SerialName("CatchAllProof")
    val catchAllProof: ProofResponse? = null
) {
    fun toProofPair() = ProofPair(
        proof = proof.toProof(),
        catchAllProof = catchAllProof?.toProof()
    )
}

@Serializable
internal data class ProofResponse(
    @SerialName("Type")
    val type: Int,
    @SerialName("Neighbors")
    val neighbors: List<String?>,
    @SerialName("Verifier")
    val vrfProof: String,
    @SerialName("Revision")
    val revision: Int? = null,
    @SerialName("ObsolescenceToken")
    val obsolescenceToken: String? = null
) {
    fun toProof() = Proof(
        type = ProofType.enumOf(type),
        neighbors = neighbors
            .mapIndexedNotNull { index, neighbor ->
                if (neighbor != null) {
                    index to neighbor
                } else null
            }
            .toMap(),
        vrfProof = vrfProof,
        revision = revision,
        obsolescenceToken = obsolescenceToken
    )
}

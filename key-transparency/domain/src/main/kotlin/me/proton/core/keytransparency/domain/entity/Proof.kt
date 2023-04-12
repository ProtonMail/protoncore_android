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

package me.proton.core.keytransparency.domain.entity

import me.proton.core.domain.type.IntEnum

public typealias ObsolescenceToken = String

/**
 * The server can return up to two proofs for an address,
 * one for the actual address, and one for the catchall address.
 *
 * @param proof: the proof for the address
 * @param catchAllProof: the proof for the catchall address
 */
public data class ProofPair(
    val proof: Proof,
    val catchAllProof: Proof?
)

/**
 * The proof to verify the inclusion of an address in KT
 *
 * @param type: proofs can have several types based on whether the address is in KT or not.
 * @param neighbors: the values of the neighbors in the merkle tree to verify inclusion.
 * @param vrfProof: the VRF proof to be able to get the path in the merkle tree for the address.
 * @param revision: the revision counter for the address in the merkle tree, or null for absence proof.
 * @param obsolescenceToken: the obsolescence token for obsolescence proofs, null otherwise.
 */
public data class Proof(
    val type: IntEnum<ProofType>,
    val neighbors: Map<Int, String>,
    val vrfProof: String,
    val revision: Int?,
    val obsolescenceToken: ObsolescenceToken?
)

/**
 * The proof types specifies what the server is trying to prove.
 */
public enum class ProofType(public val value: Int) {
    /**
     * The address is not in KT
     */
    ABSENCE(0),

    /**
     * The address is in KT
     */
    EXISTENCE(1),

    /**
     * The address is no longer in KT (obsolete)
     */
    OBSOLESCENCE(2);

    public fun getIntEnum(): IntEnum<ProofType> = IntEnum(value, this)

    public companion object {
        private val map = ProofType.values().associateBy { it.value }
        public fun enumOf(value: Int): IntEnum<ProofType> = IntEnum(value, map[value])
    }
}

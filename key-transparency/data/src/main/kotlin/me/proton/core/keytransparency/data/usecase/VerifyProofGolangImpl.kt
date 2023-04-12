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

package me.proton.core.keytransparency.data.usecase

import com.proton.gopenpgp.ktclient.Ktclient
import me.proton.core.keytransparency.domain.entity.Proof
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.usecase.GetKeyTransparencyParameters
import me.proton.core.keytransparency.domain.usecase.VerifyProof
import javax.inject.Inject
import com.proton.gopenpgp.ktclient.InsertionProof as GolangProof
import com.proton.gopenpgp.ktclient.Neighbours as GolangNeighbours

public class VerifyProofGolangImpl @Inject constructor(
    private val getKeyTransparencyParameters: GetKeyTransparencyParameters
) : VerifyProof {

    override operator fun invoke(
        email: String,
        signedKeyList: String?,
        proof: Proof,
        rootHash: String
    ) {
        val parameters = getKeyTransparencyParameters()
        checkProofType(proof)
        runCatching {
            Ktclient.verifyInsertionProof(
                email,
                signedKeyList ?: "",
                parameters.vrfPublicKey,
                rootHash,
                proof.toGolang()
            )
        }.getOrElse { throw KeyTransparencyException("Proof verification failed", it) }
    }

    private fun checkProofType(proof: Proof) {
        checkNotNull(proof.type.enum) { "Unknown proof type, was ${proof.type.value}" }
    }

    private fun Proof.toGolang(): GolangProof {
        return GolangProof(
            type.value.toLong(),
            revision?.toLong() ?: 0,
            vrfProof,
            neighbors.toGolangNeighbors()
        )
    }

    private fun Map<Int, String>.toGolangNeighbors(): GolangNeighbours {
        val neighbours = GolangNeighbours()
        require(this.size <= MAX_NEIGHBORS_SIZE) { "Neighbors list is too long" }
        this.forEach { (index, neighborHash) ->
            require(index < MAX_NEIGHBORS_SIZE) { "Neighbor index is out of bounds" }
            neighbours.setNeighbour(index.toLong(), neighborHash)
        }
        return neighbours
    }

    internal companion object {
        private const val MAX_NEIGHBORS_SIZE = 256
    }
}

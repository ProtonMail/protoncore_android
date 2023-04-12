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

import me.proton.core.keytransparency.domain.entity.Proof

/**
 * Verify that the address signed key list is
 * correctly included in the epoch's merkle tree.
 */
public interface VerifyProof {

    /**
     * Verify that the [proof]
     * confirms that the epoch
     * includes the given [signedKeyList] for the given email [email].
     *
     * @param email: the email address we want to check, the address must be normalized.
     * @param signedKeyList: the SKL of the address, or null if the address is not in KT
     * @param proof: the proof returned by the server
     * @param rootHash: the root hash of the merkle tree for the epoch.
     *
     * @throws KeyTransparencyException if the proof is invalid
     */
    public operator fun invoke(
        email: String,
        signedKeyList: String?,
        proof: Proof,
        rootHash: String
    )
}

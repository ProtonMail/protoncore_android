/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.auth.domain.crypto

import me.proton.core.auth.domain.entity.LoginInfo

interface SrpProofProvider {
    /**
     * Generates SRP Proofs for login.
     */
    fun generateSrpProofs(
        username: String,
        passphrase: ByteArray,
        info: LoginInfo
    ): SrpProofs
}

data class SrpProofs(
    val clientEphemeral: ByteArray,
    val clientProof: ByteArray,
    val expectedServerProof: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SrpProofs

        if (!clientEphemeral.contentEquals(other.clientEphemeral)) return false
        if (!clientProof.contentEquals(other.clientProof)) return false
        if (!expectedServerProof.contentEquals(other.expectedServerProof)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clientEphemeral.contentHashCode()
        result = 31 * result + clientProof.contentHashCode()
        result = 31 * result + expectedServerProof.contentHashCode()
        return result
    }
}

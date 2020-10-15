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

package me.proton.core.auth.presentation.srp

import com.proton.gopenpgp.srp.Auth
import com.proton.gopenpgp.srp.Proofs
import me.proton.core.auth.domain.crypto.SrpProofProvider
import me.proton.core.auth.domain.crypto.SrpProofs
import me.proton.core.auth.domain.entity.LoginInfo
import javax.inject.Inject

/**
 * Implementation of the [SrpProofProvider] interface which returns the generated proofs based on the SRP library.
 * @author Dino Kadrikj.
 */
class SrpProofProviderImpl @Inject constructor() : SrpProofProvider {

    /**
     * Generates SRP Proofs for login.
     */
    override fun generateSrpProofs(username: String, passphrase: ByteArray, info: LoginInfo): SrpProofs {

        val auth = Auth(
            info.version.toLong(),
            username,
            String(passphrase),
            info.salt,
            info.modulus,
            info.serverEphemeral
        )
        return auth.generateProofs(SRP_PROOF_BITS).toSrpProofs()
    }

    companion object {
        const val SRP_PROOF_BITS: Long = 2048
    }
}

internal fun Proofs.toSrpProofs(): SrpProofs = SrpProofs(
    clientEphemeral,
    clientProof,
    expectedServerProof
)

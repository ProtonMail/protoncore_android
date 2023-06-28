/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.crypto.android.srp

import android.util.Base64
import com.proton.gopenpgp.srp.Auth
import com.proton.gopenpgp.srp.Proofs
import com.proton.gopenpgp.srp.Srp
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.util.kotlin.DispatcherProvider
import java.security.SecureRandom

/**
 * Implementation of the [SrpCrypto] interface which returns the generated proofs based on the SRP library.
 */
class GOpenPGPSrpCrypto(
    private val dispatcherProvider: DispatcherProvider,
    private val saltGenerator: () -> ByteArray = {
        val salt = ByteArray(10)
        SecureRandom().nextBytes(salt)
        salt
    }
) : SrpCrypto {

    /**
     * Generates SRP Proofs for login.
     */
    override suspend fun generateSrpProofs(
        username: String,
        password: ByteArray,
        version: Long,
        salt: String,
        modulus: String,
        serverEphemeral: String
    ): SrpProofs = withContext(dispatcherProvider.Comp) {
        val auth = Auth(
            version,
            username,
            password,
            salt,
            modulus,
            serverEphemeral
        )
        auth.generateProofs(SRP_BIT_LENGTH.toLong()).toBase64SrpProofs()
    }

    override suspend fun calculatePasswordVerifier(
        username: String,
        password: ByteArray,
        modulusId: String,
        modulus: String
    ): me.proton.core.crypto.common.srp.Auth = withContext(dispatcherProvider.Comp) {
        val salt = saltGenerator()
        // newAuthForVerifier has the version hardcoded internally
        val auth = Srp.newAuthForVerifier(password, modulus, salt)
        val verifier = auth.generateVerifier(SRP_BIT_LENGTH.toLong())
        me.proton.core.crypto.common.srp.Auth(
            version = auth.version.toInt(),
            modulusId = modulusId,
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            verifier = Base64.encodeToString(verifier, Base64.NO_WRAP)
        )
    }

    companion object {
        const val SRP_BIT_LENGTH: Int = 2048
    }
}

internal fun Proofs.toBase64SrpProofs(): SrpProofs = SrpProofs(
    Base64.encodeToString(clientEphemeral, Base64.NO_WRAP),
    Base64.encodeToString(clientProof, Base64.NO_WRAP),
    Base64.encodeToString(expectedServerProof, Base64.NO_WRAP)
)

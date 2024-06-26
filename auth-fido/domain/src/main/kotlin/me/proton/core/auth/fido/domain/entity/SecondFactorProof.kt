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

package me.proton.core.auth.fido.domain.entity

public sealed class SecondFactorProof {

    /**
     * The 2FA code here is string, because the same will be used for recovery code (which happens on the same route).
     */
    public data class SecondFactorCode(
        val code: String
    ) : SecondFactorProof()

    public data class SecondFactorSignature(
        val keyHandle: String,
        val clientData: String,
        val signatureData: String
    ) : SecondFactorProof()

    public class Fido2(
        public val publicKeyOptions: Fido2PublicKeyCredentialRequestOptions,
        public val clientData: ByteArray,
        public val authenticatorData: ByteArray,
        public val signature: ByteArray,
        public val credentialID: ByteArray
    ) : SecondFactorProof()
}

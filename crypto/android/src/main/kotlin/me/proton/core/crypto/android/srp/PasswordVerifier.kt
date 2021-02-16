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

package me.proton.core.crypto.android.srp

import com.google.crypto.tink.subtle.Base64
import me.proton.core.crypto.common.srp.Auth
import java.math.BigInteger

/**
 * Handy BigInteger to ByteArray calculator, needed for password calculations.
 */
class PasswordVerifier(
    private val calculator: BigIntegerCalculator = BigIntegerCalculator(),
    private val version: Int,
    private val modulusId: String,
    private val salt: ByteArray
) {

    /**
     * Generates password Auth based on previously generated verifier (based on modulus and bit length).
     */
    fun generateAuth(bitLength: Int, modulus: ByteArray, hashedPassword: ByteArray): Auth {
        val generator = BigInteger.valueOf(2)
        val hashedPasswordBigInteger = calculator.toBI(hashedPassword)
        val modulusBigInteger = calculator.toBI(modulus)
        val generatedPasswordBigInteger = generator.modPow(hashedPasswordBigInteger, modulusBigInteger)
        val verifier = calculator.fromBI(bitLength, generatedPasswordBigInteger)
        return Auth(version, modulusId, Base64.encode(salt), Base64.encode(verifier))
    }
}

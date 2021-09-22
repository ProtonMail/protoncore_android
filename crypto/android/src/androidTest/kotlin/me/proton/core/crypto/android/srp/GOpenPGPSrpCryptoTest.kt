/*
 * Copyright (c) 2021 Proton Technologies AG
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

import android.util.Base64
import com.proton.gopenpgp.srp.Srp
import me.proton.core.test.kotlin.assertEquals
import org.junit.Test
import kotlin.test.fail

internal class GOpenPGPSrpCryptoTest {

    private fun ByteArray.encodeBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private val crypto = GOpenPGPSrpCrypto(
        saltGenerator = { Base64.decode(testSalt, Base64.DEFAULT) }
    )

    private val testModulusId = "modulusId"

    private val testModulusClearSign = """
        -----BEGIN PGP SIGNED MESSAGE-----
        Hash: SHA256
    
        W2z5HBi8RvsfYzZTS7qBaUxxPhsfHJFZpu3Kd6s1JafNrCCH9rfvPLrfuqocxWPgWDH2R8neK7PkNvjxto9TStuY5z7jAzWRvFWN9cQhAKkdWgy0JY6ywVn22+HFpF4cYesHrqFIKUPDMSSIlWjBVmEJZ/MusD44ZT29xcPrOqeZvwtCffKtGAIjLYPZIEbZKnDM1Dm3q2K/xS5h+xdhjnndhsrkwm9U9oyA2wxzSXFL+pdfj2fOdRwuR5nW0J2NFrq3kJjkRmpO/Genq1UW+TEknIWAb6VzJJJA244K/H8cnSx2+nSNZO3bbo6Ys228ruV9A8m6DhxmS+bihN3ttQ==
        -----BEGIN PGP SIGNATURE-----
        Version: ProtonMail
        Comment: https://protonmail.com
    
        wl4EARYIABAFAlwB1j0JEDUFhcTpUY8mAAD8CgEAnsFnF4cF0uSHKkXa1GIa
        GO86yMV4zDZEZcDSJo0fgr8A/AlupGN9EdHlsrZLmTA1vhIx+rOgxdEff28N
        kvNM7qIK
        =q6vu
        -----END PGP SIGNATURE-----
    """.trimIndent()

    private val testSalt = "yKlc5/CvObfoiw=="

    private val testPassword = "abc123"

    private val testVerifier = "OoEdVkB+/6xEM7cf3JSCHk/GThK1hsjupLukh1rQGHPs2liU252+Qh+iXadBGMWI3o/D6EAa1Yz2X8MsR8/BUinXKkIEQH7OpRmxXAtIvCaWdtvNFbfj/bzw5kmwExL0M4jVwTL8wAReLzzHjoVm8knb+Mp/q+hEZhdauNkrBd9Et1biJwVe8lGdxPcsQVkZzpuKWhYQ6ZdjiAwcZTEobtUHUVX1kgqu509bTO8z34LcJqgkRinU3yZuUhVDDNlP8BVIFE+AoDirznF68erdGPb0YZyLwSQJd7A2Zdty+G1rE/Ht6yX1EFs7qbDJmHO8DdrzJR0nQ2f9LexqAORdlw=="

    private val testUsername = "username"

    @Test
    fun verifierGenerationTest() {
        // GIVEN
        val expectedVersion = 4
        // WHEN
        val auth = crypto.calculatePasswordVerifier(
            username = testUsername,
            password = testPassword.toByteArray(),
            modulusId = testModulusId,
            modulus = testModulusClearSign,
        )
        // THEN
        assertEquals(testModulusId, auth.modulusId) { "Modulus id doesn't match" }
        assertEquals(testSalt, auth.salt) { "Salt doesn't match" }
        assertEquals(testVerifier, auth.verifier) { "Verifier doesn't match" }
        assertEquals(expectedVersion, auth.version) { "Version doesn't match" }
    }

    @Test
    fun proofGenerationTest() {
        // GIVEN
        val server = Srp.newServerFromSigned(
            testModulusClearSign,
            Base64.decode(testVerifier, Base64.DEFAULT),
            GOpenPGPSrpCrypto.SRP_BIT_LENGTH.toLong()
        )
        val serverEphemeral = server.generateChallenge().encodeBase64()
        // WHEN
        val proofs = crypto.generateSrpProofs(
            testUsername,
            testPassword.toByteArray(),
            4,
            testSalt,
            testModulusClearSign,
            serverEphemeral,
        )
        // THEN
        val serverProof = runCatching {
            server.verifyProofs(proofs.clientEphemeral, proofs.clientProof)
        }.getOrElse {
            fail("Failed to verify the proof: $it")
        }
        assertEquals(
            serverProof.encodeBase64(),
            proofs.expectedServerProof.encodeBase64()
        ) { "server proof didn't match" }
    }
}

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

import com.google.crypto.tink.subtle.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import java.math.BigInteger
import kotlin.test.assertEquals

/**
 * @author Dino Kadrikj.
 */
class PasswordVerifierTest {

    private lateinit var passwordVerifier: PasswordVerifier
    private val biCalculator = mockk<BigIntegerCalculator>(relaxed = true)

    private val testVersion = 1
    private val testModulusId = "test-modulusId"
    private val testSalt = "test-salt"
    private val testModulus = "test-modulus"
    private val testModulusBI: Long = 2
    private val testPassword = "test-password"
    private val testPasswordBI: Long = 1
    private val testBitLength = 1

    @Test
    fun `generating verifier works correctly`() {
        // GIVEN
        passwordVerifier = PasswordVerifier(biCalculator, testVersion, testModulusId, testSalt.toByteArray())
        every { biCalculator.toBI(testPassword.toByteArray()) } returns BigInteger.valueOf(testPasswordBI)
        every { biCalculator.toBI(testModulus.toByteArray()) } returns BigInteger.valueOf(testModulusBI)
        // WHEN
        passwordVerifier.generateAuth(testBitLength, testModulus.toByteArray(), testPassword.toByteArray())
        // THEN
        val bitLengthArgument = slot<Int>()
        val bigIntegerArgument = slot<BigInteger>()
        verify { biCalculator.fromBI(capture(bitLengthArgument), capture(bigIntegerArgument)) }

        assertEquals(testBitLength, bitLengthArgument.captured)
        assertEquals(BigInteger.valueOf(0), bigIntegerArgument.captured)
    }

    @Test
    fun `generating verifier works correctly second test`() {
        // GIVEN
        passwordVerifier = PasswordVerifier(biCalculator, testVersion, testModulusId, testSalt.toByteArray())
        every { biCalculator.toBI(testPassword.toByteArray()) } returns BigInteger.valueOf(5)
        every { biCalculator.toBI(testModulus.toByteArray()) } returns BigInteger.valueOf(3)
        // WHEN
        passwordVerifier.generateAuth(testBitLength, testModulus.toByteArray(), testPassword.toByteArray())
        // THEN
        val bitLengthArgument = slot<Int>()
        val bigIntegerArgument = slot<BigInteger>()
        verify { biCalculator.fromBI(capture(bitLengthArgument), capture(bigIntegerArgument)) }

        assertEquals(testBitLength, bitLengthArgument.captured)
        assertEquals(BigInteger.valueOf(2), bigIntegerArgument.captured)
    }

    @Test
    fun `generating Auth works correctly`() {
        // GIVEN
        passwordVerifier = PasswordVerifier(biCalculator, testVersion, testModulusId, testSalt.toByteArray())
        every { biCalculator.toBI(testPassword.toByteArray()) } returns BigInteger.valueOf(testPasswordBI)
        every { biCalculator.toBI(testModulus.toByteArray()) } returns BigInteger.valueOf(testModulusBI)
        every { biCalculator.fromBI(testBitLength, BigInteger.valueOf(0)) } returns "test".toByteArray()
        // WHEN
        val auth =
            passwordVerifier.generateAuth(
                testBitLength,
                testModulus.toByteArray(),
                testPassword.toByteArray()
            )
        // THEN
        assertEquals(testVersion, auth.version)
        assertEquals(testModulusId, auth.modulusId)
        assertEquals(Base64.encode(testSalt.toByteArray()), auth.salt)
        assertEquals(Base64.encode("test".toByteArray()), auth.verifier)

    }
}

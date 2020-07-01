// Public APIs
@file:Suppress("unused")

package ch.protonmail.libs.crypto

import ch.protonmail.libs.crypto.utils.internal.toBigInteger
import ch.protonmail.libs.crypto.utils.internal.toByteArray
import java.math.BigInteger
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

object SrpClient {

    @Throws(NoSuchAlgorithmException::class)
    fun generateProofs(
        bitLength: Int,
        modulusRepr: ByteArray,
        serverEphemeralRepr: ByteArray,
        hashedPasswordRepr: ByteArray
    ): Proofs {
        if (modulusRepr.size * 8 != bitLength || serverEphemeralRepr.size * 8 != bitLength || hashedPasswordRepr.size * 8 != bitLength) {
            TODO("Proper exception")
        }

        val modulus = modulusRepr.toBigInteger()
        val serverEphemeral = serverEphemeralRepr.toBigInteger()
        val hashedPassword = hashedPasswordRepr.toBigInteger()

        if (modulus.bitLength() != bitLength) {
            TODO("Proper exception")
        }

        val generator = BigInteger.valueOf(2)

        val multiplier =
            PasswordUtils.expandHash(generator.toByteArray(bitLength) + modulusRepr).toBigInteger()
                .mod(modulus)
        val modulusMinusOne = modulus.clearBit(0)

        if (multiplier <= BigInteger.ONE || multiplier >= modulusMinusOne) {
            TODO("Proper exception")
        }

        if (serverEphemeral <= BigInteger.ONE || serverEphemeral >= modulusMinusOne) {
            TODO("Proper exception")
        }

        if (!modulus.isProbablePrime(10) || !modulus.shiftRight(1).isProbablePrime(10)) {
            TODO("Proper exception")
        }

        val random = SecureRandom()
        var clientSecret: BigInteger
        var clientEphemeral: BigInteger
        var scramblingParam: BigInteger
        do {
            do {
                clientSecret = BigInteger(bitLength, random)
            } while (clientSecret >= modulusMinusOne || clientSecret <= BigInteger.valueOf((bitLength * 2).toLong()))
            clientEphemeral = generator.modPow(clientSecret, modulus)
            scramblingParam =
                PasswordUtils.expandHash(clientEphemeral.toByteArray(bitLength) + serverEphemeralRepr)
                    .toBigInteger()

        } while (scramblingParam == BigInteger.ZERO) // Very unlikely

        var subtracted = serverEphemeral.subtract(
            generator.modPow(
                hashedPassword,
                modulus
            ).multiply(multiplier).mod(modulus)
        )
        if (subtracted < BigInteger.ZERO) {
            subtracted = subtracted.add(modulus)
        }
        val exponent =
            scramblingParam.multiply(hashedPassword).add(clientSecret).mod(modulusMinusOne)
        val sharedSession = subtracted.modPow(exponent, modulus)

        val clientEphemeralRepr = clientEphemeral.toByteArray(bitLength)
        val clientProof = PasswordUtils.expandHash(
            clientEphemeralRepr + serverEphemeralRepr + sharedSession.toByteArray(bitLength)
        )
        val serverProof = PasswordUtils.expandHash(
            clientEphemeralRepr + clientProof + sharedSession.toByteArray(bitLength)
        )

        return Proofs(clientEphemeralRepr, clientProof, serverProof)
    }

    fun generateVerifier(
        bitLength: Int,
        modulusRepr: ByteArray,
        hashedPasswordRepr: ByteArray
    ): ByteArray {
        val modulus = modulusRepr.toBigInteger()
        val generator = BigInteger.valueOf(2)
        val hashedPassword = hashedPasswordRepr.toBigInteger()

        return generator.modPow(hashedPassword, modulus).toByteArray(bitLength)
    }

    class Proofs(
        val clientEphemeral: ByteArray,
        val clientProof: ByteArray,
        val expectedServerProof: ByteArray
    )
}

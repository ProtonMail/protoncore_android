package ch.protonmail.libs.crypto.utils.internal

import java.math.BigInteger

/*
 * Utilities for `BigInteger`
 */

/** @return [BigInteger] from [ByteArray] */
internal fun ByteArray.toBigInteger(): BigInteger { // TODO test
    val reversed = copyOf(size).reversedArray()
    return BigInteger(1, reversed)
}

/** @return [ByteArray] from [BigInteger] */
internal fun BigInteger.toByteArray(bitLength: Int): ByteArray { // TODO test
    val twosComp = toByteArray().reversed()
    val output = ByteArray(bitLength / 8)
    System.arraycopy(twosComp, 0, output, 0, kotlin.math.min(twosComp.size, output.size))
    return output
}

package ch.protonmail.libs.crypto

import kotlin.experimental.or
import kotlin.experimental.xor

object ConstantTime {
    // We can't trust MessageDigest.isEqual since Apache Harmony's is not constant time
    // and OpenJDK's wasn't until SE 6 Update 17
    fun isEqual(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) {
            return false
        }

        var diff: Byte = 0
        for (i in a.indices) {
            diff = diff or (a[i] xor b[i])
        }

        return diff.toInt() == 0
    }

    fun encodeBase64(raw: ByteArray, pad: Boolean): String {
        return encodeBase64With(raw, StdBase64(), pad)
    }

    fun encodeBase64DotSlash(raw: ByteArray, pad: Boolean): String {
        return encodeBase64With(raw, DotSlashBase64(), pad)
    }

    private fun encodeBase64With(raw: ByteArray, encoder: Base64Encoder, pad: Boolean): String {
        val builder = StringBuilder()

        var i = 0
        while (i + 3 <= raw.size) {

            val d1 =                                        (raw[i    ] and 0xff) shr 2
            val d2 = 63 and ((raw[i    ] and 0xff) shl 4 or (raw[i + 1] and 0xff) shr 4)
            val d3 = 63 and ((raw[i + 1] and 0xff) shl 2 or (raw[i + 2] and 0xff) shr 6)
            val d4 = 63 and   raw[i + 2]

            builder.append(encoder.encode6Bits(d1))
            builder.append(encoder.encode6Bits(d2))
            builder.append(encoder.encode6Bits(d3))
            builder.append(encoder.encode6Bits(d4))
            i += 3
        }

        when (raw.size % 3) {
            0 -> { /* No padding necessary */ }
            1 -> {
                val d1 =         (raw[raw.size - 1] and 0xff) shr 2
                val d2 = 63 and ((raw[raw.size - 1] and 0xff) shl 4)

                builder.append(encoder.encode6Bits(d1))
                builder.append(encoder.encode6Bits(d2))
                if (pad) {
                    builder.append('=')
                    builder.append('=')
                }
            }
            2 -> {
                val d1 =                                               (raw[raw.size - 2] and 0xff) shr 2
                val d2 = 63 and ((raw[raw.size - 2] and 0xff) shl 4 or (raw[raw.size - 1] and 0xff) shr 4)
                val d3 = 63 and  (raw[raw.size - 1] and 0xff) shl 2

                builder.append(encoder.encode6Bits(d1))
                builder.append(encoder.encode6Bits(d2))
                builder.append(encoder.encode6Bits(d3))
                if (pad) {
                    builder.append('=')
                }
            }
        }
        return builder.toString()
    }

    private interface Base64Encoder {
        fun encode6Bits(bits: Int): Char
    }

    private class StdBase64 : Base64Encoder {
        // [A-Z][a-z][0-9]+/
        override fun encode6Bits(bits: Int): Char {
            // We proceed by a series of conditionals, done in a constant time way.

            // Comparisons are done via subtraction and arithmetic right shift,
            // as (bits - x) >> 16 is 0b000000... or 0b111111... if bits >= x
            // or < x, respectively. We don't need to clear out a full 16 bits,
            // but it's certainly safe and I think some processors can handle
            // power of two or at least byte-aligned shifts slightly faster.

            var ret = bits + '/'.toInt() - 63
            // if (bits < 63) { ret = '+' + bits - 62; }
            ret += ((bits - 63) shr 16) and (('+' - 62) - ('/'.toInt() - 63))
            // if (bits < 62) { ret = '0' + bits - 52; }
            ret += ((bits - 62) shr 16) and (('0' - 52) - ('+'.toInt() - 62))
            // if (bits < 52) { ret = 'a' + bits - 26; }
            ret += ((bits - 52) shr 16) and (('a' - 26) - ('0'.toInt() - 52))
            // if (bits < 26) { ret = 'A' + bits; }
            ret += ((bits - 26) shr 16) and (('A'     ) - ('a'.toInt() - 26))

            return ret.toChar()
        }
    }

    private class DotSlashBase64 : Base64Encoder {
        // ./[A-Z][a-z][0-9]
        override fun encode6Bits(bits: Int): Char {
            // See StdBase64 for implementation explanation

            var ret = bits + '0' - 54
            // if (bits < 54) { ret = 'a' + bits - 28; }
            ret += ((bits - 54) shr 16) and (('a' - 28) - ('0' - 54))
            // if (bits < 28) { ret = 'A' + bits - 2; }
            ret += ((bits - 28) shr 16) and (('A' -  2) - ('a' - 28))
            // The characters ./ are adjacent in ascii
            // if (bits < 2) { ret = '.' + bits; }
            ret += ((bits -  2) shr 16) and (('.'     ) - ('A' -  2))

            return ret.toChar()
        }
    }
}

private infix fun Byte.and(int: Int) = this.toInt() and int
private infix fun Int.and(byte: Byte) = this and byte.toInt()
private infix fun Int.and(char: Char) = this and char.toInt()
private operator fun Int.plus(char: Char) = this + char.toInt()
private operator fun Int.minus(char: Char) = this - char.toInt()

// Public APIs
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package ch.protonmail.libs.crypto

import org.apache.commons.codec.binary.Hex
import org.mindrot.jbcrypt.BCrypt
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object PasswordUtils {
    const val LAST_AUTH_VERSION = 4

    fun cleanUserName(username: String) =
        username.replace("[_.\\-]".toRegex(), "").toLowerCase(Locale.ROOT)

    @Throws(NoSuchAlgorithmException::class)
    fun expandHash(input: ByteArray): ByteArray {
        val output = ByteArray(2048 / 8)
        val digest = MessageDigest.getInstance("SHA-512")

        digest.update(input)
        digest.update(0.toByte())
        System.arraycopy(digest.digest(), 0, output, 0, 512 / 8)
        digest.reset()

        digest.update(input)
        digest.update(1.toByte())
        System.arraycopy(digest.digest(), 0, output, 512 / 8, 512 / 8)
        digest.reset()

        digest.update(input)
        digest.update(2.toByte())
        System.arraycopy(digest.digest(), 0, output, 1024 / 8, 512 / 8)
        digest.reset()

        digest.update(input)
        digest.update(3.toByte())
        System.arraycopy(digest.digest(), 0, output, 1536 / 8, 512 / 8)
        digest.reset()

        return output
    }

    private fun bCrypt(password: String, salt: String): String {
        val ret = BCrypt.hashpw(password, BCRYPT_PREFIX + salt)
        return "$2y$" + ret.substring(4)
    }

    /**
     * Hash password using [LAST_AUTH_VERSION]
     * @see hashPassword
     */
    fun hashPassword(password: String, salt: ByteArray, modulus: ByteArray) =
        hashPassword(LAST_AUTH_VERSION, password, "", salt, modulus)

    /**
     * Hash password using the given [authVersion]
     * @return [ByteArray]
     *
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    fun hashPassword(
        authVersion: Int,
        password: String,
        username: String,
        salt: ByteArray,
        modulus: ByteArray
    ): ByteArray {
        return when (authVersion) {
            4 -> hashPasswordVersion4(password, salt, modulus)
            3 -> hashPasswordVersion3(password, salt, modulus)
            2 -> hashPasswordVersion2(password, username, modulus)
            1 -> hashPasswordVersion1(password, username, modulus)
            0 -> hashPasswordVersion0(password, username, modulus)
            else -> throw IllegalArgumentException("Unsupported Auth Version")
        }
    }

    /**
     * Hash password using version 4
     * @return [ByteArray]
     *
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private fun hashPasswordVersion4(password: String, salt: ByteArray, modulus: ByteArray) =
        hashPasswordVersion3(password, salt, modulus)

    /**
     * Hash password using version 3
     * @return [ByteArray]
     *
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private fun hashPasswordVersion3(
        password: String,
        salt: ByteArray,
        modulus: ByteArray
    ): ByteArray {
        val encodedSalt =
            ConstantTime.encodeBase64DotSlash(salt + "proton".toByteArray(), false)
        return expandHash(bCrypt(password, encodedSalt).toByteArray() + modulus)
    }

    /**
     * Hash password using version 2
     * @return [ByteArray]
     *
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private fun hashPasswordVersion2(password: String, username: String, modulus: ByteArray) =
        hashPasswordVersion1(password, cleanUserName(username), modulus)

    /**
     * Hash password using version 1
     * @return [ByteArray]
     *
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private fun hashPasswordVersion1(
        password: String,
        username: String,
        modulus: ByteArray
    ): ByteArray {
        Charsets.UTF_8
        val salt = String(
            Hex.encodeHex(
                MessageDigest.getInstance("MD5").digest(
                    username.toLowerCase(Locale.ROOT).toByteArray()
                )
            )
        )
        return expandHash(bCrypt(password, salt).toByteArray() + modulus)
    }

    /**
     * Hash password using version 0
     * @return [ByteArray]
     *
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private fun hashPasswordVersion0(
        password: String,
        username: String,
        modulus: ByteArray
    ): ByteArray {
        val preHashed = MessageDigest.getInstance("SHA-512").digest(password.toByteArray())
        return hashPasswordVersion1(
            ConstantTime.encodeBase64(preHashed, true),
            username,
            modulus
        )
    }
}

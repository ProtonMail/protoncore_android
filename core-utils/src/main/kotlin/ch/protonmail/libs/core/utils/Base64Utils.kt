package ch.protonmail.libs.core.utils

import android.util.Base64
import android.util.Base64.DEFAULT
import java.io.InputStream

// region Encode
/**
 * @return [ByteArray] encoded to Base64 from the receiver [Byte]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun ByteArray.encodeBase64(flags: Int = DEFAULT): ByteArray =
    Base64.encode(this, flags)

/**
 * @return [ByteArray] encoded to Base64 from the receiver [InputStream]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun InputStream.encodeBase64(flags: Int = DEFAULT): ByteArray =
    readBytes().encodeBase64(flags)

/**
 * @return [ByteArray] encoded to Base64 from the receiver [String]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun String.encodeBase64(flags: Int = DEFAULT): ByteArray =
    Base64.encode(toByteArray(), flags)
// endregion

// region Encode to String
/**
 * @return [String] encoded to Base64 from the receiver [Byte]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun ByteArray.encodeToBase64String(flags: Int = DEFAULT): String =
    Base64.encodeToString(this, flags)

/**
 * @return [String] encoded to Base64 from the receiver [InputStream]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun InputStream.encodeToBase64String(flags: Int = DEFAULT): String =
    readBytes().encodeToBase64String(flags)

/**
 * @return [String] encoded to Base64 from the receiver [String]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun String.encodeToBase64String(flags: Int = DEFAULT): String =
    Base64.encodeToString(toByteArray(), flags)
// endregion

// region Decode
/**
 * @return [ByteArray] decoded from Base64 receiver [ByteArray]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun ByteArray.decodeBase64(flags: Int = DEFAULT): ByteArray =
    Base64.decode(this, flags)

/**
 * @return [ByteArray] decoded from Base64 receiver [InputStream]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun InputStream.decodeBase64(flags: Int = DEFAULT): ByteArray =
    Base64.decode(this.readBytes(), flags)

/**
 * @return [ByteArray] decoded from Base64 receiver [String]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun String.decodeBase64(flags: Int = DEFAULT): ByteArray =
    Base64.decode(this, flags)
// endregion

// region Decode to String
/**
 * @return [String] decoded from Base64 receiver [ByteArray]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun ByteArray.decodeStringFromBase64(flags: Int = DEFAULT): String =
    String(decodeBase64(flags))

/**
 * @return [String] decoded from Base64 receiver [InputStream]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun InputStream.decodeStringFromBase64(flags: Int = DEFAULT): String =
    String(decodeBase64(flags))

/**
 * @return [String] decoded from Base64 receiver [String]
 * This should be executed on a background thread in most of the cases, specially for large files
 */
fun String.decodeStringFromBase64(flags: Int = DEFAULT): String =
    String(decodeBase64(flags))
// endregion

// Public APIs
@file:Suppress("unused")

package ch.protonmail.libs.crypto.utils

import android.util.Base64

/*
 * Utils for encoding / decoding
 * Author: Davide Farella
 */

// region encoding
/** @return [ByteArray] encoded from receiver [ByteArray] */
fun ByteArray.encodeBase64(flags: Int = Base64.DEFAULT, offset: Int = 0, length: Int = size) =
    Base64.encode(this, offset, length, flags)!!

/** @return [String] encoded from receiver [ByteArray] */
fun ByteArray.encodeBase64String(flags: Int = Base64.DEFAULT, offset: Int = 0, length: Int = size) =
    Base64.encodeToString(this, offset, length, flags)!!
// endregion

// region decoding
/** @return [ByteArray] decoded from receiver [ByteArray] */
fun ByteArray.decodeBase64(flags: Int = Base64.DEFAULT, offset: Int = 0, length: Int = size) =
    Base64.decode(this, offset, length, flags)!!

/** @return [ByteArray] decoded from receiver [String] */
fun String.decodeBase64(flags: Int = Base64.DEFAULT) = Base64.decode(this, flags)!!
// endregion

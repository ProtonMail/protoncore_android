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

package me.proton.core.key.domain

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.Unarmored
import java.io.File
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

internal fun Armored.toByteArray() = toByteArray(Charsets.UTF_8)
internal fun Unarmored.fromByteArray() = toString(Charsets.UTF_8)

internal fun ByteArray.padKey(): ByteArray =
    // Make sure we have a 16 bytes key.
    fromByteArray().padEnd(16).take(16).toByteArray()

internal fun ByteArray.encrypt(key: ByteArray): ByteArray {
    val secretKeySpec: SecretKey = SecretKeySpec(key.padKey(), "AES")
    val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
    return Base64.getEncoder().encode(cipher.doFinal(this))
}

internal fun ByteArray.decrypt(key: ByteArray): ByteArray {
    val secretKeySpec: SecretKey = SecretKeySpec(key.padKey(), "AES")
    val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
    return cipher.doFinal(Base64.getDecoder().decode(this))
}

internal fun ByteArray.allEqual(element: Byte) = all { it == element }
internal fun getTempFile(filename: String) = File.createTempFile("$filename.", "")
internal fun ByteArray.getFile(filename: String): File = getTempFile(filename).apply { appendBytes(this@getFile) }

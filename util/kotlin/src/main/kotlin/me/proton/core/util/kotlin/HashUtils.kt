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

package me.proton.core.util.kotlin

import java.io.File
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun File.sha256() = HashUtils.sha256(this)
fun File.sha512() = HashUtils.sha512(this)

object HashUtils {

    fun sha512(input: String) = shaString("SHA-512", input)
    fun sha256(input: String) = shaString("SHA-256", input)

    fun sha512(input: File) = shaFile("SHA-512", input)
    fun sha256(input: File) = shaFile("SHA-256", input)

    fun hmacSha512(input: String, key: ByteArray) = hmacString("HmacSHA512", input, key)
    fun hmacSha256(input: String, key: ByteArray) = hmacString("HmacSHA256", input, key)

    fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    private fun shaString(type: String, input: String): String =
        MessageDigest.getInstance(type)
            .digest(input.toByteArray())
            .toHexString()

    private fun shaFile(type: String, input: File): String {
        val digest = MessageDigest.getInstance(type)
        input.inputStream().use {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes = it.read(buffer)
            while (bytes > 0) {
                digest.update(buffer, 0, bytes)
                bytes = it.read(buffer)
            }
        }
        return digest.digest().toHexString()
    }

    private fun hmacString(type: String, data: String, key: ByteArray): String {
        val mac = Mac.getInstance(type)
        mac.init(SecretKeySpec(key, type))
        return mac.doFinal(data.toByteArray()).toHexString()
    }
}

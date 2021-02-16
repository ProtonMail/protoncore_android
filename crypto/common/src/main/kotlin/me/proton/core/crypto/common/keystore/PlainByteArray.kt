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

package me.proton.core.crypto.common.keystore

import java.io.Closeable

/**
 * Plain/decrypted [ByteArray], implementing [Closeable] to clear memory after usage.
 */
data class PlainByteArray(
    val array: ByteArray
) : Closeable {

    override fun close() {
        array.fill(0)
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is EncryptedByteArray && array.contentEquals(other.array)

    override fun hashCode(): Int = array.contentHashCode()
}

/**
 * Wrap this [ByteArray] as [PlainByteArray], executes the given block function on the [PlainByteArray] and then
 * close it whether an exception is thrown or not.
 *
 * Note: The original [ByteArray] is unusable after this function return.
 *
 * @param block a function to process this Closeable resource.
 * @return the result of block function invoked on this resource.
 */
inline fun <R> ByteArray.use(block: (PlainByteArray) -> R): R = PlainByteArray(this).use { block(it) }

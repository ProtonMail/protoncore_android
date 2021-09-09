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

package me.proton.core.crypto.common.pgp

import me.proton.core.util.kotlin.HashUtils
import java.io.Closeable

/**
 * Key to hash data, unarmored, implementing [Closeable] to clear memory after usage.
 */
data class HashKey(
    val key: Unarmored,
    val status: VerificationStatus
) : Closeable {

    override fun close() {
        key.fill(0)
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is HashKey && key.contentEquals(other.key)

    override fun hashCode(): Int = key.contentHashCode()
}

fun HashKey.hmacSha256(input: String) = HashUtils.hmacSha256(input, key)
fun HashKey.hmacSha512(input: String) = HashUtils.hmacSha512(input, key)

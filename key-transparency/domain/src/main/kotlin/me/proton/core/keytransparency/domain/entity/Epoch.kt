/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.keytransparency.domain.entity

import me.proton.core.domain.type.IntEnum

/**
 * An epoch is a snapshot of all the keys
 * of proton addresses at a point in time.
 * The keys are hashed in a merkle tree.
 * And this merkle tree is then committed by the server,
 * by creating a TLS certificate with the epoch ID, the time and
 * the root of the merkle tree.
 */
public data class Epoch(
    val epochId: EpochId,
    val previousChainHash: String,
    val certificateChain: String,
    val certificateIssuer: IntEnum<CertificateIssuer>,
    val treeHash: String,
    val chainHash: String,
    val certificateTime: Long
)

/**
 * Certificates are issued by two different issuers.
 * ZeroSSL and Let's encrypt.
 */
public enum class CertificateIssuer(public val value: Int) {
    LetsEncrypt(0),
    ZeroSsl(1);

    public companion object {
        private val map = values().associateBy { it.value }
        public fun enumOf(value: Int): IntEnum<CertificateIssuer> = IntEnum(value, map[value])
    }
}


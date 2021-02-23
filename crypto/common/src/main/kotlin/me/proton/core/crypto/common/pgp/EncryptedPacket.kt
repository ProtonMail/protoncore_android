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

package me.proton.core.crypto.common.pgp

/**
 * Asymmetrically Encrypted key to decrypt [DataPacket], unarmored.
 */
typealias KeyPacket = Unarmored

/**
 * Symmetrically encrypted data with [KeyPacket], unarmored.
 */
typealias DataPacket = Unarmored

enum class PacketType {
    Key,
    Data,
}

/**
 * Encrypted [Unarmored] packet - usually extracted from [EncryptedMessage], see [split].
 */
data class EncryptedPacket(
    val packet: Unarmored,
    val type: PacketType
) {
    override fun equals(other: Any?): Boolean =
        this === other || other is EncryptedPacket && type == other.type && packet.contentEquals(other.packet)

    override fun hashCode(): Int = 31 * type.hashCode() + packet.contentHashCode()
}

/**
 * Extract the list of [EncryptedPacket] from a [EncryptedMessage].
 */
fun EncryptedMessage.split(pgpCrypto: PGPCrypto): List<EncryptedPacket> = pgpCrypto.getEncryptedPackets(this)

/**
 * @return first [EncryptedPacket.packet] of type [PacketType.Key].
 */
fun List<EncryptedPacket>.keyPacket() = first { it.type == PacketType.Key }.packet

/**
 * @return first [EncryptedPacket.packet] of type [PacketType.Data].
 */
fun List<EncryptedPacket>.dataPacket() = first { it.type == PacketType.Data }.packet

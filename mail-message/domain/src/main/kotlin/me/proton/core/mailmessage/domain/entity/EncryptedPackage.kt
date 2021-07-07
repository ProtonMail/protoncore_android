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

package me.proton.core.mailmessage.domain.entity

import me.proton.core.mailsettings.domain.entity.PackageType

data class EncryptedPackage(
    val addresses: Map<String, Address>,
    val mimeType: String,
    val body: String,
    val type: Int,
    val attachmentKeys: List<Key>? = null,
    val bodyKey: Key? = null,
) {

    sealed class Address(val packageType: PackageType, val signed: Boolean) {

        data class Internal(
            val bodyKeyPacket: String,
            val attachmentKeyPackets: List<String>
        ) : Address(PackageType.ProtonMail, true)

        object External : Address(PackageType.Cleartext, false)
    }

    data class Key(
        val key: String,
        val algorithm: String,
    )
}

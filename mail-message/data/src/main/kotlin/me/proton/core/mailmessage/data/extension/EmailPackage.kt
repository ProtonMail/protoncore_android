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

package me.proton.core.mailmessage.data.extension

import me.proton.core.mailmessage.data.api.request.EmailPackage
import me.proton.core.mailmessage.domain.entity.EncryptedPackage
import me.proton.core.util.kotlin.toInt

fun EncryptedPackage.toEmailPackage(): EmailPackage = EmailPackage(
    addresses = addresses.mapValues { entry ->
        when (val address = entry.value) {
            is EncryptedPackage.Address.Internal -> EmailPackage.Address(
                type = address.packageType.type,
                signature = address.signed.toInt(),
                bodyKeyPacket = address.bodyKeyPacket,
                attachmentKeyPackets = address.attachmentKeyPackets
            )
            is EncryptedPackage.Address.External -> EmailPackage.Address(
                type = address.packageType.type,
                signature = address.signed.toInt()
            )
        }
    },
    mimeType = mimeType,
    body = body,
    type = type,
    attachmentKeys = attachmentKeys?.map { EmailPackage.Key(it.key, it.algorithm) },
    bodyKey = bodyKey?.let { EmailPackage.Key(it.key, it.algorithm) }
)

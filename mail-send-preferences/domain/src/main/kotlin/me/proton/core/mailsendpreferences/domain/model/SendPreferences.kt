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

package me.proton.core.mailsendpreferences.domain.model

import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.mailsettings.domain.entity.MimeType
import me.proton.core.mailsettings.domain.entity.PackageType

/**
 * Set of user's preferences for sending emails to a given recipient. This is combined
 * from email settings, public key repository and Proton contact settings, including
 * pinned public keys for recipient email address.
 */
data class SendPreferences(
    val encrypt: Boolean,
    val sign: Boolean,
    val pgpScheme: PackageType,
    val mimeType: MimeType,
    val publicKey: PublicKey?
)
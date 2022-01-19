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

package me.proton.android.core.coreexample.utils

import ezvcard.VCard
import ezvcard.VCardVersion
import ezvcard.property.Email
import ezvcard.property.FormattedName
import ezvcard.property.Uid

fun createToBeSignedVCard(seedName: String): VCard {
    return VCard().apply {
        formattedName = FormattedName(seedName)
        addEmail(Email("$seedName@testmail.com").apply {
            group = "ITEM1"
        })
        uid = Uid.random()
        version = VCardVersion.V4_0
    }
}

fun createToBeEncryptedAndSignedVCard(seedName: String): VCard {
    return VCard().apply {
        addNote("confidential note about $seedName from UtcMillis: ${System.currentTimeMillis()}")
    }
}

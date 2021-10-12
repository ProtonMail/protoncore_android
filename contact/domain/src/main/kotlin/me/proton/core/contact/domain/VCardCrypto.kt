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

package me.proton.core.contact.domain

import ezvcard.VCard
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.signText
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.User

fun User.signContactCard(cryptoContext: CryptoContext, vCard: VCard): ContactCard.Signed {
    return useKeys(cryptoContext) {
        val vCardData = vCard.write()
        val vCardSignature = signText(vCardData)
        ContactCard.Signed(vCardData, vCardSignature)
    }
}

fun User.encryptAndSignContactCard(cryptoContext: CryptoContext, vCard: VCard): ContactCard.Encrypted {
    return useKeys(cryptoContext) {
        val vCardData = vCard.write()
        val encryptedVCardData = encryptText(vCardData)
        val vCardSignature = signText(vCardData)
        ContactCard.Encrypted(encryptedVCardData, vCardSignature)
    }
}

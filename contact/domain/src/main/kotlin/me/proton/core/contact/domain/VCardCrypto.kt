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

import ezvcard.Ezvcard
import ezvcard.VCard
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.DecryptedVCard
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.decryptText
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.signText
import me.proton.core.key.domain.verifyText

fun KeyHolderContext.signContactCard(vCard: VCard): ContactCard.Signed {
    val vCardData = vCard.write()
    val vCardSignature = signText(vCardData)
    return ContactCard.Signed(vCardData, vCardSignature)
}

fun KeyHolderContext.encryptAndSignContactCard(vCard: VCard): ContactCard.Encrypted {
    val vCardData = vCard.write()
    val encryptedVCardData = encryptText(vCardData)
    val vCardSignature = signText(vCardData)
    return ContactCard.Encrypted(encryptedVCardData, vCardSignature)
}

fun KeyHolderContext.decryptContactCard(contactCard: ContactCard): DecryptedVCard {
    return when (contactCard) {
        is ContactCard.ClearText -> decryptContactCardClearText(contactCard)
        is ContactCard.Encrypted -> decryptContactCardEncrypted(contactCard)
        is ContactCard.Signed -> decryptContactCardSigned(contactCard)
    }
}

private fun KeyHolderContext.decryptContactCardClearText(contactCard: ContactCard.ClearText): DecryptedVCard {
    return DecryptedVCard(
        card = Ezvcard.parse(contactCard.data).first(),
        status = VerificationStatus.NotSigned
    )
}

private fun KeyHolderContext.decryptContactCardSigned(contactCard: ContactCard.Signed): DecryptedVCard {
    val verified = verifyText(contactCard.data, contactCard.signature)
    return DecryptedVCard(
        card = Ezvcard.parse(contactCard.data).first(),
        status = VerificationStatus.Success.takeIf { verified } ?: VerificationStatus.Failure
    )
}

private fun KeyHolderContext.decryptContactCardEncrypted(contactCard: ContactCard.Encrypted): DecryptedVCard {
    val decryptedText = decryptText(contactCard.data)
    val signature = contactCard.signature
    val status = when {
        signature == null -> VerificationStatus.NotSigned
        verifyText(decryptedText, signature) -> VerificationStatus.Success
        else -> VerificationStatus.Failure
    }
    return DecryptedVCard(
        card = Ezvcard.parse(decryptedText).first(),
        status = status
    )
}

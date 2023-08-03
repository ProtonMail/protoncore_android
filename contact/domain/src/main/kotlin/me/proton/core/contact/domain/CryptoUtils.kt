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

package me.proton.core.contact.domain

import ezvcard.VCard
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicKey

interface CryptoUtils {

    sealed class PinnedKeysOrError {
        data class Success(val pinnedPublicKeys: List<PublicKey>) : PinnedKeysOrError()

        sealed class Error : PinnedKeysOrError() {
            object NoKeysAvailable : Error()
            object NoEmailInVCard : Error()
        }
    }

    sealed class PinnedKeysPurpose {
        object VerifyingSignature : PinnedKeysPurpose()
        object Encrypting : PinnedKeysPurpose()
    }

    fun extractPinnedPublicKeys(
        purpose: PinnedKeysPurpose,
        vCardEmail: String,
        vCard: VCard,
        publicAddress: PublicAddress,
        cryptoContext: CryptoContext
    ): PinnedKeysOrError

}
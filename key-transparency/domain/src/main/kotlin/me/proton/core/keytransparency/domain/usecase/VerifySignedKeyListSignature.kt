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

package me.proton.core.keytransparency.domain.usecase

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.getVerifiedTimestampOfText
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheckNotNull
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

internal class VerifySignedKeyListSignature @Inject constructor(
    private val cryptoContext: CryptoContext
) {

    operator fun invoke(address: PublicAddress, skl: PublicSignedKeyList): Long = invoke(address.publicKeyRing(), skl)

    operator fun invoke(address: UserAddress, skl: PublicSignedKeyList): Long =
        invoke(address.publicKeyRing(cryptoContext), skl)

    operator fun invoke(keyRing: PublicKeyRing, skl: PublicSignedKeyList): Long {
        val sklData = requireNotNull(skl.data) { "The signed key list's data was null" }
        val sklSignature = requireNotNull(skl.signature) { "Signed key list had no signature" }
        return keyTransparencyCheckNotNull(
            keyRing.getVerifiedTimestampOfText(cryptoContext, sklData, sklSignature)
        ) { "Failed to verify the signature of the SKL" }
    }
}

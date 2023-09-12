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
import me.proton.core.contact.domain.CryptoUtils.PinnedKeysOrError
import me.proton.core.contact.domain.CryptoUtils.PinnedKeysPurpose
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.getFingerprintOrNull
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.Recipient
import me.proton.core.key.domain.entity.key.isCompromised
import me.proton.core.key.domain.entity.key.isObsolete
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoUtilsImpl @Inject constructor() : CryptoUtils {

    override fun extractPinnedPublicKeys(
        purpose: PinnedKeysPurpose,
        vCardEmail: String,
        vCard: VCard,
        publicAddress: PublicAddress,
        cryptoContext: CryptoContext
    ): PinnedKeysOrError {

        val isInternal = publicAddress.recipient == Recipient.Internal
        val publicAddressKey = publicAddress.keys.firstOrNull { it.publicKey.isPrimary }

        val propertyGroup = vCard.getGroupForEmail(vCardEmail) ?: return PinnedKeysOrError.Error.NoEmailInVCard

        val vCardPublicKeys = vCard.getKeysForGroup(propertyGroup)

        val pinnedPublicKeys = vCardPublicKeys.mapNotNull { pinnedPublicKeyBytes ->
            val armoredPinnedPublicKey = cryptoContext.pgpCrypto.getArmored(pinnedPublicKeyBytes, PGPHeader.PublicKey)
            extractPinnedPublicKey(
                cryptoContext,
                armoredPinnedPublicKey,
                publicAddress,
                isInternal,
                purpose,
                publicAddressKey
            )
        }

        return if (pinnedPublicKeys.isEmpty()) {
            return PinnedKeysOrError.Error.NoKeysAvailable
        } else {
            when (purpose) {
                PinnedKeysPurpose.Encrypting -> PinnedKeysOrError.Success(listOf(pinnedPublicKeys.first()))
                PinnedKeysPurpose.VerifyingSignature -> PinnedKeysOrError.Success(pinnedPublicKeys)
            }
        }
    }

    private fun extractPinnedPublicKey(
        cryptoContext: CryptoContext,
        pinnedPublicKey: Armored,
        publicAddress: PublicAddress,
        isInternal: Boolean,
        purpose: PinnedKeysPurpose,
        publicAddressKey: PublicAddressKey?
    ): PublicKey? {

        val pinnedKeyFingerprint = cryptoContext.pgpCrypto.getFingerprintOrNull(pinnedPublicKey)

        val matchingPublicAddressKey = publicAddress.keys.find {
            cryptoContext.pgpCrypto.getFingerprintOrNull(it.publicKey.key) == pinnedKeyFingerprint
        }

        return if (
            // pinned key is somehow malformed
            (pinnedKeyFingerprint == null) ||
            // pinned key is not in the public key repository
            (isInternal && matchingPublicAddressKey == null) ||
            (matchingPublicAddressKey?.flags?.isCompromised() == true) ||
            // pinned key is obsolete (invalid for encrypting but we can still verify)
            (matchingPublicAddressKey?.flags?.isObsolete() == true && purpose == PinnedKeysPurpose.Encrypting) ||
            (cryptoContext.pgpCrypto.isKeyExpired(pinnedPublicKey)) ||
            (cryptoContext.pgpCrypto.isKeyRevoked(pinnedPublicKey)) ||
            (publicAddressKey != null && (publicAddressKey.flags.isObsolete() || publicAddressKey.flags.isCompromised()))
        ) {
            null
        } else {
            PublicKey(pinnedPublicKey, isPrimary = true, isActive = true, canEncrypt = true, canVerify = true)
        }
    }

}
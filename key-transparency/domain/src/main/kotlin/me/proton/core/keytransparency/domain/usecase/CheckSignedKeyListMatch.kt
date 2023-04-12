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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKeyFlags
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.fingerprint
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheck
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheckNotNull
import me.proton.core.keytransparency.domain.extensions.toPublicAddress
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.util.kotlin.deserializeList
import me.proton.core.util.kotlin.toInt
import javax.inject.Inject

internal class CheckSignedKeyListMatch @Inject constructor(
    private val cryptoContext: CryptoContext
) {

    operator fun invoke(userAddress: UserAddress, skl: PublicSignedKeyList) = this.invoke(
        userAddress.toPublicAddress(cryptoContext),
        skl
    )

    operator fun invoke(address: PublicAddress, skl: PublicSignedKeyList) {
        val sklData = keyTransparencyCheckNotNull(skl.data) { "Signed key list's data is null" }
        keyTransparencyCheck(address.matchKeys(sklData)) { "Signed key list's data doesn't match the keys" }
    }

    private fun PublicAddress.matchKeys(sklData: String): Boolean =
        this.getKeyList(cryptoContext).isTheSame(sklData.parseKeyList())

    @Serializable
    private data class KeyListElement(
        @SerialName("Fingerprint")
        val fingerprint: String,
        @SerialName("SHA256Fingerprints")
        val sha256Fingerprints: List<String>,
        @SerialName("Flags")
        val flags: PublicAddressKeyFlags,
        @SerialName("Primary")
        val primary: Int
    ) {
        override fun equals(other: Any?): Boolean = this === other ||
            other is KeyListElement &&
            other.fingerprint.lowercase() == this.fingerprint.lowercase() &&
            other.sha256Fingerprints.map { it.lowercase() }.sorted() ==
            this.sha256Fingerprints.map { it.lowercase() }.sorted() &&
            other.flags == this.flags &&
            other.primary == this.primary

        override fun hashCode(): Int {
            var result = fingerprint.lowercase().hashCode()
            result = 31 * result + sha256Fingerprints.map { it.lowercase() }.sorted().hashCode()
            result = 31 * result + flags
            result = 31 * result + primary
            return result
        }
    }

    private fun List<KeyListElement>.isTheSame(other: List<KeyListElement>) =
        this.sortedBy { it.fingerprint } == other.sortedBy { it.fingerprint }

    private fun PublicAddress.getKeyList(context: CryptoContext): List<KeyListElement> = keys
        .filter { it.publicKey.isActive }
        .map { key ->
            KeyListElement(
                fingerprint = key.publicKey.fingerprint(context),
                sha256Fingerprints =
                context.pgpCrypto.getJsonSHA256Fingerprints(key.publicKey.key).deserializeList(),
                flags = key.flags,
                primary = key.publicKey.isPrimary.toInt()
            )
        }

    private fun String.parseKeyList() = this.deserializeList<KeyListElement>()
}

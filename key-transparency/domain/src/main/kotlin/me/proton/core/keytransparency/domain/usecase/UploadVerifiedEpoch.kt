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
import me.proton.core.crypto.common.pgp.SignatureContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.VerifiedEpoch
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData.Companion.toJson
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

internal class UploadVerifiedEpoch @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val userRepository: UserRepository,
    private val keyTransparencyRepository: KeyTransparencyRepository
) {

    suspend operator fun invoke(
        userId: UserId,
        addressId: AddressId,
        verifiedEpochData: VerifiedEpochData
    ) {
        val verifiedEpochDataSerialized = verifiedEpochData.toJson()
        val verifiedEpochSignature = userRepository.getUser(userId).useKeys(cryptoContext) {
            signData(
                verifiedEpochDataSerialized.toByteArray(Charsets.UTF_8),
                signatureContext = SignatureContext(
                    value = Constants.KT_VERIFIED_EPOCH_SIGNATURE_CONTEXT,
                    isCritical = true
                )
            )
        }
        val verifiedEpochBundle = VerifiedEpoch(
            data = verifiedEpochDataSerialized,
            signature = verifiedEpochSignature
        )
        keyTransparencyRepository.uploadVerifiedEpoch(userId, addressId, verifiedEpochBundle)
    }
}

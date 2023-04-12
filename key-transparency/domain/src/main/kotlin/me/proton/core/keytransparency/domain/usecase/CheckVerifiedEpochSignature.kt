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
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.verifyData
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.VerifiedEpoch
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheck
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

internal class CheckVerifiedEpochSignature @Inject constructor(
    private val userRepository: UserRepository,
    private val cryptoContext: CryptoContext
) {
    suspend operator fun invoke(userId: UserId, verifiedEpoch: VerifiedEpoch) {
        userRepository.getUser(userId).publicKeyRing(cryptoContext).run {
            keyTransparencyCheck(
                verifyData(
                    cryptoContext,
                    verifiedEpoch.data.toByteArray(Charsets.UTF_8),
                    verifiedEpoch.signature,
                    verificationContext = VerificationContext(
                        value = Constants.KT_VERIFIED_EPOCH_SIGNATURE_CONTEXT,
                        required = VerificationContext.ContextRequirement.Required.Always
                    )
                )
            ) { "Couldn't verify the verified epoch" }
        }
    }
}

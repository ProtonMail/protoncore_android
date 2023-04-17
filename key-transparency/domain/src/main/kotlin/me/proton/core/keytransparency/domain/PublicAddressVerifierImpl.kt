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

package me.proton.core.keytransparency.domain

import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.repository.PublicAddressVerifier
import me.proton.core.keytransparency.domain.usecase.IsKeyTransparencyEnabled
import me.proton.core.keytransparency.domain.usecase.LogKeyTransparency
import me.proton.core.keytransparency.domain.usecase.VerifyPublicAddress
import javax.inject.Inject

public class PublicAddressVerifierImpl @Inject internal constructor(
    private val verifyPublicAddressUsecase: VerifyPublicAddress,
    private val logKeyTransparency: LogKeyTransparency,
    private val isKeyTransparencyEnabled: IsKeyTransparencyEnabled
) : PublicAddressVerifier {

    override suspend fun verifyPublicAddress(userId: UserId, address: PublicAddress) {
        if (isKeyTransparencyEnabled(userId)) {
            KeyTransparencyLogger.d("Verifying public keys of address")
            val verifiedState = verifyPublicAddressUsecase(userId, address)
            logKeyTransparency.logPublicAddressVerification(verifiedState)
        }
    }

}

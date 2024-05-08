/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.userrecovery.domain.usecase

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Based64Encoded
import me.proton.core.crypto.common.pgp.EncryptedSignature
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.generateRandomBytes
import me.proton.core.key.domain.getBase64EncodedNoWrap
import me.proton.core.key.domain.signText
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

/**
 * Generate and sign a new user primary recovery secret.
 */
class GetRecoverySecret @Inject constructor(
    private val userManager: UserManager,
    private val cryptoContext: CryptoContext
) {
    suspend operator fun invoke(
        userId: UserId
    ): Pair<Based64Encoded, EncryptedSignature> {
        return userManager.getUser(userId).useKeys(cryptoContext) {
            val token = generateRandomBytes(32)
            val secret = getBase64EncodedNoWrap(token)
            val signature = signText(secret)
            secret to signature
        }
    }
}

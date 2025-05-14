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
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyText
import me.proton.core.user.domain.repository.UserRemoteDataSource
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

class GetExistingVerifiedRecoverySecret @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userRepository: UserRepository
) {
    /**
     * @return A non-null recovery secret (in case the signature has been verified), null if verification failed.
     * @throws IllegalArgumentException In case the primary recovery secret or signature is missing.
     */
    suspend operator fun invoke(
        userId: UserId
    ): Based64Encoded? {
        // The `recoverySecret` is only present when fetching from remote:
        val user = userRemoteDataSource.fetch(userId)
        userRepository.updateUser(user)

        val primaryKey = user.keys.firstOrNull { it.privateKey.isPrimary }
        val recoverySecret = requireNotNull(primaryKey?.recoverySecret)
        val recoverySecretSignature = requireNotNull(primaryKey?.recoverySecretSignature)

        val isSignatureVerified = user.useKeys(cryptoContext) {
            verifyText(recoverySecret, recoverySecretSignature)
        }

        return when {
            isSignatureVerified -> recoverySecret
            else -> null
        }
    }
}

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

package me.proton.core.auth.domain.usecase.scopes

import com.google.crypto.tink.subtle.Base64
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

class ObtainLockedScope @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val context: CryptoContext
) {
    suspend operator fun invoke(
        userId: UserId,
        username: String,
        password: EncryptedString
    ): Boolean {
        // first we obtain auth info
        val authInfo = authRepository.getAuthInfo(
            userId = userId,
            username = username
        )

        password.decrypt(context.keyStoreCrypto).toByteArray().use {
            val clientProofs = context.srpCrypto.generateSrpProofs(
                username = username,
                password = it.array,
                version = authInfo.version.toLong(),
                salt = authInfo.salt,
                modulus = authInfo.modulus,
                serverEphemeral = authInfo.serverEphemeral
            )
            return userRepository.unlockUserForLockedScope(
                userId,
                Base64.encode(clientProofs.clientEphemeral),
                Base64.encode(clientProofs.clientProof),
                authInfo.srpSession
            )
        }
    }
}

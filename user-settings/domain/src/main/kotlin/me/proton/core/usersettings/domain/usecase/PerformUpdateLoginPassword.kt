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

package me.proton.core.usersettings.domain.usecase

import me.proton.core.auth.domain.ClientSecret
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import javax.inject.Inject

class PerformUpdateLoginPassword @Inject constructor(
    context: CryptoContext,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val userSettingsRepository: UserSettingsRepository,
    @ClientSecret private val clientSecret: String
) {
    private val keyStore = context.keyStoreCrypto
    private val srp = context.srpCrypto

    suspend operator fun invoke(
        userId: UserId,
        password: EncryptedString,
        newPassword: EncryptedString,
        secondFactorCode: String = ""
    ): UserSettings {
        val user = userRepository.getUser(userId)
        val username = requireNotNull(user.name ?: user.email)

        val loginInfo = authRepository.getLoginInfo(
            username = username,
            clientSecret = clientSecret
        )
        val modulus = authRepository.randomModulus()

        password.decrypt(keyStore).toByteArray().use { decryptedPassword ->
            newPassword.decrypt(keyStore).toByteArray().use { decryptedNewPassword ->
                val clientProofs: SrpProofs = srp.generateSrpProofs(
                    username = username,
                    password = decryptedPassword.array,
                    version = loginInfo.version.toLong(),
                    salt = loginInfo.salt,
                    modulus = loginInfo.modulus,
                    serverEphemeral = loginInfo.serverEphemeral
                )
                val auth = srp.calculatePasswordVerifier(
                    username = username,
                    password = decryptedNewPassword.array,
                    modulusId = modulus.modulusId,
                    modulus = modulus.modulus
                )
                return userSettingsRepository.updateLoginPassword(
                    sessionUserId = userId,
                    srpProofs = clientProofs,
                    srpSession = loginInfo.srpSession,
                    secondFactorCode = secondFactorCode,
                    auth = auth
                )
            }
        }
    }
}

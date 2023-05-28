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

import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.user.domain.extension.nameNotNull
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import javax.inject.Inject

class PerformUpdateRecoveryEmail @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val srpCrypto: SrpCrypto,
    private val keyStoreCrypto: KeyStoreCrypto,
) {
    suspend operator fun invoke(
        sessionUserId: SessionUserId,
        newRecoveryEmail: String,
        password: EncryptedString,
        secondFactorCode: String = ""
    ): UserSettings {
        val user = userRepository.getUser(sessionUserId)
        val username = user.nameNotNull()
        val account = accountRepository.getAccountOrNull(sessionUserId)
        val loginInfo = authRepository.getAuthInfoSrp(requireNotNull(account?.sessionId), username)
        password.decrypt(keyStoreCrypto).toByteArray().use { decryptedPassword ->
            val clientProofs: SrpProofs = srpCrypto.generateSrpProofs(
                username = username,
                password = decryptedPassword.array,
                version = loginInfo.version.toLong(),
                salt = loginInfo.salt,
                modulus = loginInfo.modulus,
                serverEphemeral = loginInfo.serverEphemeral
            )
            return userSettingsRepository.updateRecoveryEmail(
                sessionUserId = sessionUserId,
                email = newRecoveryEmail,
                srpProofs = clientProofs,
                srpSession = loginInfo.srpSession,
                secondFactorCode = secondFactorCode
            )
        }
    }
}

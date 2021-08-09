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

import com.google.crypto.tink.subtle.Base64
import me.proton.core.auth.domain.ClientSecret
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decryptWith
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.usersettings.domain.entity.PrivateKey
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import javax.inject.Inject

class PerformUpdateMailboxPassword @Inject constructor(
    private val authRepository: AuthRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val srpCrypto: SrpCrypto,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val cryptoContext: CryptoContext,
    @ClientSecret private val clientSecret: String
) {
    suspend operator fun invoke(
        sessionUserId: SessionUserId,
        twoPasswordMode: Boolean,
        username: String,
        loginPassword: EncryptedString,
        newPassword: EncryptedString,
        secondFactorCode: String = ""
    ) {
        val loginInfo = authRepository.getLoginInfo(
            username = username,
            clientSecret = clientSecret
        )
        val modulus = authRepository.randomModulus()
        val keySalt = cryptoContext.pgpCrypto.generateNewKeySalt()

        loginPassword.decryptWith(keyStoreCrypto).toByteArray().use { decryptedLoginPassword ->
            newPassword.decryptWith(keyStoreCrypto).toByteArray().use { decryptedNewPassword ->
                val clientProofs: SrpProofs = srpCrypto.generateSrpProofs(
                    username = username,
                    password = decryptedLoginPassword.array,
                    version = loginInfo.version.toLong(),
                    salt = loginInfo.salt,
                    modulus = loginInfo.modulus,
                    serverEphemeral = loginInfo.serverEphemeral
                )

                val auth = if (!twoPasswordMode) srpCrypto.calculatePasswordVerifier(
                    username = username,
                    password = decryptedNewPassword.array,
                    modulusId = modulus.modulusId,
                    modulus = modulus.modulus
                ) else null
                // todo: calculate the keys
                val keys: List<PrivateKey> = emptyList()
                val userKeys: List<PrivateKey> = emptyList()
                val organizationKey = ""
                cryptoContext.pgpCrypto.getPassphrase(decryptedNewPassword.array, keySalt).use { passphrase ->
                    userSettingsRepository.updateKeysForPasswordChange(
                        sessionUserId = sessionUserId,
                        clientEphemeral = Base64.encode(clientProofs.clientEphemeral),
                        clientProof = Base64.encode(clientProofs.clientProof),
                        srpSession = loginInfo.srpSession,
                        secondFactorCode = secondFactorCode,
                        auth = auth,
                        keys = keys,
                        userKeys = userKeys,
                        organizationKey = organizationKey
                    )
                }
            }
        }
    }
}

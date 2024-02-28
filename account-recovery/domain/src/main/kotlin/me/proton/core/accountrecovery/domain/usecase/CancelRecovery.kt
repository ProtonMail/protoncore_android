/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.accountrecovery.domain.usecase

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountrecovery.domain.repository.AccountRecoveryRepository
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.extension.suspend
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

public class CancelRecovery @Inject constructor(
    private val accountManager: AccountManager,
    private val accountRecoveryRepository: AccountRecoveryRepository,
    private val authRepository: AuthRepository,
    private val cryptoContext: CryptoContext,
    private val eventManagerProvider: EventManagerProvider,
) {
    public suspend operator fun invoke(
        password: EncryptedString,
        userId: UserId
    ) {
        val account = requireNotNull(accountManager.getAccount(userId).firstOrNull()) {
            "Could not find account for user $userId."
        }
        val sessionId = requireNotNull(account.sessionId) {
            "Missing sessionId for user $userId."
        }
        val username = requireNotNull(account.username) {
            "Missing username for user $userId."
        }
        return invoke(
            username = username,
            password = password,
            sessionId = sessionId,
            userId = userId
        )
    }

    private suspend operator fun invoke(
        username: String,
        password: EncryptedString,
        sessionId: SessionId,
        userId: UserId
    ) {
        val authInfo = authRepository.getAuthInfoSrp(
            sessionId = sessionId,
            username = username
        )

        val clientProofs = password.decrypt(cryptoContext.keyStoreCrypto).toByteArray().use {
            cryptoContext.srpCrypto.generateSrpProofs(
                username = username,
                password = it.array,
                version = authInfo.version.toLong(),
                salt = authInfo.salt,
                modulus = authInfo.modulus,
                serverEphemeral = authInfo.serverEphemeral
            )
        }

        return eventManagerProvider.suspend(EventManagerConfig.Core(userId)) {
            accountRecoveryRepository.cancelRecoveryAttempt(
                clientProofs,
                authInfo.srpSession,
                userId
            )
        }
    }
}

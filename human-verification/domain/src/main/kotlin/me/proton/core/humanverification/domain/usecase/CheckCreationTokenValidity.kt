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

package me.proton.core.humanverification.domain.usecase

import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.humanverification.domain.repository.UserVerificationRepository
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

class CheckCreationTokenValidity @Inject constructor(
    private val clientIdProvider: ClientIdProvider,
    private val userVerificationRepository: UserVerificationRepository,
    private val humanVerificationRepository: HumanVerificationRepository
) {
    suspend operator fun invoke(sessionId: SessionId?, token: String, tokenType: TokenType) {
        userVerificationRepository.checkCreationTokenValidity(
            sessionId = sessionId,
            token = token,
            tokenType = tokenType,
        )

        val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = sessionId))

        humanVerificationRepository.insertHumanVerificationDetails(
            details = HumanVerificationDetails(
                clientId = clientId,
                verificationMethods = listOf(
                    when (tokenType) {
                        TokenType.EMAIL -> VerificationMethod.EMAIL
                        TokenType.SMS -> VerificationMethod.PHONE
                        TokenType.CAPTCHA -> VerificationMethod.CAPTCHA
                        TokenType.PAYMENT -> VerificationMethod.PAYMENT
                    }
                ),
                state = HumanVerificationState.HumanVerificationSuccess,
                tokenType = tokenType.value,
                tokenCode = token
            )
        )
    }
}

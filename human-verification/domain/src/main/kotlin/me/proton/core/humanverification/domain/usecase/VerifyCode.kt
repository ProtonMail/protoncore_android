/*
 * Copyright (c) 2020 Proton Technologies AG
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

import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.repository.HumanVerificationRemoteRepository
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

/**
 * Use case for sending the verification code (token) to the API for verification.
 * Depends on [HumanVerificationRemoteRepository] which is responsible for contacting the API via network connection.
 */
class VerifyCode @Inject constructor(
    private val humanVerificationRemoteRepository: HumanVerificationRemoteRepository
) {

    /**
     * Sends the verification code (token) to the API.
     *
     * @param tokenType the type of verification method used.
     * @param verificationCode the verification code (token)
     */
    suspend operator fun invoke(sessionId: SessionId, tokenType: String, verificationCode: String): VerificationResult =
        humanVerificationRemoteRepository.verifyCode(sessionId, tokenType, verificationCode)
}

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

import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.exception.InvalidValidationOptionException
import me.proton.core.humanverification.domain.repository.HumanVerificationRemoteRepository
import javax.inject.Inject

/**
 * Use case for sending the verification code to any destination chosen by the user, based on the
 * verification type (method), which can be captcha, email, sms etc..
 *
 * Depends on the [HumanVerificationRemoteRepository] for contacting the API.
 *
 * @param humanVerificationRemoteRepository the remote repository interface implementation.
 *
 * @author Dino Kadrikj.
 */
class ResendVerificationCodeToDestination @Inject
constructor(private val humanVerificationRemoteRepository: HumanVerificationRemoteRepository) {

    /**
     * Send the verification code (token) to the API. This is an alternative function with slightly
     * different signature than the [invoke] function.
     *
     * @param tokenType the verification method (type)
     * @param destination destination (could be email address or phone number) depending on the
     * verification type (method) from the [tokenType]
     *
     * @throws InvalidValidationOptionException if the verification type (method) does not support
     * sending the verification code (currently supported [TokenType.EMAIL] and [TokenType.SMS]
     */
    suspend operator fun invoke(tokenType: TokenType, destination: String): VerificationResult {
        return when (tokenType) {
            TokenType.SMS -> humanVerificationRemoteRepository.sendVerificationCodePhoneNumber(
                destination
            )
            TokenType.EMAIL -> humanVerificationRemoteRepository.sendVerificationCodeEmailAddress(
                destination
            )
            else -> throw InvalidValidationOptionException("Invalid verification type selected")
        }
    }
}

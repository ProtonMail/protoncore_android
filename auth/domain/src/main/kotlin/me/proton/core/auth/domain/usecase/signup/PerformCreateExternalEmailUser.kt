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

package me.proton.core.auth.domain.usecase.signup

import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.domain.useFlow
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.runWithObservability
import me.proton.core.user.domain.entity.CreateUserType
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

class PerformCreateExternalEmailUser @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val srpCrypto: SrpCrypto,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val challengeManager: ChallengeManager,
    private val challengeConfig: SignupChallengeConfig,
    private val observabilityManager: ObservabilityManager
) {

    suspend operator fun invoke(
        email: String,
        password: EncryptedString,
        referrer: String?,
        metricData: ((Result<UserId>) -> ObservabilityData)? = null
    ): UserId {
        require(email.isNotBlank()) { "Email must not be empty." }

        val modulus = authRepository.randomModulus()

        password.decrypt(keyStoreCrypto).toByteArray().use { decryptedPassword ->
            val auth = srpCrypto.calculatePasswordVerifier(
                username = email,
                password = decryptedPassword.array,
                modulusId = modulus.modulusId,
                modulus = modulus.modulus
            )
            return challengeManager.useFlow(challengeConfig.flowName) { frames ->
                userRepository.runWithObservability(observabilityManager, metricData) {
                    createExternalEmailUser(
                        email = email,
                        password = password,
                        referrer = referrer,
                        type = CreateUserType.Normal,
                        auth = auth,
                        frames = frames
                    ).userId
                }
            }
        }
    }
}

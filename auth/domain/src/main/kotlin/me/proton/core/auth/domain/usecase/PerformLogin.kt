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

package me.proton.core.auth.domain.usecase

import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.domain.useFlow
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.runWithObservability
import javax.inject.Inject

/**
 * Performs the login request along with the login info request which is always preceding it.
 */
class PerformLogin @Inject constructor(
    private val authRepository: AuthRepository,
    private val srpCrypto: SrpCrypto,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val challengeManager: ChallengeManager,
    private val challengeConfig: LoginChallengeConfig,
    private val observabilityManager: ObservabilityManager
) {
    suspend operator fun invoke(
        username: String,
        password: EncryptedString,
        loginMetricData: ((HttpApiStatus) -> ObservabilityData)? = null
    ): SessionInfo {
        val loginInfo = authRepository.getAuthInfo(
            sessionId = null,
            username = username,
        )
        password.decrypt(keyStoreCrypto).toByteArray().use { decryptedPassword ->
            val srpProofs: SrpProofs = srpCrypto.generateSrpProofs(
                username = username,
                password = decryptedPassword.array,
                version = loginInfo.version.toLong(),
                salt = loginInfo.salt,
                modulus = loginInfo.modulus,
                serverEphemeral = loginInfo.serverEphemeral
            )
            return challengeManager.useFlow(challengeConfig.flowName) { frames ->
                authRepository.runWithObservability(observabilityManager, loginMetricData) {
                    performLogin(
                        frames = frames,
                        username = username,
                        srpProofs = srpProofs,
                        srpSession = loginInfo.srpSession
                    )
                }
            }
        }
    }
}

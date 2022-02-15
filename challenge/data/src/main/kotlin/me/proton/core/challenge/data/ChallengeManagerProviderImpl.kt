/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.challenge.data

import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.domain.ChallengeManagerConfig
import me.proton.core.challenge.domain.ChallengeManagerProvider
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.util.kotlin.exhaustive
import java.util.UUID
import javax.inject.Singleton

@Singleton
class ChallengeManagerProviderImpl(
    private val clientIdProvider: ClientIdProvider,
    private val challengeManagerFactory: ChallengeManagerFactory
) : ChallengeManagerProvider {

    private val managers = mutableMapOf<ChallengeManagerConfig, ChallengeManager>()

    override suspend fun get(config: ChallengeManagerConfig): ChallengeManager {
        return managers.getOrPut(config) {
            val clientId = when (config) {
                is ChallengeManagerConfig.Login,
                is ChallengeManagerConfig.SignUp -> clientIdProvider.getClientId(sessionId = null)
            }.exhaustive

            val challengeManager = challengeManagerFactory.create(UUID.randomUUID(), clientId!!)
            challengeManager.removeFrames()
            challengeManager
        }
    }
}

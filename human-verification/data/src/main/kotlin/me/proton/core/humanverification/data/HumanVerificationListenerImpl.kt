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

package me.proton.core.humanverification.data

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.network.domain.humanverification.HumanVerificationAvailableMethods
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationListener.HumanVerificationResult

class HumanVerificationListenerImpl(
    private val humanVerificationRepository: HumanVerificationRepository
) : HumanVerificationListener {

    private val verificationProcessObservers = mutableMapOf<ClientId, Channel<HumanVerificationResult>>()

    override suspend fun onHumanVerificationNeeded(
        clientId: ClientId,
        methods: HumanVerificationAvailableMethods
    ): HumanVerificationResult {
        humanVerificationRepository.insertHumanVerificationDetails(
            HumanVerificationDetails(
                clientId = clientId,
                verificationMethods = methods.verificationMethods,
                verificationToken = methods.verificationToken,
                state = HumanVerificationState.HumanVerificationNeeded
            )
        )
        val state = humanVerificationRepository.onHumanVerificationStateChanged(initialState = true)
            .filter { it.clientId == clientId }
            .map { it.state }
            .filter {
                it in listOf(
                    HumanVerificationState.HumanVerificationSuccess,
                    HumanVerificationState.HumanVerificationFailed
                )
            }
            .first()
        return if (state == HumanVerificationState.HumanVerificationSuccess)
            HumanVerificationResult.Success
        else
            HumanVerificationResult.Failure
    }

    override suspend fun onHumanVerificationInvalid(clientId: ClientId) {
        humanVerificationRepository.updateHumanVerificationState(
            clientId = clientId,
            state = HumanVerificationState.HumanVerificationInvalid
        )
    }

    override suspend fun awaitHumanVerificationProcessFinished(clientId: ClientId): HumanVerificationResult =
        ensureProcessObserver(clientId).receive()

    override suspend fun notifyHumanVerificationProcessFinished(clientId: ClientId) {
        val details = humanVerificationRepository.getHumanVerificationDetails(clientId)
        if (details?.state == null) return
        val result = if (details.state == HumanVerificationState.HumanVerificationSuccess)
            HumanVerificationResult.Success
        else
            HumanVerificationResult.Failure
        ensureProcessObserver(clientId).trySend(result)
    }

    private fun ensureProcessObserver(clientId: ClientId): Channel<HumanVerificationResult> =
        verificationProcessObservers.getOrPut(clientId) { Channel(1) }
}

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

import kotlinx.coroutines.flow.Flow
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.HvResultTotal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HumanVerificationManagerImpl @Inject constructor(
    private val humanVerificationProvider: HumanVerificationProvider,
    private val humanVerificationListener: HumanVerificationListener,
    private val humanVerificationRepository: HumanVerificationRepository,
    private val observabilityManager: ObservabilityManager
) : HumanVerificationManager, HumanVerificationWorkflowHandler,
    HumanVerificationProvider by humanVerificationProvider,
    HumanVerificationListener by humanVerificationListener {

    override fun onHumanVerificationStateChanged(initialState: Boolean): Flow<HumanVerificationDetails> =
        humanVerificationRepository.onHumanVerificationStateChanged(initialState = initialState)

    override suspend fun addDetails(details: HumanVerificationDetails) {
        humanVerificationRepository.insertHumanVerificationDetails(details = details)
    }

    override suspend fun clearDetails(clientId: ClientId) {
        humanVerificationRepository.deleteHumanVerificationDetails(clientId)
    }

    override suspend fun handleHumanVerificationSuccess(clientId: ClientId, tokenType: String, tokenCode: String) {
        humanVerificationRepository.updateHumanVerificationState(
            clientId = clientId,
            state = HumanVerificationState.HumanVerificationSuccess,
            tokenType = tokenType,
            tokenCode = tokenCode
        )
        observabilityManager.enqueue(HvResultTotal(HvResultTotal.Status.success))
    }

    override suspend fun handleHumanVerificationFailed(clientId: ClientId) {
        humanVerificationRepository.updateHumanVerificationState(
            clientId = clientId,
            state = HumanVerificationState.HumanVerificationFailed,
        )
        observabilityManager.enqueue(HvResultTotal(HvResultTotal.Status.failure))
    }

    override suspend fun handleHumanVerificationCancelled(clientId: ClientId) {
        humanVerificationRepository.updateHumanVerificationState(
            clientId = clientId,
            state = HumanVerificationState.HumanVerificationCancelled,
        )
        observabilityManager.enqueue(HvResultTotal(HvResultTotal.Status.cancellation))
    }
}

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

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.network.domain.humanverification.HumanVerificationApiDetails
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.session.ClientId
import me.proton.core.network.domain.session.HumanVerificationListener
import me.proton.core.network.domain.session.HumanVerificationListener.HumanVerificationResult

class HumanVerificationListenerImpl(
    private val humanVerificationRepository: HumanVerificationRepository
) : HumanVerificationListener {

    /**
     * Called when a Human Verification Workflow is needed for a User.
     *
     * Implementation of this function should suspend until a [HumanVerificationResult] is returned.
     *
     * Any consecutive API call made without an updated and valid [HumanVerificationDetails] with headers will return
     * the same error and then will be queued until this function return. After, queued calls will be retried.
     */
    override suspend fun onHumanVerificationNeeded(
        clientId: ClientId,
        details: HumanVerificationApiDetails
    ): HumanVerificationResult {
        humanVerificationRepository.insertHumanVerificationDetails(
            details = HumanVerificationDetails.fromApiDetails(clientId, details)
        )
        val state = humanVerificationRepository.onHumanVerificationStateChanged(initialState = true)
            .filter { it.clientId == clientId }
            .map { it.state }
            .filter { it == HumanVerificationState.HumanVerificationSuccess || it == HumanVerificationState.HumanVerificationFailed }
            .first()
        return when (state) {
            null -> HumanVerificationResult.Failure
            HumanVerificationState.HumanVerificationSuccess -> HumanVerificationResult.Success
            else -> HumanVerificationResult.Failure
        }
    }

    override suspend fun onHumanVerificationFailed(clientId: ClientId) =
        humanVerificationRepository.updateHumanVerificationCompleted(clientId)

    override suspend fun onHumanVerificationPassed(clientId: ClientId) {
        humanVerificationRepository.updateHumanVerificationCompleted(clientId)
    }

    override suspend fun onExternalAccountHumanVerificationNeeded(
        clientId: ClientId,
        details: HumanVerificationDetails
    ) = humanVerificationRepository.insertHumanVerificationDetails(details = details)

    override suspend fun onExternalAccountHumanVerificationDone() = humanVerificationRepository.clear()
}

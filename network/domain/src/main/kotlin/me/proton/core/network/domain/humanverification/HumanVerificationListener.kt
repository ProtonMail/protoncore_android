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

package me.proton.core.network.domain.humanverification

import me.proton.core.network.domain.client.ClientId

interface HumanVerificationListener {

    sealed class HumanVerificationResult {
        object Success : HumanVerificationResult()
        object Failure : HumanVerificationResult()
    }

    /**
     * Called when a Human Verification Workflow is needed for a [ClientId].
     *
     * Implementation of this function should suspend until a [HumanVerificationResult] is returned.
     *
     * Any consecutive API call made without an updated and valid [HumanVerificationDetails] will return the same error
     * and then will be queued until this function return. After, queued calls will be retried.
     */
    suspend fun onHumanVerificationNeeded(
        clientId: ClientId,
        methods: HumanVerificationAvailableMethods
    ): HumanVerificationResult

    /**
     * Called when a current Human Verification token/code is invalid for a [ClientId].
     *
     * Implementation of this function should remove the associated token for this [ClientId] before returning.
     *
     * Any consecutive API call made without an updated token/code will return the same error.
     */
    suspend fun onHumanVerificationInvalid(
        clientId: ClientId,
    )
}

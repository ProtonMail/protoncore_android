/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.network.domain.deviceverification

import me.proton.core.network.domain.session.SessionId

/**
 * An interface for a listener that handles device verification events.
 */
interface DeviceVerificationListener {

    /**
     * A sealed class that represents the result of a device verification operation.
     * This class is used to indicate whether the verification was successful or failed.
     */
    sealed class DeviceVerificationResult {
        object Success : DeviceVerificationResult()
        object Failure : DeviceVerificationResult()
    }

    /**
     * Called when a device verification workflow is needed.
     * This method suspends the current coroutine until the verification is completed.
     *
     * @param methods a [DeviceVerificationMethods] object that contains the challenge type and payload.
     * @return a [DeviceVerificationResult] indicating whether the verification was successful or failed.
     */
    suspend fun onDeviceVerification(sessionId: SessionId, methods: DeviceVerificationMethods): DeviceVerificationResult
}
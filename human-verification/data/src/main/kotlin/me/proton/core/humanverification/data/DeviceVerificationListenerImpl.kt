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

package me.proton.core.humanverification.data

import me.proton.core.crypto.common.srp.SrpChallenge
import me.proton.core.network.domain.deviceverification.ChallengeType.Argon2
import me.proton.core.network.domain.deviceverification.ChallengeType.Ecdlp
import me.proton.core.network.domain.deviceverification.ChallengeType.WASM
import me.proton.core.network.domain.deviceverification.DeviceVerificationListener
import me.proton.core.network.domain.deviceverification.DeviceVerificationListener.DeviceVerificationResult
import me.proton.core.network.domain.deviceverification.DeviceVerificationMethods
import me.proton.core.network.domain.deviceverification.DeviceVerificationProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject
import kotlin.time.measureTimedValue

/**
 * An implementation of [DeviceVerificationListener] that uses a [DeviceVerificationProvider]
 * to handle device verification events.
 *
 * @property deviceVerificationProvider a data provider that cache the device verification details.
 */
class DeviceVerificationListenerImpl @Inject constructor(
    private val deviceVerificationProvider: DeviceVerificationProvider,
    private val srpChallenge: SrpChallenge,
) : DeviceVerificationListener {

    /**
     * Called when a device verification workflow is needed.
     * This method suspends the current coroutine until the verification is completed.
     *
     * @param methods a [DeviceVerificationMethods] object that contains the challenge type and payload.
     * @return a [DeviceVerificationResult] indicating whether the verification was successful or failed.
     */
    override suspend fun onDeviceVerification(
        sessionId: SessionId,
        methods: DeviceVerificationMethods
    ): DeviceVerificationResult {
        // Try to get solved challenge from cache through provider
        val cached = deviceVerificationProvider.getSolvedChallenge(methods.challengePayload)
        if (cached != null) {
            deviceVerificationProvider.setSolvedChallenge(sessionId, methods.challengePayload, cached)
            return DeviceVerificationResult.Success
        }

        val solvedChallenge = measureTimedValue {
            try {
                when (methods.challengeType.enum) {
                    WASM -> srpChallenge.argon2PreimageChallenge(methods.challengePayload)
                    Ecdlp -> srpChallenge.ecdlpChallenge(methods.challengePayload)
                    Argon2 -> srpChallenge.argon2PreimageChallenge(methods.challengePayload)
                    null -> throw UnsupportedOperationException("Unsupported challenge type: ${methods.challengeType.value}.")
                }
            } catch (e: Exception) {
                CoreLogger.e(LogTag.SRP_CHALLENGE_ERROR, e)
                // If an exception occurs, return a failure result.
                return DeviceVerificationResult.Failure
            }
        }

        // If the challenge is not solved, return a failure result.
        if (solvedChallenge.value.isEmpty()) {
            return DeviceVerificationResult.Failure
        }

        // Add duration to solved challenge.
        val solved = "${solvedChallenge.value}, ${solvedChallenge.duration.inWholeMilliseconds}"


        // Use the deviceVerificationProvider to save the solved challenge
        deviceVerificationProvider.setSolvedChallenge(sessionId, methods.challengePayload, solved)

        return DeviceVerificationResult.Success
    }
}
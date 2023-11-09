/*
 * Copyright (c) 2023 Proton AG
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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.srp.SrpChallenge
import me.proton.core.domain.type.IntEnum
import me.proton.core.network.domain.deviceverification.ChallengeType
import me.proton.core.network.domain.deviceverification.DeviceVerificationListener
import me.proton.core.network.domain.deviceverification.DeviceVerificationMethods
import me.proton.core.network.domain.deviceverification.DeviceVerificationProvider
import me.proton.core.network.domain.session.SessionId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceVerificationListenerImplTest {
    @MockK
    private lateinit var deviceVerificationProvider: DeviceVerificationProvider

    @MockK
    private lateinit var srpChallenge: SrpChallenge

    private lateinit var tested: DeviceVerificationListenerImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = DeviceVerificationListenerImpl(deviceVerificationProvider, srpChallenge)
    }

    @Test
    fun `successful verification`() = runTest {
        // GIVEN
        val sessionId = SessionId("session_id")
        val payload = "payload"
        val methods = DeviceVerificationMethods(ChallengeType.enumOf(2), payload)

        coEvery { deviceVerificationProvider.getSolvedChallenge(payload) } returns null
        coJustRun { deviceVerificationProvider.setSolvedChallenge(sessionId, payload, any()) }
        coEvery { srpChallenge.argon2PreimageChallenge(payload) } returns "argon2Result"

        // WHEN
        val result = tested.onDeviceVerification(sessionId, methods)

        // THEN
        assertEquals(DeviceVerificationListener.DeviceVerificationResult.Success, result)
        coVerify { deviceVerificationProvider.setSolvedChallenge(sessionId, payload, any()) }
    }

    @Test
    fun `cached verification`() = runTest {
        // GIVEN
        val sessionId = SessionId("session_id")
        val payload = "payload"
        val methods = DeviceVerificationMethods(ChallengeType.enumOf(1), payload)
        val solvedChallenge = "solved-challenge"
        coEvery { deviceVerificationProvider.getSolvedChallenge(payload) } returns solvedChallenge
        coJustRun { deviceVerificationProvider.setSolvedChallenge(sessionId, payload, any()) }

        // WHEN
        val result = tested.onDeviceVerification(sessionId, methods)

        // THEN
        assertEquals(DeviceVerificationListener.DeviceVerificationResult.Success, result)
        coVerify {
            deviceVerificationProvider.setSolvedChallenge(
                sessionId,
                payload,
                solvedChallenge
            )
        }
    }

    @Test
    fun `failed verification`() = runTest {
        // GIVEN
        val sessionId = SessionId("session_id")
        val payload = "payload"
        val methods = DeviceVerificationMethods(ChallengeType.enumOf(2), payload)

        coEvery { deviceVerificationProvider.getSolvedChallenge(payload) } returns null
        coEvery { srpChallenge.ecdlpChallenge(payload) } throws Throwable("Failed")

        // WHEN
        val result = tested.onDeviceVerification(sessionId, methods)

        // THEN
        assertEquals(DeviceVerificationListener.DeviceVerificationResult.Failure, result)
        coVerify(exactly = 0) {
            deviceVerificationProvider.setSolvedChallenge(
                sessionId,
                payload,
                any()
            )
        }
    }

    @Test
    fun `empty challenge result`() = runTest {
        // GIVEN
        val sessionId = SessionId("session_id")
        val payload = "payload"
        val methods = DeviceVerificationMethods(ChallengeType.enumOf(1), payload)

        coEvery { deviceVerificationProvider.getSolvedChallenge(payload) } returns null
        coEvery { srpChallenge.argon2PreimageChallenge(payload) } returns ""

        // WHEN
        val result = tested.onDeviceVerification(sessionId, methods)

        // THEN
        assertEquals(DeviceVerificationListener.DeviceVerificationResult.Failure, result)
        coVerify(exactly = 0) {
            deviceVerificationProvider.setSolvedChallenge(
                sessionId,
                payload,
                any()
            )
        }
    }

    @Test
    fun `unknown verification method`() = runTest {
        // GIVEN
        val sessionId = SessionId("session_id")
        val payload = "payload"
        val methods = DeviceVerificationMethods(IntEnum(-1, null), payload)

        coEvery { deviceVerificationProvider.getSolvedChallenge(payload) } returns null

        // WHEN
        val result = tested.onDeviceVerification(sessionId, methods)

        // THEN
        assertEquals(DeviceVerificationListener.DeviceVerificationResult.Failure, result)
        coVerify(exactly = 0) {
            deviceVerificationProvider.setSolvedChallenge(
                sessionId,
                payload,
                any()
            )
        }
    }
}

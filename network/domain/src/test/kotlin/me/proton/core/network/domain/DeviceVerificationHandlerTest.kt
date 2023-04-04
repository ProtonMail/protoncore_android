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

package me.proton.core.network.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.network.domain.deviceverification.ChallengeType
import me.proton.core.network.domain.deviceverification.DeviceVerificationListener
import me.proton.core.network.domain.deviceverification.DeviceVerificationMethods
import me.proton.core.network.domain.handlers.DeviceVerificationNeededHandler
import me.proton.core.network.domain.session.ResolvedSession
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.network.domain.session.getResolvedSession
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

/**
 * Tests for Device Verification domain handler.
 */
class DeviceVerificationHandlerTest {

    private val sessionId = SessionId("sessionId")
    private val session = Session(
        sessionId,
        "TokenOK",
        "TokenOK",
        listOf("ok1", "ok2"),
    )
    private val sessionIdProvider = mockk<SessionProvider>()
    private val deviceVerificationListener = mockk<DeviceVerificationListener>()

    private val apiBackend = mockk<ApiBackend<Any>>()

    @BeforeTest
    fun beforeTest() {
        coEvery { sessionIdProvider.getResolvedSession(any()) } returns ResolvedSession.Found.Authenticated(session)
        coEvery { sessionIdProvider.getSession(any()) } returns session
    }

    @Test
    fun `test device verification called`() = runTest {
        val challengeType = ChallengeType.enumOf(1)
        val deviceVerificationMethods = DeviceVerificationMethods(challengeType, "TestPayload")
        val apiResult = ApiResult.Error.Http(
            422,
            "Device Verification required",
            ApiResult.Error.ProtonData(
                9002,
                "Device Verification required",
                null,
                null,
                deviceVerificationMethods
            )
        )

        coEvery {
            deviceVerificationListener.onDeviceVerification(
                sessionId,
                any()
            )
        } returns DeviceVerificationListener.DeviceVerificationResult.Success

        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")

        val deviceVerificationHandler =
            DeviceVerificationNeededHandler<Any>(sessionId, sessionIdProvider, deviceVerificationListener)

        val result = deviceVerificationHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) {
            deviceVerificationListener.onDeviceVerification(sessionId, deviceVerificationMethods)
        }
    }

    @Test
    fun `test device verification called but retry api failed`() = runTest {
        val challengeType = ChallengeType.enumOf(1)
        val deviceVerificationMethods = DeviceVerificationMethods(challengeType, "TestPayload")
        val apiResult = ApiResult.Error.Http(
            422,
            "device Verification required",
            ApiResult.Error.ProtonData(
                9002,
                "device Verification required",
                null,
                null,
                deviceVerificationMethods
            )
        )

        coEvery {
            deviceVerificationListener.onDeviceVerification(
                sessionId,
                any()
            )
        } returns DeviceVerificationListener.DeviceVerificationResult.Success

        coEvery { apiBackend.invoke<Any>(any()) } returns apiResult

        val deviceVerificationHandler =
            DeviceVerificationNeededHandler<Any>(sessionId, sessionIdProvider, deviceVerificationListener)
        val result = deviceVerificationHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) {
            deviceVerificationListener.onDeviceVerification(sessionId, deviceVerificationMethods)
        }
    }

    @Test
    fun `test device verification not called on other errors`() = runTest {
        val apiResult = ApiResult.Error.Http(
            422,
            "Some error",
            ApiResult.Error.ProtonData(
                9000,
                "Some error"
            )
        )

        val deviceVerificationHandler =
            DeviceVerificationNeededHandler<Any>(sessionId, sessionIdProvider, deviceVerificationListener)

        val result = deviceVerificationHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            deviceVerificationListener.onDeviceVerification(sessionId, any())
        }
    }

    @Test
    fun `test no proton data does not crash the handler`() = runTest {
        val apiResult = ApiResult.Error.Http(
            422,
            "Some error",
            null
        )

        val deviceVerificationHandler =
            DeviceVerificationNeededHandler<Any>(sessionId, sessionIdProvider, deviceVerificationListener)

        val result = deviceVerificationHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            deviceVerificationListener.onDeviceVerification(sessionId, any())
        }
    }

    @Test
    fun `test connectivity error does not invoke device verification`() = runTest {
        val apiResult = ApiResult.Error.Connection(
            false
        )

        val deviceVerificationHandler =
            DeviceVerificationNeededHandler<Any>(sessionId, sessionIdProvider, deviceVerificationListener)

        val result = deviceVerificationHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            deviceVerificationListener.onDeviceVerification(sessionId, any())
        }
    }
}

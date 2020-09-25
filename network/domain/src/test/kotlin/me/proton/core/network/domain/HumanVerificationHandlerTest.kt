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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.network.domain.handlers.HumanVerificationHandler
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import org.junit.Test
import kotlin.test.assertNotNull

/**
 * Tests for Human Verification domain handler.
 */
@ExperimentalCoroutinesApi
class HumanVerificationHandlerTest {

    private val sessionId: SessionId = SessionId("id")
    private val sessionListener = mockk<SessionListener>(relaxed = true)
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)

    val scope = CoroutineScope(TestCoroutineDispatcher())
    val apiBackend = mockk<ApiBackend<Any>>()

    @Test
    fun `test human verification called`() = runBlockingTest {
        val humanVerificationDetails =
            HumanVerificationDetails(listOf(VerificationMethod.CAPTCHA, VerificationMethod.EMAIL), "test")
        val apiResult = ApiResult.Error.Http(
            422,
            "Human Verification required",
            ApiResult.Error.ProtonData(
                9001,
                "Human Verification required",
                humanVerificationDetails
            )
        )

        coEvery { sessionListener.onHumanVerificationNeeded(any(), any()) } returns SessionListener.HumanVerificationResult.Success
        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")

        val humanVerificationHandler =
            HumanVerificationHandler<Any>(sessionId, sessionProvider, sessionListener, scope)

        val result = humanVerificationHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) {
            sessionListener.onHumanVerificationNeeded(any(), humanVerificationDetails)
        }
    }

    @Test
    fun `test human verification not called on other errors`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            422,
            "Some error",
            ApiResult.Error.ProtonData(
                9000,
                "Some error"
            )
        )

        val humanVerificationHandler =
            HumanVerificationHandler<Any>(sessionId, sessionProvider, sessionListener, scope)

        val result = humanVerificationHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            sessionListener.onHumanVerificationNeeded(any(), any())
        }
    }

    @Test
    fun `test no proton data does not crash the handler`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            422,
            "Some error",
            null
        )

        val humanVerificationHandler =
            HumanVerificationHandler<Any>(sessionId, sessionProvider, sessionListener, scope)

        val result = humanVerificationHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            sessionListener.onHumanVerificationNeeded(any(), any())
        }
    }

    @Test
    fun `test connectivity error does not invoke human verification`() = runBlockingTest {
        val apiResult = ApiResult.Error.Connection(
            false
        )

        val humanVerificationHandler =
            HumanVerificationHandler<Any>(sessionId, sessionProvider, sessionListener, scope)

        val result = humanVerificationHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            sessionListener.onHumanVerificationNeeded(any(), any())
        }
    }

}

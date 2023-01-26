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

package me.proton.core.observability.data.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.observability.data.api.ObservabilityApi
import me.proton.core.observability.domain.usecase.SendObservabilityEvents
import me.proton.core.test.android.api.TestApiManager
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SendObservabilityEventsTest {
    private lateinit var api: ObservabilityApi
    private lateinit var tested: SendObservabilityEvents

    @BeforeTest
    fun setUp() {
        api = mockk()
        tested = SendObservabilityEventsImpl(mockk {
            coEvery { get(ObservabilityApi::class) } returns TestApiManager(api)
        })
    }

    @Test
    fun sendsEvents() = runTest {
        coEvery { api.postDataMetrics(any()) } returns GenericResponse(ResponseCodes.OK)
        tested(mockk())
    }

    @Test
    fun rethrowsOnRetryableError() = runTest {
        coEvery { api.postDataMetrics(any()) } throws
            HttpException(
                Response.error<GenericResponse>(
                    HttpResponseCodes.HTTP_TOO_MANY_REQUESTS,
                    "429 Too many requests".toResponseBody()
                )
            )

        assertFailsWith<ApiException> {
            tested(mockk())
        }
    }

    @Test
    fun ignoresUnrecoverableError() = runTest {
        coEvery { api.postDataMetrics(any()) } throws SerializationException()
        tested(mockk())
    }
}

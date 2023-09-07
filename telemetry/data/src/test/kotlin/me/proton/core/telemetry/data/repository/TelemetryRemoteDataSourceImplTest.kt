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

package me.proton.core.telemetry.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.telemetry.data.api.TelemetryApi
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.repository.TelemetryRemoteDataSource
import me.proton.core.test.android.api.TestApiManager
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TelemetryRemoteDataSourceImplTest {
    private lateinit var api: TelemetryApi
    private lateinit var source: TelemetryRemoteDataSource

    private val sampleEvent =
        TelemetryEvent(
            group = "group",
            name = "name",
            values = mapOf("key" to 0F),
            dimensions = mapOf("dimension" to "value")
        )

    @BeforeTest
    fun setUp() {
        api = mockk()
        source = TelemetryRemoteDataSourceImpl(
            mockk {
                coEvery { get(TelemetryApi::class, any()) } returns TestApiManager(api)
            }
        )
    }

    @Test
    fun sendsEvents() = runTest {
        coEvery { api.postDataMetrics(any()) } returns GenericResponse(ResponseCodes.OK)
        source.sendEvents(null, listOf(sampleEvent))
    }

    @Test
    fun rethrowsError() = runTest {
        coEvery { api.postDataMetrics(any()) } throws
            HttpException(
                Response.error<GenericResponse>(
                    HttpResponseCodes.HTTP_TOO_MANY_REQUESTS,
                    "429 Too many requests".toResponseBody()
                )
            )

        assertFailsWith<ApiException> {
            source.sendEvents(null, listOf(sampleEvent))
        }
    }
}

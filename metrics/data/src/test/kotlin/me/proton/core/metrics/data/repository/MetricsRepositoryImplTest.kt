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

package me.proton.core.metrics.data.repository

import io.mockk.called
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.proton.core.domain.entity.UserId
import me.proton.core.metrics.data.remote.MetricsApi
import me.proton.core.metrics.data.remote.request.toMetricsRequest
import me.proton.core.metrics.domain.entity.Metrics
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import org.junit.Before
import org.junit.Test

internal class MetricsRepositoryImplTest {

    private lateinit var apiProvider: ApiProvider

    private val apiManagerFactory = mockk<ApiManagerFactory>(relaxed = true)

    private val sessionProvider = mockk<SessionProvider>(relaxed = true)

    private val authenticatedApi = mockk<MetricsApi>()

    private val unauthenticatedApi = mockk<MetricsApi>()

    private val userId = UserId("test-user-id")

    private val sessionId = SessionId("session-id")

    private val metrics = Metrics(
        logTag = "Hello",
        title = "world",
        data = buildJsonObject {
            put("lorem", "ipsum")
        }
    )

    lateinit var metricsRepository: MetricsRepositoryImpl

    @Before
    fun setUp() {
        coEvery { sessionProvider.getSessionId(userId) } returns sessionId
        every {
            apiManagerFactory.create(
                interfaceClass = MetricsApi::class
            )
        } returns mockk {
            val block = slot<suspend MetricsApi.() -> Unit>()
            coEvery { invoke<Unit>(any(), capture(block)) } coAnswers {
                block.captured(unauthenticatedApi)
                ApiResult.Success(Unit)
            }
        }
        every {
            apiManagerFactory.create(
                sessionId,
                interfaceClass = MetricsApi::class
            )
        } returns mockk {
            val block = slot<suspend MetricsApi.() -> Unit>()
            coEvery { invoke<Unit>(any(), capture(block)) } coAnswers {
                block.captured(authenticatedApi)
                ApiResult.Success(Unit)
            }
        }
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider)
        metricsRepository = MetricsRepositoryImpl(apiProvider)
    }

    @Test
    fun `post(userId, metrics) makes an authenticated api call to post the metrics`() = runBlockingTest {
        // given
        coJustRun { authenticatedApi.postMetrics(any()) }
        // when
        metricsRepository.post(userId, metrics)
        // then 
        coVerify {
            authenticatedApi.postMetrics(metrics.toMetricsRequest())
            unauthenticatedApi wasNot called
        }
    }

    @Test
    fun `post(null, metrics) makes an unauthenticated api call to post the metrics`() = runBlockingTest {
        // given
        coJustRun { unauthenticatedApi.postMetrics(any()) }
        // when
        metricsRepository.post(null, metrics)
        // then
        coVerify {
            unauthenticatedApi.postMetrics(metrics.toMetricsRequest())
            authenticatedApi wasNot called
        }
    }
}

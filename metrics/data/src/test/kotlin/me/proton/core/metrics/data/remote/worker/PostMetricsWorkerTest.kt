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

package me.proton.core.metrics.data.remote.worker

import android.content.Context
import androidx.work.WorkerParameters
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.proton.core.domain.entity.UserId
import me.proton.core.metrics.domain.entity.Metrics
import me.proton.core.metrics.domain.repository.MetricsRepository
import me.proton.core.util.kotlin.serialize

import org.junit.Before
import org.junit.Test

internal class PostMetricsWorkerTest {


    private val context = mockk<Context>(relaxed = true)

    private val params = mockk<WorkerParameters>(relaxed = true)

    private val metricsRepository = mockk<MetricsRepository>()

    private lateinit var worker: PostMetricsWorker

    private val metrics = Metrics(
        logTag = "Hello",
        title = "world",
        data = buildJsonObject {
            put("lorem", "ipsum")
        }
    )

    private val userId = UserId("test-user-id")

    @Before
    fun setUp() {
        worker = PostMetricsWorker(context, params, metricsRepository)
    }

    @Test
    fun `worker posts the metrics to the backend`() = runTest {
        // given
        every { params.inputData.getString(PostMetricsWorker.INPUT_USER_ID) } returns userId.id
        every { params.inputData.getString(PostMetricsWorker.INPUT_METRICS) } returns metrics.serialize()
        coJustRun { metricsRepository.post(any(), any()) }
        // when
        worker.doWork()
        // then
        coVerify {
            metricsRepository.post(userId, metrics)
        }
    }

    @Test
    fun `worker posts the metrics to the backend without userId`() = runTest {
        // given
        every { params.inputData.getString(PostMetricsWorker.INPUT_USER_ID) } returns null
        every { params.inputData.getString(PostMetricsWorker.INPUT_METRICS) } returns metrics.serialize()
        coJustRun { metricsRepository.post(any(), any()) }
        // when
        worker.doWork()
        // then
        coVerify {
            metricsRepository.post(null, metrics)
        }
    }
}

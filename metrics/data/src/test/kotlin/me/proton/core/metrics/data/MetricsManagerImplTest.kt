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

package me.proton.core.metrics.data

import androidx.work.WorkManager
import androidx.work.WorkRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.proton.core.domain.entity.UserId
import me.proton.core.metrics.data.remote.worker.PostMetricsWorker
import me.proton.core.metrics.domain.entity.Metrics
import me.proton.core.util.kotlin.serialize
import org.junit.Before
import org.junit.Test

internal class MetricsManagerImplTest {

    private val workManager = mockk<WorkManager>()
    private lateinit var metricsManager: MetricsManagerImpl

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
        metricsManager = MetricsManagerImpl(workManager)
    }

    @Test
    fun `send() enqueues a worker to post the metrics`() {
        // given
        coEvery { workManager.enqueue(any<WorkRequest>()) } returns mockk()
        // when
        metricsManager.send(userId, metrics)
        // then
        coVerify {
            workManager.enqueue(
                match<WorkRequest> {
                    it.workSpec.workerClassName == PostMetricsWorker::class.java.name &&
                        it.workSpec.input.getString(PostMetricsWorker.INPUT_USER_ID) == userId.id &&
                        it.workSpec.input.getString(PostMetricsWorker.INPUT_METRICS) == metrics.serialize()
                }
            )
        }
    }
}

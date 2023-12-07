package me.proton.core.eventmanager.data.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManager
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.util.kotlin.serialize
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EventWorkerDoWorkTest {

    private val userId = UserId("userId")
    private val config = EventManagerConfig.Core(userId) as EventManagerConfig
    private val serializedConfig = config.serialize()
    private val data = workDataOf(EventWorker.KEY_INPUT_CONFIG to serializedConfig)
    private val manager = mockk<EventManager>(relaxed = true) {
        coEvery { this@mockk.process() } returns Unit
    }
    private val context = mockk<Context>(relaxed = true)
    private val params = mockk<WorkerParameters>(relaxed = true) {
        every { this@mockk.inputData } returns data
    }
    private val provider = mockk<EventManagerProvider>(relaxed = true) {
        coEvery { this@mockk.get(config) } returns manager
    }

    private fun buildWorker() = EventWorker(context, params, provider)

    @Test
    fun doWorkReturnSuccess() = runTest {
        // WHEN
        val result = buildWorker().doWork()
        // THEN
        assertIs<ListenableWorker.Result.Success>(result)
    }

    @Test
    fun doWorkReturnRetryOnException() = runTest {
        // GIVEN
        coEvery { manager.process() } throws Exception("error")
        // WHEN
        val result = buildWorker().doWork()
        // THEN
        assertIs<ListenableWorker.Result.Retry>(result)
    }

    @Test
    fun doWorkReturnRetryOnApiException() = runTest {
        // GIVEN
        coEvery { manager.process() } throws ApiException(ApiResult.Error.Connection())
        // WHEN
        val result = buildWorker().doWork()
        // THEN
        assertIs<ListenableWorker.Result.Retry>(result)
    }

    @Test
    fun doWorkReturnFailureOnCancellation() = runTest {
        // GIVEN
        coEvery { manager.process() } throws CancellationException()
        // WHEN
        val result = buildWorker().doWork()
        // THEN
        assertIs<ListenableWorker.Result.Failure>(result)
    }

    @Test
    fun getRequestForContainsTags() = runTest {
        // WHEN
        val request = EventWorker.getRequestFor(
            config = config,
            backoffDelay = 30.seconds,
            repeatInterval = 30.minutes,
            initialDelay = 0.seconds,
            requiresBatteryNotLow = false,
            requiresStorageNotLow = false
        )
        // THEN
        assertContains(request.tags, serializedConfig)
        assertContains(request.tags, config.id)
        assertContains(request.tags, config.listenerType.name)
        assertContains(request.tags, config.userId.id)
    }

    @Test
    fun getRequestForContainsDurations() = runTest {
        // WHEN
        val request = EventWorker.getRequestFor(
            config = config,
            backoffDelay = 30.seconds,
            repeatInterval = 30.minutes,
            initialDelay = 0.seconds,
            requiresBatteryNotLow = false,
            requiresStorageNotLow = false
        )
        // THEN
        assertEquals(request.workSpec.backoffDelayDuration, 30.seconds.inWholeMilliseconds)
        assertEquals(request.workSpec.intervalDuration, 30.minutes.inWholeMilliseconds)
        assertEquals(request.workSpec.initialDelay, 0.seconds.inWholeMilliseconds)
    }
}

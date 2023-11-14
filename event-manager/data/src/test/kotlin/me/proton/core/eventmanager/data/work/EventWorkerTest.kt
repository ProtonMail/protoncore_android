package me.proton.core.eventmanager.data.work

import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import org.junit.Test
import kotlin.test.assertEquals

class EventWorkerTest {

    // WARNING: Any failing tests below potentially break EventLoop enqueue/cancel logic.

    @Test
    fun coreEventManagerConfigSerializationForTag() {
        val expected = "{\"type\":\"me.proton.core.eventmanager.domain.EventManagerConfig.Core\",\"userId\":{\"id\":\"userId\"}}"
        val userId = UserId("userId")
        val config = EventManagerConfig.Core(userId)
        val actual = EventWorker.getRequestTagFor(config)
        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun calendarEventManagerConfigSerializationForTag() {
        val expected = "{\"type\":\"me.proton.core.eventmanager.domain.EventManagerConfig.Calendar\",\"userId\":{\"id\":\"userId\"},\"calendarId\":\"calendarId\"}"
        val userId = UserId("userId")
        val calendarId = "calendarId"
        val apiVersion = "v1"
        val config = EventManagerConfig.Calendar(userId, calendarId, apiVersion)
        val actual = EventWorker.getRequestTagFor(config)
        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun driveShareEventManagerConfigSerializationForTag() {
        val expected = "{\"type\":\"me.proton.core.eventmanager.domain.EventManagerConfig.Drive.Share\",\"userId\":{\"id\":\"userId\"},\"shareId\":\"shareId\"}"
        val userId = UserId("userId")
        val shareId = "shareId"
        val config = EventManagerConfig.Drive.Share(userId, shareId)
        val actual = EventWorker.getRequestTagFor(config)
        assertEquals(expected = expected, actual = actual)

        val configVolume = EventManagerConfig.Drive.Share(userId, shareId)
        assertEquals(expected = expected, actual = EventWorker.getRequestTagFor(configVolume))
    }

    @Test
    fun driveVolumeEventManagerConfigSerializationForTag() {
        val expected = "{\"type\":\"me.proton.core.eventmanager.domain.EventManagerConfig.Drive.Volume\",\"userId\":{\"id\":\"userId\"},\"volumeId\":\"volumeId\"}"
        val userId = UserId("userId")
        val volumeId = "volumeId"
        val config = EventManagerConfig.Drive.Volume(userId, volumeId)
        val actual = EventWorker.getRequestTagFor(config)
        assertEquals(expected = expected, actual = actual)
    }
}

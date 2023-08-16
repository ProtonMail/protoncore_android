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

package me.proton.core.util.android.sentry

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.sentry.Hint
import io.sentry.SentryEvent
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.util.android.device.isDeviceRooted
import me.proton.core.util.android.sentry.CustomSentryTagsProcessor.Companion.APP_VERSION
import me.proton.core.util.android.sentry.CustomSentryTagsProcessor.Companion.DEVICE_MANUFACTURER
import me.proton.core.util.android.sentry.CustomSentryTagsProcessor.Companion.DEVICE_MODEL
import me.proton.core.util.android.sentry.CustomSentryTagsProcessor.Companion.OS_NAME
import me.proton.core.util.android.sentry.CustomSentryTagsProcessor.Companion.OS_RELEASE
import me.proton.core.util.android.sentry.CustomSentryTagsProcessor.Companion.OS_ROOTED
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CustomSentryTagsProcessorTest {
    private val context = mockk<Context>(relaxed = true)
    private val apiClient = mockk<ApiClient>(relaxed = true)
    private val deviceMetadata = mockk<DeviceMetadata>(relaxed = true)
    private val networkPrefs = mockk<NetworkPrefs>(relaxed = true)
    private lateinit var tagsProcessor: CustomSentryTagsProcessor

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.util.android.device.DeviceUtilsKt")
        tagsProcessor = CustomSentryTagsProcessor(context, apiClient, deviceMetadata, networkPrefs)
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.util.android.device.DeviceUtilsKt")
    }

    @Test
    fun `headers set properly`() {
        every { deviceMetadata.osRelease() } returns "TestOsRelease"
        every { deviceMetadata.manufacturer() } returns "TestManufacturer"
        every { deviceMetadata.deviceModel() } returns "TestDeviceModel"

        every { apiClient.appVersionHeader } returns "TestAppVersion"
        val event = SentryEvent()
        val hint = Hint()
        val eventReturned = tagsProcessor.process(event, hint)

        assertNotNull(eventReturned)
        assertNotNull(eventReturned.tags)
        assertEquals(8, eventReturned.tags!!.size)
        assertEquals("TestOsRelease", eventReturned.getTag(OS_RELEASE))
        assertEquals("TestManufacturer", eventReturned.getTag(DEVICE_MANUFACTURER))
        assertEquals("TestDeviceModel", eventReturned.getTag(DEVICE_MODEL))
        assertEquals("TestAppVersion", eventReturned.getTag(APP_VERSION))
        assertEquals("Android", eventReturned.getTag(OS_NAME))
    }

    @Test
    fun `device rooted true`() {
        every { isDeviceRooted(context) } returns true
        val event = SentryEvent()
        val hint = Hint()
        val eventReturned = tagsProcessor.process(event, hint)

        assertEquals("true", eventReturned!!.getTag(OS_ROOTED))
    }

    @Test
    fun `device rooted false`() {
        every { isDeviceRooted(context) } returns false
        val event = SentryEvent()
        val hint = Hint()
        val eventReturned = tagsProcessor.process(event, hint)

        assertEquals("false", eventReturned!!.getTag(OS_ROOTED))
    }
}
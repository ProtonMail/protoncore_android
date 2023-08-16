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
import io.sentry.EventProcessor
import io.sentry.Hint
import io.sentry.SentryEvent
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.util.android.device.isDeviceRooted
import javax.inject.Inject

internal class CustomSentryTagsProcessor @Inject constructor(
    private val context: Context,
    private val apiClient: ApiClient,
    private val deviceMetadata: DeviceMetadata,
    private val networkPrefs: NetworkPrefs
) : EventProcessor {

    override fun process(event: SentryEvent, hint: Hint): SentryEvent? {
        event.setTag(OS_NAME, OS_NAME_VALUE)
        event.setTag(OS_RELEASE, deviceMetadata.osRelease())
        event.setTag(OS_DISPLAY, deviceMetadata.osDisplay())
        event.setTag(OS_ROOTED, isDeviceRooted(context).toString())
        event.setTag(DEVICE_MANUFACTURER, deviceMetadata.manufacturer())
        event.setTag(DEVICE_MODEL, deviceMetadata.deviceModel())
        event.setTag(APP_VERSION, apiClient.appVersionHeader)
        event.setTag(APP_ALT_ROUTING, (networkPrefs.activeAltBaseUrl != null).toString())
        return event
    }

    internal companion object {
        internal const val OS_NAME_VALUE = "Android"
        internal const val OS_NAME = "os.name"
        internal const val OS_RELEASE = "os.release"
        internal const val OS_DISPLAY = "os.display"
        internal const val OS_ROOTED = "os.rooted"
        internal const val DEVICE_MANUFACTURER = "device.manufacturer"
        internal const val DEVICE_MODEL = "device.model"
        internal const val APP_VERSION = "app.version"
        internal const val APP_ALT_ROUTING = "app.alternateRouting"
    }
}
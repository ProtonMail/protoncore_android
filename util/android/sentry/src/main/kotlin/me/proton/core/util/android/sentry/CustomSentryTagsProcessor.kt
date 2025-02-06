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
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.EventProcessor
import io.sentry.Hint
import io.sentry.SentryEvent
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.util.android.device.DeviceMetadata
import me.proton.core.payment.domain.usecase.GoogleServicesAvailability
import me.proton.core.payment.domain.usecase.GoogleServicesUtils
import me.proton.core.util.android.device.isDeviceRooted
import java.util.Locale
import java.util.Optional
import java.util.TimeZone
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

public class CustomSentryTagsProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiClient: ApiClient,
    private val deviceMetadata: DeviceMetadata,
    private val networkPrefs: NetworkPrefs,
    private val googleServicesUtils: Optional<GoogleServicesUtils>
) : EventProcessor {

    override fun process(event: SentryEvent, hint: Hint): SentryEvent {
        event.setTag(OS_NAME, OS_NAME_VALUE)
        event.setTag(OS_RELEASE, deviceMetadata.osRelease())
        event.setTag(OS_DISPLAY, deviceMetadata.osDisplay())
        event.setTag(OS_ROOTED, isDeviceRooted(context).toString())
        event.setTag(DEVICE_MANUFACTURER, deviceMetadata.manufacturer())
        event.setTag(DEVICE_MODEL, deviceMetadata.deviceModel())
        event.setTag(APP_VERSION, apiClient.appVersionHeader)
        event.setTag(APP_ALT_ROUTING, (networkPrefs.activeAltBaseUrl != null).toString())
        event.setTag(TIMEZONE, TimeZone.getDefault().id)
        event.setTag(LOCALE, Locale.getDefault().toString())
        event.setTag(GOOGLE_PLAY_SERVICES_AVAILABLE, getGooglePlayServicesAvailability().toString())
        event.setTag(GOOGLE_PLAY_SERVICES_VERSION, getGooglePlayServicesVersion().toString())
        return event
    }

    private fun getGooglePlayServicesAvailability(): GoogleServicesAvailability =
        googleServicesUtils.getOrNull()?.isGooglePlayServicesAvailable() ?: GoogleServicesAvailability.Unknown

    private fun getGooglePlayServicesVersion(): Int = googleServicesUtils.getOrNull()?.getApkVersion() ?: -1

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
        internal const val GOOGLE_PLAY_SERVICES_AVAILABLE = "google.play.services.available"
        internal const val GOOGLE_PLAY_SERVICES_VERSION = "google.play.services.version"
        internal const val LOCALE = "locale"
        internal const val TIMEZONE = "timezone"
    }
}
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

package me.proton.android.core.coreexample.hilttests.di

import me.proton.android.core.coreexample.hilttests.mocks.AndroidTestApiClient

internal val CalendarApiClient = AndroidTestApiClient(
    appName = "android-calendar",
    productName = "ProtonCalendar",
    versionName = "2.21.2"
)

internal val DriveApiClient = AndroidTestApiClient(
    appName = "android-drive",
    productName = "ProtonDrive",
    versionName = "2.7.0"
)

internal val MailApiClient = AndroidTestApiClient(
    appName = "android-mail",
    productName = "ProtonMail",
    versionName = "4.0.16"
)

internal val VpnApiClient = AndroidTestApiClient(
    appName = "android-vpn",
    productName = "ProtonVPN",
    versionName = "5.5.27.0"
)

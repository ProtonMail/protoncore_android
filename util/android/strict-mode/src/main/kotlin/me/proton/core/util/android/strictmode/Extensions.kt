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

package me.proton.core.util.android.strictmode

import android.os.Build
import android.os.StrictMode

public fun StrictMode.VmPolicy.Builder.detectCommon(): StrictMode.VmPolicy.Builder =
    this
        .detectActivityLeaks()
        .detectCleartextNetwork()
        .detectFileUriExposure()
        .detectLeakedClosableObjects()
        .detectLeakedRegistrationObjects()
        .detectLeakedSqlLiteObjects()
        // .detectUntaggedSockets() // Not needed (unless we want to use `android.net.TrafficStats`).
        // .detectNonSdkApiUsage() // Skip: some androidx libraries violate this.
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                detectContentUriWithoutPermission()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                detectCredentialProtectedWhileLocked()
                detectImplicitDirectBoot()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                detectIncorrectContextUse()
                detectUnsafeIntentLaunch()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                penaltyDeathOnFileUriExposure()
            }
        }

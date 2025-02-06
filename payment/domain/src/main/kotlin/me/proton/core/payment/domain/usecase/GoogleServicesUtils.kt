/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.payment.domain.usecase

/** Utils for Google Play Services.
 * NOTE: This class is provided conditionally,
 * only if the app includes the `payment-iap` module.
 * When injecting, use `Optional<GoogleServicesUtils>`.
 */
public interface GoogleServicesUtils {
    /** Returns the version of the Google Play Services. */
    public fun getApkVersion(): Int
    public fun isGooglePlayServicesAvailable(): GoogleServicesAvailability
}

public enum class GoogleServicesAvailability {
    Success,
    ServiceMissing,
    ServiceUpdating,
    ServiceVersionUpdateRequired,
    ServiceDisabled,
    ServiceInvalid,
    Unknown,
    Unexpected
}

/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.paymentiap.data

import android.content.Context
import com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED
import com.google.android.gms.common.ConnectionResult.SERVICE_INVALID
import com.google.android.gms.common.ConnectionResult.SERVICE_MISSING
import com.google.android.gms.common.ConnectionResult.SERVICE_UPDATING
import com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.payment.domain.usecase.GoogleServicesAvailability
import me.proton.core.payment.domain.usecase.GoogleServicesUtils
import javax.inject.Inject

public class GoogleServicesUtilsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GoogleServicesUtils {
    override fun getApkVersion(): Int = GoogleApiAvailability.getInstance().getApkVersion(context)

    override fun isGooglePlayServicesAvailable(): GoogleServicesAvailability =
        when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
            SUCCESS -> GoogleServicesAvailability.Success
            SERVICE_MISSING -> GoogleServicesAvailability.ServiceMissing
            SERVICE_UPDATING -> GoogleServicesAvailability.ServiceUpdating
            SERVICE_VERSION_UPDATE_REQUIRED -> GoogleServicesAvailability.ServiceVersionUpdateRequired
            SERVICE_DISABLED -> GoogleServicesAvailability.ServiceDisabled
            SERVICE_INVALID -> GoogleServicesAvailability.ServiceInvalid
            else -> GoogleServicesAvailability.Unexpected
        }
}

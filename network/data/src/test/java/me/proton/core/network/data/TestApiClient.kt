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

package me.proton.core.network.data

import android.os.Build
import me.proton.core.network.domain.ApiClient
import java.util.Locale
import javax.inject.Inject

const val VERSION_NAME = "3.0.0" // imitating ProtonMail version

class TestApiClient @Inject constructor() : ApiClient {
    override val shouldUseDoh: Boolean = false
    override val appVersionHeader: String = "android-mail@$VERSION_NAME"
    override val userAgent: String = String.format(
        Locale.US,
        "%s/%s (Android %s; %s; %s %s; %s)",
        "ProtonMail",
        VERSION_NAME,
        Build.VERSION.RELEASE,
        Build.MODEL,
        Build.BRAND,
        Build.DEVICE,
        Locale.getDefault().language
    )
    override val enableDebugLogging: Boolean = true
    override fun forceUpdate(errorMessage: String) {}
}

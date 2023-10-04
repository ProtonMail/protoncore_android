/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.plan.data

import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.plan.domain.PlanIconsEndpointProvider
import okhttp3.HttpUrl
import javax.inject.Inject

class PlanIconsEndpointProviderImpl @Inject constructor(
    @BaseProtonApiUrl
    private val baseProtonApiUrl: HttpUrl,
    private val networkPrefs: NetworkPrefs,
) : PlanIconsEndpointProvider {

    override fun get(): String = when (networkPrefs.activeAltBaseUrl) {
        null -> "$baseProtonApiUrl/payments/v5/resources/icons/"
        else -> "${networkPrefs.activeAltBaseUrl}/payments/v5/resources/icons/"
    }.replace("//", "/")
}

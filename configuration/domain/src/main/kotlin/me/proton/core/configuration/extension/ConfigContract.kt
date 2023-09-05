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

package me.proton.core.configuration.extension

import me.proton.core.configuration.entity.ConfigContract
import me.proton.core.configuration.entity.DefaultConfig
import me.proton.core.network.data.di.Constants
import me.proton.core.network.data.di.Constants.DEFAULT_SPKI_PINS

public val ConfigContract.certificatePins: Array<String>
    get() = if (useDefaultPins == true) DEFAULT_SPKI_PINS else emptyArray()

public val ConfigContract.apiPins: List<String>
    get() =
        if (useDefaultPins == true) Constants.ALTERNATIVE_API_SPKI_PINS else emptyList()

public val dohProvidersUrls: Array<String> get() = Constants.DOH_PROVIDERS_URLS

public val testTag: String get() = "me.proton.core.configuration"

public fun ConfigContract.mergeWith(other: ConfigContract): ConfigContract = DefaultConfig(
    other.host ?: host,
    other.proxyToken ?: proxyToken,
    other.apiPrefix ?: apiPrefix,
    other.baseUrl ?: baseUrl,
    other.apiHost ?: apiHost,
    other.hv3Host ?: hv3Host,
    other.hv3Url ?: hv3Url,
    other.useDefaultPins ?: useDefaultPins
)

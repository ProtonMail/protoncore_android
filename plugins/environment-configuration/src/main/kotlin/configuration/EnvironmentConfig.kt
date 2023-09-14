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

package configuration

@SuppressWarnings("LongParameterList")
open class EnvironmentConfig(
    open val host: String? = null,
    open val proxyToken: String? = null,
    open val apiPrefix: String? = null,
    open val baseUrl: String? = null,
    open val apiHost: String? = null,
    open val hv3Host: String? = null,
    open val hv3Url: String? = null,
    open val useDefaultPins: Boolean? = host == "proton.me"
)

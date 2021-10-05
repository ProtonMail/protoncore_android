/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

import me.proton.core.util.kotlin.LoggerLogTag

object LogTag {
    /** Default tag for this module. */
    const val DEFAULT: String = "core.network"

    /** Tag for Network API calls. */
    val API_CALL = LoggerLogTag("core.network.api.call")

    /** Tag for token refresh. */
    val REFRESH_TOKEN = LoggerLogTag("core.network.token.refresh")

    /** Tag for Server time parse error. */
    val SERVER_TIME_PARSE_ERROR = LoggerLogTag("core.network.server.time.error")
}

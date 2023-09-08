/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.network.domain

object LogTag {
    /** Default tag for this module. */
    const val DEFAULT: String = "core.network"

    /** Tag for errors related to network interceptors. */
    const val INTERCEPTOR: String = "core.network.interceptor"

    /** Tag for Network API requests.
     *  Only one line with this tag should be logged per request.
     */
    const val API_REQUEST = "core.network.api.request"

    /** Tag for Network API results.
     *  Only one line with this tag should be logged per request.
     */
    const val API_RESPONSE = "core.network.api.response"

    /** Tag for Network API failures.
     *  Only one line with this tag should be logged per request.
     */
    const val API_ERROR = "core.network.api.error"

    /** Tag for Server time parse error. */
    const val SERVER_TIME_PARSE_ERROR = "core.network.server.time.error"
}

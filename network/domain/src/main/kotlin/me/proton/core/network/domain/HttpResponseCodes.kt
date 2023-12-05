/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.network.domain

/**
 * Contains general constants http response codes.
 */
object HttpResponseCodes {
    const val HTTP_UNAUTHORIZED = 401
    const val HTTP_BAD_REQUEST = 400
    const val HTTP_MISDIRECTED_REQUEST = 421
    const val HTTP_UNPROCESSABLE = 422
    const val HTTP_FORBIDDEN = 403
    const val HTTP_REQUEST_TIMEOUT = 408
    const val HTTP_CONFLICT = 409
    const val HTTP_TOO_MANY_REQUESTS = 429
    const val HTTP_SERVICE_UNAVAILABLE = 503
}

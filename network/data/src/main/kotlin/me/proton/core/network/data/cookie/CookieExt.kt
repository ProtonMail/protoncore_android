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

package me.proton.core.network.data.cookie

import okhttp3.Cookie

internal fun Cookie.hasExpired(): Boolean = expiresAt < System.currentTimeMillis()

/** Represents cookie identity with regard to `Set-Cookie` header:
 *  > If the user agent receives a new cookie with the same cookie-name,
 *  domain-value, and path-value as a cookie that it has already stored,
 *  the existing cookie is evicted and replaced with the new cookie.
 *
 * [RFC-6265 Section 4.1.2](https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2)
 */
typealias CookieKey = String

internal fun Cookie.key(): CookieKey = "name=$name domain=$domain path=$path"

internal fun Cookie.toSerializableCookie(): SerializableCookie =
    SerializableCookie(
        name = name,
        value = value,
        expiresAt = expiresAt,
        domain = domain,
        hostOnly = hostOnly,
        path = path,
        secure = secure,
        httpOnly = httpOnly
    )

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

import kotlinx.serialization.Serializable
import okhttp3.Cookie

@Serializable
internal data class SerializableCookies(
    val map: Map<String, SerializableCookie>
)

@Serializable
internal data class SerializableCookie(
    val name: String,
    val value: String,
    val expiresAt: Long,
    val domain: String,
    val hostOnly: Boolean,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean
)

internal fun SerializableCookie.toOkHttpCookie(): Cookie {
    val cookie = this
    return Cookie.Builder().apply {
        name(cookie.name)
        value(cookie.value)
        expiresAt(cookie.expiresAt)
        if (cookie.hostOnly) {
            hostOnlyDomain(cookie.domain)
        } else {
            domain(cookie.domain)
        }
        path(cookie.path)
        if (cookie.secure) secure()
        if (cookie.httpOnly) httpOnly()
    }.build()
}

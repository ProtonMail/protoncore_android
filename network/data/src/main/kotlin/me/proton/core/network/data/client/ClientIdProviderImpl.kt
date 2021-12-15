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

package me.proton.core.network.data.client

import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.session.SessionId
import java.net.HttpCookie
import java.net.URI

class ClientIdProviderImpl(
    private val baseUrl: String,
    private val cookieStore: ProtonCookieStore,
) : ClientIdProvider {

    override fun getClientId(sessionId: SessionId?): ClientId? {
        val cookieSessionId = cookieStore.get(URI.create(baseUrl)).cookieSessionId()
        // When DoH is working, the Session-Id cookie might not be related to the current baseUrl
        val fallbackSessionId = cookieStore.cookies.cookieSessionId()
        return ClientId.newClientId(sessionId, cookieSessionId ?: fallbackSessionId)
    }

}

internal fun List<HttpCookie>.cookieSessionId(): String? = find { it.name == "Session-Id" }?.value

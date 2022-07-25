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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.session.SessionId
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl

class ClientIdProviderImpl constructor(
    private val baseUrl: String,
    private val cookieStore: ProtonCookieStore
) : ClientIdProvider {

    override suspend fun getClientId(sessionId: SessionId?): ClientId? {
        val cookieSessionId = cookieStore.get(baseUrl.toHttpUrl()).cookieSessionId()
        // When DoH is working, the Session-Id cookie might not be related to the current baseUrl
        val fallbackSessionId = suspend { cookieStore.all().cookieSessionId() } // lazy evaluation
        return ClientId.newClientId(sessionId, cookieSessionId ?: fallbackSessionId())
    }

}

@VisibleForTesting
internal suspend fun Flow<Cookie>.cookieSessionId(): String? = firstOrNull { it.name == "Session-Id" }?.value

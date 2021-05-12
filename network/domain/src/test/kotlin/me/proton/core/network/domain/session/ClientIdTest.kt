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

package me.proton.core.network.domain.session

import me.proton.core.test.kotlin.assertIs
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ClientIdTest {

    @Test
    fun `new client id from non-null session and non-null cookie`() {

        val sessionId = SessionId("test-session-id")
        val cookieId = "test-cookie-id"
        val clientId = ClientId.newClientId(sessionId, cookieId)

        assertNotNull(clientId)
        assertIs<ClientId.AccountSession>(clientId)
        assertIs<SessionId>((clientId as ClientId.AccountSession).sessionId)
        assertEquals("test-session-id", clientId.sessionId.id)
    }

    @Test
    fun `new client id from non-null session and null cookie`() {

        val sessionId = SessionId("test-session-id")
        val clientId = ClientId.newClientId(sessionId, null)

        assertNotNull(clientId)
        assertIs<ClientId.AccountSession>(clientId)
        assertIs<SessionId>((clientId as ClientId.AccountSession).sessionId)
        assertEquals("test-session-id", clientId.sessionId.id)
    }

    @Test
    fun `new client id from null session and non-null cookie`() {

        val cookieId = "test-cookie-id"
        val clientId = ClientId.newClientId(null, cookieId)

        assertNotNull(clientId)
        assertIs<ClientId.CookieSession>(clientId)
        assertIs<CookieSessionId>((clientId as ClientId.CookieSession).sessionId)
        assertEquals("test-cookie-id", clientId.sessionId.id)
    }
}

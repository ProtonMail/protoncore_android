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

import io.mockk.every
import io.mockk.mockk
import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.domain.client.ClientId
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Test
import java.net.HttpCookie
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClientIdProviderImplTest : CoroutinesTest {

    lateinit var provider: ClientIdProviderImpl
    private val cookieStore = mockk<ProtonCookieStore>(relaxed = true)

    private val sessionCookie = HttpCookie("Session-Id", "default cookie")
    private val fallbackCookie = HttpCookie("Session-Id", "fallback cookie")

    @Before
    fun setup() {
        provider = ClientIdProviderImpl("https://proton.me", cookieStore)
    }

    @Test
    fun `cookieSessionId extension fun searchs for Session-Id cookie value in list`() {
        // GIVEN
        val cookies = listOf(sessionCookie)
        // WHEN
        val sessionId = cookies.cookieSessionId()
        // THEN
        assertEquals(sessionCookie.value, sessionId)
    }

    @Test
    fun `If a Session-Id cookie with the right Uri is found, we use it`() {
        // GIVEN
        every { cookieStore.get(any()) } returns listOf(sessionCookie)
        // WHEN
        val sessionId = provider.getClientId(null)
        // THEN
        assertEquals(sessionCookie.value, (sessionId as? ClientId.CookieSession)?.id)
    }

    @Test
    fun `If a Session-Id cookie with the right Uri is not found, we use the fallback one`() {
        // GIVEN
        every { cookieStore.get(any()) } returns emptyList()
        every { cookieStore.cookies } returns listOf(fallbackCookie)
        // WHEN
        val sessionId = provider.getClientId(null)
        // THEN
        assertEquals(fallbackCookie.value, (sessionId as? ClientId.CookieSession)?.id)
    }

    @Test
    fun `If no Session-Id cookie is found and fallback one is null, sessionId will be null too`() {
        // GIVEN
        every { cookieStore.get(any()) } returns emptyList()
        every { cookieStore.cookies } returns emptyList()
        // WHEN
        val sessionId = provider.getClientId(null)
        // THEN
        assertNull(sessionId)
    }

}

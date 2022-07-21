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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.domain.client.ClientId
import me.proton.core.test.kotlin.CoroutinesTest
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClientIdProviderImplTest : CoroutinesTest {

    lateinit var provider: ClientIdProviderImpl
    private val cookieJar = mockk<ProtonCookieStore>()

    private val domain = "proton.me"
    private val sessionCookie = Cookie.Builder().name("Session-Id").value("default cookie").domain(domain).build()
    private val fallbackCookie = Cookie.Builder().name("Session-Id").value("fallback cookie").domain(domain).build()

    @Before
    fun setup() {
        provider = ClientIdProviderImpl("https://$domain".toHttpUrl(), cookieJar)
    }

    @Test
    fun `cookieSessionId extension fun searchs for Session-Id cookie value in list`() = runBlockingTest {
        // GIVEN
        val cookies = flowOf(sessionCookie)
        // WHEN
        val sessionId = cookies.cookieSessionId()
        // THEN
        assertEquals(sessionCookie.value, sessionId)
    }

    @Test
    fun `If a Session-Id cookie with the right Uri is found, we use it`() = runBlockingTest {
        // GIVEN
        every { cookieJar.get(any()) } returns flowOf(sessionCookie)
        // WHEN
        val sessionId = provider.getClientId(null)
        // THEN
        assertEquals(sessionCookie.value, (sessionId as? ClientId.CookieSession)?.id)
    }

    @Test
    fun `If a Session-Id cookie with the right Uri is not found, we use the fallback one`() = runBlockingTest {
        // GIVEN
        every { cookieJar.get(any()) } returns flowOf()
        every { cookieJar.all() } returns flowOf(fallbackCookie)
        // WHEN
        val sessionId = provider.getClientId(null)
        // THEN
        assertEquals(fallbackCookie.value, (sessionId as? ClientId.CookieSession)?.id)
    }

    @Test
    fun `If no Session-Id cookie is found and fallback one is null, sessionId will be null too`() = runBlockingTest {
        // GIVEN
        every { cookieJar.get(any()) } returns flowOf()
        every { cookieJar.all() } returns flowOf()
        // WHEN
        val sessionId = provider.getClientId(null)
        // THEN
        assertNull(sessionId)
    }

}

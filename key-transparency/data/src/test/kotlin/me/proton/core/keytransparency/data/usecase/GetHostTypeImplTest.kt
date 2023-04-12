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

package me.proton.core.keytransparency.data.usecase

import io.mockk.every
import io.mockk.mockk
import me.proton.core.keytransparency.domain.usecase.HostType
import okhttp3.HttpUrl
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetHostTypeImplTest {

    private val baseUrl = mockk<HttpUrl>()
    private lateinit var getHostTypeImpl: GetHostTypeImpl

    @Before
    fun setUp() {
        getHostTypeImpl = GetHostTypeImpl(baseUrl)
    }

    @Test
    fun `api-proton-me`() {
        // given
        every { baseUrl.host } returns "api.proton.me"
        // when
        val hostType = getHostTypeImpl()
        // then
        assertEquals(HostType.Production, hostType)
    }

    @Test
    fun `mail-api-proton-me`() {
        // given
        every { baseUrl.host } returns "mail-api.proton.me"
        // when
        val hostType = getHostTypeImpl()
        // then
        assertEquals(HostType.Production, hostType)
    }

    @Test
    fun `multipart-proton-me`() {
        // given
        every { baseUrl.host } returns "something.rubbish.proton.me"
        // when
        val hostType = getHostTypeImpl()
        // then
        assertEquals(HostType.Production, hostType)
    }

    @Test
    fun `api-proton-black`() {
        // given
        every { baseUrl.host } returns "api.proton.black"
        // when
        val hostType = getHostTypeImpl()
        // then
        assertEquals(HostType.Black, hostType)
    }

    @Test
    fun `mail-api-proton-black`() {
        // given
        every { baseUrl.host } returns "mail-api.proton.black"
        // when
        val hostType = getHostTypeImpl()
        // then
        assertEquals(HostType.Black, hostType)
    }

    @Test
    fun `mail-api-scientist-proton-black`() {
        // given
        every { baseUrl.host } returns "mail-api.scientist.proton.black"
        // when
        val hostType = getHostTypeImpl()
        // then
        assertEquals(HostType.Other, hostType)
    }

    @Test
    fun `multipart-proton-black`() {
        // given
        every { baseUrl.host } returns "something.rubbish.proton.black"
        // when
        val hostType = getHostTypeImpl()
        // then
        assertEquals(HostType.Other, hostType)
    }

    @Test
    fun `api-proton-pink`() {
        // given
        every { baseUrl.host } returns "api.proton.pink"
        // when
        val hostType = getHostTypeImpl()
        // then
        assertEquals(HostType.Other, hostType)
    }

    @Test
    fun `other domain`() {
        // given
        every { baseUrl.host } returns "mail.domain.ch"
        // when
        val hostType = getHostTypeImpl()
        // then
        assertEquals(HostType.Other, hostType)
    }
}
